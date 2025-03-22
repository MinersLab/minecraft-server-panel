package minerslab.mcsp.view.app

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.app.instance.Instance
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.service.InstanceService
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Route("/apps", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class AppsView(
    instanceRepository: InstanceRepository,
    authContext: AuthenticationContext,
    private val instanceService: InstanceService
) : VerticalLayout() {

    init {
        isPadding = true
        val addInstanceButton = Button("创建实例", Icon(VaadinIcon.PLUS)).apply {
            addClickListener {
                UI.getCurrent().navigate("app/new/")
            }
            addThemeVariants(ButtonVariant.LUMO_WARNING)
        }
        val grid = Grid(Instance::class.java, false)
        grid.addColumn { it.getName() }.setHeader("实例名称")
        grid.addColumn { if (instanceService.get(it)?.isAlive == true) "运行中" else "未运行" }.setHeader("运行状态")
        grid.addColumn { "${it.config.inputCharset} / ${it.config.outputCharset}" }.setHeader("字节流编码")
        grid.addColumn { if (it.config.lastLaunchTime == null) "-" else LocalDateTime.ofInstant(Instant.ofEpochMilli(it.config.lastLaunchTime!!), ZoneId.systemDefault()) }
            .setHeader("最后启动")
        grid.isRowsDraggable = true
        grid.addComponentColumn { instance ->
            Button("管理").apply {
                addClickListener {
                    UI.getCurrent().navigate("apps/${instance.id}/manage")
                }
            }
        }.setFrozenToEnd(true).setAutoWidth(true).setFlexGrow(0)
        grid.setItems(instanceRepository.findAll().filter { it.config.users.contains(authContext.principalName.get()) })
        add(addInstanceButton, grid)
    }

}