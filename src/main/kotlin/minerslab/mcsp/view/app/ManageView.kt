package minerslab.mcsp.view.app

import com.vaadin.flow.component.Html
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.JavaScript
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.html.Pre
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
import minerslab.mcsp.component.Breadcrumb
import minerslab.mcsp.component.Interval
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.service.InstanceService
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
        isPadding = true
    }

    private fun ansiToHtml(text: String): PendingJavaScriptResult = UI.getCurrent().page.executeJs("return (new AnsiUp).ansi_to_html($0)", text)

    override fun beforeEnter(event: BeforeEnterEvent) {
        val instanceId = event.routeParameters.get("id").get()
        val instance = instanceRepository.findById(UUID.fromString(instanceId))
        val inputCharset: Charset = Charset.forName(instance.config.inputCharset)
        val outputCharset: Charset = Charset.forName(instance.config.outputCharset)
        if (!instance.config.users.contains(authContext.principalName.get())) {
            event.rerouteToError(AccessDeniedException::class.java)
        }
        val outputHtml = Html("<div>无内容</div>").apply {
            setWidthFull()
            setHeightFull()
        }
        val outputPre = Pre(outputHtml).apply {
            style["padding"] = "1em"
        }
        val scroller = Scroller(outputPre)
        scroller.maxHeight = "60vh"
        scroller.setWidthFull()
        scroller.scrollDirection = Scroller.ScrollDirection.BOTH
        fun log(content: String = "") {
            val stream = FileOutputStream(instance.log, true)
            stream.flush()
            stream.write(content.toByteArray(outputCharset))
        }
        val input = TextField().apply {
            setWidthFull()
            placeholder = "输入命令"
            prefixComponent = VaadinIcon.ARROW_RIGHT.create()
            addKeyUpListener {
                if (it.key.matches("Enter")) {
                    log("$value\n")
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
        val startButton = Button("运行").apply {
            isVisible = instanceService.get(instance)?.isAlive != true
            addClickListener {
                instance.log.writeText("")
                log("[MCSP] 启动中...\n> " + instance.config.launchCommandLine + "\n")
                instanceService.run(instance).onExit().handle { process, _ -> log("[MCSP] 程序已退出(${process.exitValue()})！") }
                UI.getCurrent().refreshCurrentRoute(false)
            }
        }
        val stopButton = Button("终止").apply {
            isVisible = instanceService.get(instance)?.isAlive == true
            addThemeVariants(ButtonVariant.LUMO_ERROR)
            addClickListener {
                log("[MCSP] 停止中...\n")
                instanceService.stop(instance)
                UI.getCurrent().refreshCurrentRoute(false)
            }
        }
        val forceStopButton = Button("强制终止").apply {
            isVisible = instanceService.get(instance)?.isAlive == true
            addThemeVariants(ButtonVariant.LUMO_ERROR)
            addClickListener {
                log("[MCSP] 强制停止中...\n")
                instanceService.stop(instance, true)
                UI.getCurrent().refreshCurrentRoute(false)
            }
        }
        @Suppress("ThisExpressionReferencesGlobalObjectJS") val interval = Interval().once {
            val text = instance.log.readText(outputCharset)
            ansiToHtml(text.slice(max(text.length - 20000, 0) until text.length)).then { that ->
                scroller.element.executeJs("return Math.abs(this.scrollTop + this.clientHeight - this.scrollHeight)").then {
                    outputHtml.setHtmlContent(
                        "<div>${that.asString()}</div>"
                    )
                    if (it.asNumber() <= 1.5) scroller.scrollToBottom()
                }
            }
        }.apply { timeout = 1000 }
        val configButton = Button("配置").apply {
            addClickListener {
                UI.getCurrent().navigate("/apps/$instanceId/config")
            }
        }
        val fileButton = Button("文件").apply {
            addClickListener {
                UI.getCurrent().navigate("/apps/$instanceId/file/")
            }
        }
        add(
            interval,
            Breadcrumb(RouterLink("我的应用", AppsView::class.java), Text(instance.getName())),
            H4(
                Icon(VaadinIcon.COG).also { it.style["margin-right"] = "0.5rem" },
                Text(instance.getName())
            ).apply {
                if (instanceService.get(instance)?.isAlive == true) style["color"] = "var(--lumo-success-text-color)"
            },
            scroller,
            input,
            HorizontalLayout().apply {
                add(startButton, stopButton, forceStopButton, configButton, fileButton)
            }
        )
    }

}