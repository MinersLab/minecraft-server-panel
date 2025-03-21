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
import minerslab.mcsp.app.instance.Instance
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository

@Route("/apps", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class AppsView(instanceRepository: InstanceRepository) : VerticalLayout() {

    init {
        isPadding = true
        val addInstanceButton = Button("创建实例", Icon(VaadinIcon.PLUS)).apply {
            addClickListener {
                instanceRepository.addInstance()
                UI.getCurrent().refreshCurrentRoute(false)
            }
            addThemeVariants(ButtonVariant.LUMO_WARNING)
        }
        val grid = Grid(Instance::class.java, false)
        grid.addColumn { it.getName() }.setHeader("实例名称")
        grid.isRowsDraggable = true
        grid.addComponentColumn { instance ->
            Button("管理").apply {
                addClickListener {
                    UI.getCurrent().navigate("apps/${instance.id}/manage")
                }
            }
        }.setFrozenToEnd(true).setAutoWidth(true).setFlexGrow(0)
        grid.setItems(instanceRepository.findAll())
        add(addInstanceButton, grid)
    }

}