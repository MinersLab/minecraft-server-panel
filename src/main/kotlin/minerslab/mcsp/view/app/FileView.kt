package minerslab.mcsp.view.app

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.formlayout.FormLayout
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.menubar.MenuBar
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.FileBuffer
import com.vaadin.flow.router.*
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.component.Breadcrumb
import minerslab.mcsp.entity.instance.Instance
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.security.McspAuthenticationContext
import minerslab.mcsp.service.InstanceService
import minerslab.mcsp.util.*
import minerslab.mcsp.util.FileSizeUtil.formatFileSize
import java.io.File
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.io.path.pathString
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

@Route("/apps/:id/file", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class FileView(
    private val instanceRepository: InstanceRepository,
    private val authContext: McspAuthenticationContext,
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
        authContext.checkAccess(users = instance.config.users)
        val grid = createGrid(base)

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
        val fileClipboard = getFileClipboard()

        grid.selectionMode = Grid.SelectionMode.MULTI
        grid.addComponentColumn {
            createFileNameColumn(it, base).apply {
                if (fileClipboard?.second == true && fileClipboard.first == it)
                    style["color"] = "var(--lumo-primary-text-color)"
            }
        }.setHeader("文件名")
        grid.addColumn { getFileType(it) }.setHeader("类型")
        grid.addColumn { getFileModificationTime(it) }.setHeader("修改时间")
        grid.isRowsDraggable = true
        grid.addColumn { formatFileSize(it.length()) }.setHeader("大小")
        grid.setItems(files)
        grid.setHeightFull()
        grid.addComponentColumn { createActionColumn(it) }.setHeader("操作").setAutoWidth(true)
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
            .format(DATETIME_FORMAT_1)
    }

    private fun createActionColumn(file: File): HorizontalLayout {
        return HorizontalLayout().apply {
            Anchor().apply {
                isVisible = file.isFile
                setTarget("_blank")
                if (isVisible) href = download(file)
                add(Button(Icon(VaadinIcon.DOWNLOAD)).apply { setTooltipText("下载") })
            }.also { add(it) }
            Button(Icon(VaadinIcon.SCISSORS)).apply {
                setTooltipText("移动")
                addClickListener {
                    setFileClipboard(file, true)
                }
            }.also { add(it) }
            Button(Icon(VaadinIcon.COPY)).apply {
                setTooltipText("复制")
                addClickListener {
                    setFileClipboard(file)
                }
            }.also { add(it) }
            Button(Icon(VaadinIcon.EDIT)).apply {
                setTooltipText("重命名")
                addClickListener { openRenameDialog(file) }
            }.also { add(it) }
            Button(Icon(VaadinIcon.TRASH)).apply {
                setTooltipText("删除")
                addThemeVariants(ButtonVariant.LUMO_ERROR)
                addClickListener {
                    showConfirmDialog("删除") {
                        if (it) {
                            file.deleteRecursively()
                            UI.getCurrent().refreshCurrentRoute(false)
                            Notification.show("文件 ${file.name} 已删除")
                        }
                    }
                }
            }.also { add(it) }
        }
    }

    private fun openRenameDialog(file: File) = showPromptDialog(
        "重命名 ${file.name}",
        "新的名称",
        file.name,
        { old, new -> old != new && new.isNotBlank() }
    ) {
        val status = file.renameTo(File(file.parentFile, it))
        Notification.show(if (status) "已将 ${file.name} 重命名为 $it" else "重命名失败")
            .addThemeVariants(
                if (status) NotificationVariant.LUMO_SUCCESS else NotificationVariant.LUMO_WARNING
            )
        UI.getCurrent().refreshCurrentRoute(true)
    }

    private fun createBarButtons(): HorizontalLayout {
        val clipboard = getFileClipboard()
        val items = arrayOf<Component>(
            Button(Icon(VaadinIcon.PASTE)).apply {
                isVisible = clipboard != null && !files.contains(clipboard.first) && !base.normalize()
                    .startsWith(clipboard.first)
                setTooltipText("粘贴")
                if (clipboard?.second == true) addThemeVariants(ButtonVariant.LUMO_WARNING)
                addClickListener {
                    if (clipboard == null) return@addClickListener
                    clipboard.first.copyTo(base.resolve(clipboard.first.name))
                    if (clipboard.second) {
                        clipboard.first.deleteRecursively()
                        clearFileClipboard()
                    }
                    UI.getCurrent().refreshCurrentRoute(false)
                }
            },
            MenuBar().apply {
                val fileMenu = createIconItem(VaadinIcon.PLUS.create(), label = "添加")
                fileMenu.subMenu.createIconItem(VaadinIcon.FILE.create(), label = "新建文件").addClickListener {
                    showCreateDialog()
                }
                fileMenu.subMenu.createIconItem(VaadinIcon.FOLDER.create(), label = "新建文件夹").addClickListener {
                    showCreateDialog(true)
                }
                fileMenu.subMenu.createIconItem(VaadinIcon.UPLOAD.create(), label = "上传文件").addClickListener {
                    showUploadDialog(FileBuffer())
                }
            }
        )
        return row(*items, fullWidth = false)
    }

    private fun showCreateDialog(isFolder: Boolean = false) = showPromptDialog(
        "输入${if (isFolder) "文件夹" else "文件"}名称",
        "名称",
        validator = { _, new -> new.isNotBlank() && !base.getChildFile(new).exists() }
    ) {
        val file = base.getChildFile(it)
        if (isFolder) file.mkdir()
        else file.createNewFile()
        Notification.show("${if (isFolder) "文件夹" else "文件"}创建成功")
        UI.getCurrent().refreshCurrentRoute(false)
    }

    private fun showUploadDialog(buffer: FileBuffer) {
        showDialog {
            val upload = Upload(buffer)
            upload.i18n = UPLOAD_I18N
            upload.isDropAllowed = true
            upload.addStartedListener { file ->
                if (base.getChildFile(file.fileName).exists()) {
                    upload.interruptUpload()
                    Notification.show("文件已存在").addThemeVariants(NotificationVariant.LUMO_WARNING)
                }
            }
            upload.addSucceededListener { event ->
                val file = base.getChildFile(event.fileName)
                if (file.exists()) return@addSucceededListener
                try {
                    Files.copy(buffer.inputStream, file.toPath())
                    Notification.show("文件上传成功").addThemeVariants(NotificationVariant.LUMO_SUCCESS)
                } catch (_: Throwable) {
                    Notification.show("文件上传失败").addThemeVariants(NotificationVariant.LUMO_WARNING)
                }
            }
            headerTitle = "拖拽或移动文件以上传"
            val section = Div(upload)
            add(FormLayout(section))
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
            addToEnd(createBarButtons())
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
                RouterLink(
                    instance.getName(),
                    FileView::class.java,
                    RouteParameters(mapOf("id" to instanceId))
                ).apply { setQueryParameters(event.location.queryParameters.excluding("path")) },
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

    private fun getFileClipboard(): Pair<File, Boolean>? = runCatching {
        val clipboardFile = instance.path.toFile()
            .resolve(event.location.queryParameters.getSingleParameter("clipboard-file").getOrNull() ?: return null)
        val isCut =
            event.location.queryParameters.getSingleParameter("clipboard-cut").map { it == "true" }.getOrElse { false }
        clipboardFile to isCut
    }.getOrNull()

    private fun clearFileClipboard() {
        UI.getCurrent()
            .navigate(event.location.path, event.location.queryParameters.excluding("clipboard-file", "clipboard-cut"))
    }

    private fun setFileClipboard(file: File, cut: Boolean = false) {
        UI.getCurrent().navigate(
            event.location.path,
            event.location.queryParameters.merging(
                "clipboard-file",
                base.resolve(file.path).toRelativeString(instance.path.toFile())
            ).merging("clipboard-cut", cut.toString())
        )
        UI.getCurrent().refreshCurrentRoute(false)
    }

    private fun download(file: File): String {
        val directory = file.parentFile.toRelativeString(instance.path.toFile())
        val path = Path.of(directory).pathString
        val name = URLEncoder.encode(file.name, Charsets.UTF_8)

        return "/api/instance/$instanceId/download/$name?path=${URLEncoder.encode(path, Charsets.UTF_8)}"
    }

    private fun goto(resolve: String) {
        UI.getCurrent().navigate(
            event.location.path,
            event.location.queryParameters.merging(
                "path",
                base.resolve(resolve).toRelativeString(instance.path.toFile())
            )
        )
        UI.getCurrent().refreshCurrentRoute(false)
    }
}