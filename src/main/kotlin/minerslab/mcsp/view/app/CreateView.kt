package minerslab.mcsp.view.app

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository

@Route("app/new/", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class CreateView(
    instanceRepository: InstanceRepository,
    authContext: AuthenticationContext,
) : VerticalLayout(),
    RouterLayout {
    init {
        isPadding = true
        val name =
            TextField("实例名称").apply {
                placeholder = "默认名称"
            }
        val confirm =
            Button("创建实例").apply {
                addClickListener {
                    val instance = instanceRepository.addInstance()
                    if (name.value.trim().isEmpty()) name.value = instance.getName()
                    val config = instance.config
                    config.users.add(authContext.principalName.get())
                    config.name = name.value
                    config.createdAt = System.currentTimeMillis()
                    instance.config = config
                    UI.getCurrent().navigate("/apps")
                }
                addThemeVariants(ButtonVariant.LUMO_WARNING)
            }
        add(name, confirm)
    }
}
