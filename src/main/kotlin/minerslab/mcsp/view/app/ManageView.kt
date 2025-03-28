package minerslab.mcsp.view.app

import com.vaadin.flow.component.Html
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.JavaScript
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Paragraph
import com.vaadin.flow.component.html.Pre
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.PendingJavaScriptResult
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.*
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.RolesAllowed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import minerslab.mcsp.app.instance.Instance
import minerslab.mcsp.component.Badge
import minerslab.mcsp.component.Breadcrumb
import minerslab.mcsp.component.Card
import minerslab.mcsp.component.Interval
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.service.InstanceService
import minerslab.mcsp.util.*
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.math.max

@Route("/apps/:id/manage", layout = MainLayout::class)
@RolesAllowed("ADMIN")
@JavaScript("https://cdn.jsdelivr.net/npm/ansi_up@5.1.0/ansi_up.min.js")
class ManageView(
    private val instanceRepository: InstanceRepository,
    private val authContext: AuthenticationContext,
    private val instanceService: InstanceService
) : VerticalLayout(), BeforeEnterObserver, RouterLayout {

    private val commandHistory = mutableListOf<String>()
    private var currentCommand: Int = 0

    init {
        setHeightFull()
        isPadding = true
    }

    private fun ansiToHtml(text: String): PendingJavaScriptResult =
        UI.getCurrent().page.executeJs("return (new AnsiUp).ansi_to_html($0)", text)

    override fun beforeEnter(event: BeforeEnterEvent) {
        val instanceId = event.routeParameters.get("id").get()
        val instance = instanceRepository.findById(UUID.fromString(instanceId))
        val inputCharset: Charset = Charset.forName(instance.config.inputCharset)
        val outputCharset: Charset = Charset.forName(instance.config.outputCharset)
        if (!instance.config.users.contains(authContext.principalName.get())) {
            event.rerouteToError(AccessDeniedException::class.java)
        }
        initializeLayout(instance, inputCharset, outputCharset)
    }

    private fun initializeLayout(instance: Instance, inputCharset: Charset, outputCharset: Charset) {
        val outputHtml = Html("<div>无内容</div>").apply {
            setWidthFull()
            setHeightFull()
        }
        val outputPre = Pre(outputHtml).apply {
            style["padding"] = "1em"
        }
        val scroller = Scroller(outputPre)
        scroller.maxHeight = "70vh"
        scroller.setWidthFull()
        scroller.setHeightFull()
        scroller.scrollDirection = Scroller.ScrollDirection.BOTH

        val input = createCommandInputField(instance, inputCharset, outputCharset, scroller)

        val interval = createInterval(instance, outputCharset, outputHtml, scroller)

        val title = Div(
            Icon(VaadinIcon.COG).also { it.style["margin-right"] = "0.5rem" },
            Text(instance.getName())
        )
        title.setWidthFull()
        val titleBar = Span().apply {
            setWidthFull()
        }
        Interval().timeout(500).once(true) {
            titleBar.removeAll()
            titleBar.add(alignRow(title, row(*createControlButtons(instance), fullWidth = false)))
        }.also { add(it) }
        add(
            interval,
            Breadcrumb(RouterLink("我的应用", AppsView::class.java), Text(instance.getName())),
            titleBar,
            scroller,
            input,
            alignRow(createInformationCard(instance), createFunctionCard(instance))
        )
    }

    private fun createInformationCard(instance: Instance) = Card().apply {
        width = "40%"
        val config = instance.config
        title = Div("基本信息")
        add(Paragraph(instance.getName()))
        val badgeList = Paragraph()
        Interval().timeout(500).once(true) {
            val status = instanceService.getStatus(instance)
            val badges = arrayOf(
                Badge(createIcon(VaadinIcon.INFO_CIRCLE), Span(status.statusName), classes = status.color),
                Badge(Span("启动次数: ${config.launchTimes}")),
            )
            badgeList.removeAll()
            badgeList.add(row(*badges))
        }.also { add(it) }
        add(badgeList)
        if (config.lastLaunchTime != null)
            add(Paragraph("最后启动: ${toLocalDateTime(config.lastLaunchTime!!).format(DATETIME_FORMAT_1)}"))
        add(Paragraph("创建时间: ${toLocalDateTime(config.createdAt).format(DATETIME_FORMAT_1)}"))
        add(Paragraph("输入编码: ${config.inputCharset} 输出编码: ${config.outputCharset}"))
    }

    private fun createFunctionCard(instance: Instance) = Card().apply {
        width = "60%"
        title = Div("功能组")
        add(createManageButtons(instance))
    }

    private fun createCommandInputField(
        instance: Instance,
        inputCharset: Charset,
        outputCharset: Charset,
        scroller: Scroller
    ): TextField {
        return TextField().apply {
            setWidthFull()
            placeholder = "输入命令"
            prefixComponent = VaadinIcon.ARROW_RIGHT.create()
            addKeyUpListener {
                if (it.key.matches("Enter")) {
                    log(instance, "$value\n", outputCharset)
                    instanceService.get(instance)?.outputStream?.write("$value\n".toByteArray(inputCharset))
                    instanceService.get(instance)?.outputStream?.flush()
                    if (commandHistory.contains(value)) commandHistory.remove(value)
                    commandHistory += value
                    currentCommand = commandHistory.size
                    value = ""
                    scroller.scrollToBottom()
                } else if (it.key.matches("ArrowUp")) {
                    currentCommand = max(currentCommand - 1, 0)
                    value = commandHistory.getOrNull(currentCommand) ?: ""
                } else if (it.key.matches("ArrowDown")) {
                    currentCommand = max(currentCommand + 1, commandHistory.size - 1)
                    value = commandHistory.getOrNull(currentCommand) ?: ""
                }
            }
        }
    }

    private fun createControlButtons(instance: Instance): Array<Button> {
        val startButton = Button("运行").apply {
            isVisible = instanceService.get(instance)?.isAlive != true
            addClickListener {
                instance.log.writeText("")
                log(
                    instance,
                    "[MCSP] 启动中...\n> " + instance.config.launchCommandLine + "\n",
                    Charset.forName(instance.config.outputCharset)
                )
                instanceService.run(instance).onExit()
                    .handle { process, _ ->
                        log(
                            instance,
                            "[MCSP] 程序已退出(${process.exitValue()})！",
                            Charset.forName(instance.config.outputCharset)
                        )
                    }
                UI.getCurrent().refreshCurrentRoute(false)
            }
        }
        val stopButton = Button("关闭").apply {
            isVisible = instanceService.getStatus(instance).isRunning
            addClickListener {
                log(instance, "[MCSP] 关闭中...\n", Charset.forName(instance.config.outputCharset))
                CoroutineScope(Dispatchers.Default).async {
                    instanceService.stop(instance)
                }.invokeOnCompletion { UI.getCurrent().refreshCurrentRoute(false) }
            }
        }

        val restartButton = Button("重启").apply {
            isVisible = instanceService.getStatus(instance).isRunning
            addThemeVariants(ButtonVariant.LUMO_CONTRAST)
            addClickListener {
                log(instance, "[MCSP] 重启中...\n", Charset.forName(instance.config.outputCharset))
                CoroutineScope(Dispatchers.Default).async {
                    instanceService.restart(instance)
                }.invokeOnCompletion { UI.getCurrent().refreshCurrentRoute(false) }
            }
        }

        val forceStopButton = Button("终止").apply {
            isVisible = instanceService.get(instance)?.isAlive == true
            addThemeVariants(ButtonVariant.LUMO_ERROR)
            addClickListener {
                log(instance, "[MCSP] 终止中...\n", Charset.forName(instance.config.outputCharset))
                CoroutineScope(Dispatchers.Default).async {
                    instanceService.stop(instance, true)
                }.invokeOnCompletion { UI.getCurrent().refreshCurrentRoute(false) }
            }
        }

        return arrayOf(startButton, stopButton, restartButton, forceStopButton)
    }

    private fun createManageButtons(instance: Instance): HorizontalLayout {
        val configButton = Button("配置").apply {
            addClickListener {
                UI.getCurrent().navigate("/apps/${instance.id}/config")
            }
        }
        val fileButton = Button("文件").apply {
            addClickListener {
                UI.getCurrent().navigate("/apps/${instance.id}/file/")
            }
        }
        return row(configButton, fileButton)
    }

    private fun log(instance: Instance, content: String, charset: Charset) {
        val stream = FileOutputStream(instance.log, true)
        stream.flush()
        stream.write(content.toByteArray(charset))
    }

    @Suppress("ThisExpressionReferencesGlobalObjectJS")
    private fun createInterval(
        instance: Instance,
        outputCharset: Charset,
        outputHtml: Html,
        scroller: Scroller
    ): Interval {
        return Interval().once {
            val text = instance.log.readText(outputCharset)
            ansiToHtml(text.slice(max(text.length - 20000, 0) until text.length)).then { that ->
                scroller.element.executeJs("return Math.abs(this.scrollTop + this.clientHeight - this.scrollHeight)")
                    .then {
                        val htmlContent = "<div>${that.asString()}</div>"
                        if (htmlContent != outputHtml.innerHtml) {
                            outputHtml.setHtmlContent(htmlContent)
                            if (it.asNumber() <= 1.5) scroller.scrollToBottom()
                        }
                    }
            }
        }.apply { timeout = 1000 }
    }
}