package minerslab.mcsp.view.app

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.entity.instance.Instance
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.security.McspAuthenticationContext
import minerslab.mcsp.service.InstanceService
import minerslab.mcsp.util.toLocalDateTime

@Route("/apps", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class AppsView(
    private val authContext: McspAuthenticationContext,
    instanceRepository: InstanceRepository,
    private val instanceService: InstanceService,
) : VerticalLayout() {
    init {
        isPadding = true
        val addInstanceButton =
            Button("创建实例", Icon(VaadinIcon.PLUS)).apply {
                addClickListener {
                    UI.getCurrent().navigate("app/new/")
                }
                addThemeVariants(ButtonVariant.LUMO_WARNING)
            }
        val grid = Grid(Instance::class.java, false)
        grid.addColumn { it.getName() }.setHeader("实例名称")
        grid.addColumn { instanceService.getStatus(it).statusName }.setHeader("运行状态")
        grid.addColumn { "${it.config.inputCharset} / ${it.config.outputCharset}" }.setHeader("字节流编码")
        grid
            .addColumn {
                if (it.config.lastLaunchTime == null) "-" else toLocalDateTime(it.config.lastLaunchTime!!)
            }.setHeader("最后启动")
        grid.isRowsDraggable = true
        grid
            .addComponentColumn { instance ->
                Button("管理").apply {
                    addClickListener {
                        UI.getCurrent().navigate("apps/${instance.id}/manage")
                    }
                }
            }.setFrozenToEnd(true)
            .setAutoWidth(true)
            .setFlexGrow(0)
        grid.setItems(instanceRepository.findAll().filter { it.config.users.contains(authContext.userName) })
        add(addInstanceButton, grid)
    }
}
