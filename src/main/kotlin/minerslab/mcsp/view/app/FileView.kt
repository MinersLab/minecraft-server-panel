package minerslab.mcsp.view.app

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.*
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.component.Breadcrumb
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.service.InstanceService
import minerslab.mcsp.util.FileSizeUtil.formatFileSize
import minerslab.mcsp.util.getChildFile
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Route("/apps/:id/file", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class FileView(
    private val instanceRepository: InstanceRepository,
    private val authContext: AuthenticationContext,
    private val instanceService: InstanceService
) : VerticalLayout(), BeforeEnterObserver, RouterLayout {

    init {
        setHeightFull()
        isSpacing = false
        isPadding = true
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        removeAll()
        val instanceId = event.routeParameters.get("id").get()
        val instance = instanceRepository.findById(UUID.fromString(instanceId))
        val base = event.location.queryParameters.getSingleParameter("path")
            .map { instance.path.toFile().getChildFile(it) }
            .flatMap { if (it.isDirectory) Optional.of(it) else Optional.empty() }
            .getOrElse { instance.path.toFile() }

        if (!instance.config.users.contains(authContext.principalName.get())) {
            event.rerouteToError(AccessDeniedException::class.java)
        }

        fun goto(resolve: String) {
            UI.getCurrent().navigate(
                event.location.path,
                event.location.queryParameters.merging(
                    "path",
                    base.resolve(resolve).toRelativeString(instance.path.toFile())
                )
            )
            UI.getCurrent().refreshCurrentRoute(true)
        }

        val grid = Grid(File::class.java, false)
        val files = base.listFiles()?.filterNot { it.name.startsWith(".mcsp") }?.sortedByDescending { it.isDirectory }?.toMutableList() ?: mutableListOf()
        grid.selectionMode = Grid.SelectionMode.MULTI
        grid.addComponentColumn { that ->
            Span(that.toRelativeString(base)).apply {
                if (!that.isDirectory) return@apply
                classNames += "mcsp-link"
                addClickListener {
                    goto(that.name)
                }
            }
        }.setHeader("文件名")
        grid.addColumn {
            if (it.isDirectory) "文件夹"
            else "${it.extension.uppercase()} 文件"
        }.setHeader("类型")
        grid.addColumn { LocalDateTime.ofInstant(Instant.ofEpochMilli(it.lastModified()), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")) }
            .setHeader("修改时间")
        grid.isRowsDraggable = true
        grid.addColumn { formatFileSize(it.length()) }.setHeader("大小")
        grid.setItems(files)
        grid.setHeightFull()

        var newFileName = ""
        val nameDialog = Dialog().apply {
            isCloseOnEsc = false
            isCloseOnOutsideClick = false
            val input = TextField().apply {
                placeholder = "文件名"
            }.also { add(it) }
            addOpenedChangeListener { if (it.isOpened) input.value = newFileName }
            footer.apply {
                Button("确认").apply {
                    addClickListener {
                        newFileName = input.value
                        input.value = ""
                        close()
                    }
                }.also { add(it) }
                Button("取消").apply {
                    addClickListener {
                        input.value = ""
                        close()
                    }
                }.also { add(it) }
            }

        }
        add(nameDialog)

        grid.addComponentColumn { file ->
            Div().apply {
                Button(Icon(VaadinIcon.EDIT)).apply {
                    setTooltipText("重命名")
                    addClickListener {
                        newFileName = file.name
                        nameDialog.headerTitle = "重命名 ${file.name}"
                        nameDialog.open()
                        nameDialog.addOpenedChangeListener {
                            if (it.isOpened) return@addOpenedChangeListener
                            if (newFileName.isNotBlank() && newFileName != file.name) {
                                val status = file.renameTo(File(file.parentFile, newFileName))
                                Notification.show(if (status) "已将 ${file.name} 重命名为 $newFileName" else "重命名失败").addThemeVariants(
                                    if (status) NotificationVariant.LUMO_SUCCESS else NotificationVariant.LUMO_WARNING
                                )
                            }
                            it.unregisterListener()
                            UI.getCurrent().refreshCurrentRoute(true)
                        }
                    }
                }.also { add(it) }
            }
        }.setHeader("操作")

        val search = TextField().apply {
            placeholder = "搜索"
            addValueChangeListener { that ->
                grid.setItems(files.filter { it.name.contains(that.value, ignoreCase = true) })
            }
        }

        val bar = HorizontalLayout().apply {
            isPadding = true
            isSpacing = true
            addToStart(Span("文件管理"))
            addToMiddle(search)
            addToEnd(Button("新建"))
            setWidthFull()
        }

        val pathComponents = base.toRelativeString(instance.path.toFile())
            .replace('\\', '/')
            .split("/")
            .filterNot { it.isBlank() }
            .toMutableList()
        val fileNav by lazy {
            Breadcrumb(
                RouterLink(instance.getName(), FileView::class.java, RouteParameters(mapOf("id" to instanceId))),
                *pathComponents.dropLast(1).map {
                    RouterLink(
                        it,
                        FileView::class.java,
                        RouteParameters(mapOf("id" to instanceId))
                    ).apply {
                        setQueryParameters(
                            QueryParameters(
                                mapOf(
                                    "path" to listOf(
                                        pathComponents.take(
                                            pathComponents.indexOf(it) + 1
                                        ).joinToString("/")
                                    )
                                )
                            )
                        )
                    }
                }.toTypedArray(),
                Text(pathComponents.last())
            ) { Span("/") }
        }
        add(
            Breadcrumb(
                RouterLink("我的应用", AppsView::class.java),
                RouterLink(instance.getName(), ManageView::class.java, RouteParameters(mapOf("id" to instanceId))),
                Text("文件管理")
            ),
            bar
        )
        if (pathComponents.isNotEmpty()) add(fileNav)
        add(grid)
    }

}