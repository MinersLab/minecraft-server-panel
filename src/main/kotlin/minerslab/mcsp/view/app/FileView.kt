package minerslab.mcsp.view.app

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.*
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.RolesAllowed
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
        isPadding = true
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
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
            UI.getCurrent().refreshCurrentRoute(false)
        }

        val grid = Grid(File::class.java, false)
        val files = base.listFiles()?.filterNot { it.name.startsWith(".mcsp") }?.sortedByDescending { it.isDirectory }?.toMutableList() ?: mutableListOf()
        if (base.parentFile.normalize().startsWith(instance.path.toFile()))
            files.add(0, base.parentFile)
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

        add(bar, grid)

    }

}