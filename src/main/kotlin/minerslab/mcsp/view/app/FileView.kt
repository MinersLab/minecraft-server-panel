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
import minerslab.mcsp.app.instance.Instance
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

    private lateinit var event: BeforeEnterEvent
    private lateinit var base: File
    private lateinit var instanceId: String
    private lateinit var instance: Instance
    private lateinit var files: List<File>

    init {
        setHeightFull()
        isSpacing = false
        isPadding = true
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        removeAll()

        this.event = event
        instanceId = event.routeParameters.get("id").get()
        instance = instanceRepository.findById(UUID.fromString(instanceId))
        base = getBaseDirectory()
        files = base.listFiles()?.filterNot { it.name.startsWith(".mcsp") }?.sortedByDescending { it.isDirectory }
            ?.toMutableList() ?: mutableListOf()

        if (!instance.config.users.contains(authContext.principalName.get())) {
            event.rerouteToError(AccessDeniedException::class.java)
        }

        val grid = createGrid(base)
        val nameDialog = createNameDialog()

        add(nameDialog)
        add(createBar(grid))
        addBreadcrumbs()
        add(grid)
    }

    private fun getBaseDirectory(): File {
        return event.location.queryParameters.getSingleParameter("path")
            .map { instance.path.toFile().getChildFile(it) }
            .flatMap { if (it.isDirectory) Optional.of(it) else Optional.empty() }
            .getOrElse { instance.path.toFile() }
    }

    private fun createGrid(base: File): Grid<File> {
        val grid = Grid(File::class.java, false)

        grid.selectionMode = Grid.SelectionMode.MULTI
        grid.addComponentColumn { createFileNameColumn(it, base) }.setHeader("文件名")
        grid.addColumn { getFileType(it) }.setHeader("类型")
        grid.addColumn { getFileModificationTime(it) }.setHeader("修改时间")
        grid.isRowsDraggable = true
        grid.addColumn { formatFileSize(it.length()) }.setHeader("大小")
        grid.setItems(files)
        grid.setHeightFull()
        grid.addComponentColumn { createActionColumn(it) }.setHeader("操作")
        return grid
    }

    private fun createFileNameColumn(file: File, base: File): Span {
        return Span(file.toRelativeString(base)).apply {
            if (file.isDirectory) {
                classNames += "mcsp-link"
                addClickListener { goto(file.name) }
            }
        }
    }

    private fun getFileType(file: File): String {
        return if (file.isDirectory) "文件夹" else "${file.extension.uppercase()} 文件"
    }

    private fun getFileModificationTime(file: File): String {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss"))
    }

    private fun createActionColumn(file: File): Div {
        return Div().apply {
            Button(Icon(VaadinIcon.EDIT)).apply {
                setTooltipText("重命名")
                addClickListener { openRenameDialog(file) }
            }.also { add(it) }
        }
    }

    private fun openRenameDialog(file: File) {
        val nameDialog = createNameDialog()
        nameDialog.headerTitle = "重命名 ${file.name}"
        nameDialog.open()
        nameDialog.addOpenedChangeListener {
            if (!it.isOpened) {
                val newFileName = nameDialog.getElement().getProperty("value")
                if (newFileName.isNotBlank() && newFileName != file.name) {
                    val status = file.renameTo(File(file.parentFile, newFileName))
                    Notification.show(if (status) "已将 ${file.name} 重命名为 $newFileName" else "重命名失败")
                        .addThemeVariants(
                            if (status) NotificationVariant.LUMO_SUCCESS else NotificationVariant.LUMO_WARNING
                        )
                }
                it.unregisterListener()
                UI.getCurrent().refreshCurrentRoute(true)
            }
        }
    }

    private fun createNameDialog(): Dialog {
        var newFileName = ""
        return Dialog().apply {
            isCloseOnEsc = false
            isCloseOnOutsideClick = false
            val input = TextField().apply { placeholder = "文件名" }.also { add(it) }
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
    }

    private fun createBar(grid: Grid<File>): HorizontalLayout {
        val search = TextField().apply {
            placeholder = "搜索"
            addValueChangeListener { that ->
                grid.setItems(files.filter { it.name.contains(that.value, ignoreCase = true) })
            }
        }

        return HorizontalLayout().apply {
            isPadding = true
            isSpacing = true
            addToStart(Span("文件管理"))
            addToMiddle(search)
            addToEnd(Button("新建"))
            setWidthFull()
        }
    }

    private fun addBreadcrumbs() {
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
                pathComponents.lastOrNull()?.let { Text(it) }
            ) { Span("/") }
        }
        add(
            Breadcrumb(
                RouterLink("我的应用", AppsView::class.java),
                RouterLink(instance.getName(), ManageView::class.java, RouteParameters(mapOf("id" to instanceId))),
                Text("文件管理")
            ),
            fileNav
        )
    }

    private fun goto(resolve: String) {
        UI.getCurrent().navigate(
            event.location.path,
            event.location.queryParameters.merging(
                "path",
                base.resolve(resolve).toRelativeString(instance.path.toFile())
            )
        )
        UI.getCurrent().refreshCurrentRoute(true)
    }
}