package minerslab.mcsp.view.app

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.AccessDeniedException
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.service.InstanceService
import java.util.*

@Route("/apps/:id/config", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class ConfigView(
    private val instanceRepository: InstanceRepository,
    private val authContext: AuthenticationContext,
    private val instanceService: InstanceService
) : VerticalLayout(), BeforeEnterObserver, RouterLayout {

    override fun beforeEnter(event: BeforeEnterEvent) {
        val instanceId = event.routeParameters.get("id").get()
        val instance = instanceRepository.findById(UUID.fromString(instanceId))
        if (!instance.config.users.contains(authContext.principalName.get())) {
            event.rerouteToError(AccessDeniedException::class.java)
        }
        val config = instance.config
        val name = TextField("实例名") { config.name = it.value }.apply { value = config.name }
        val inputCharset = TextField("输入编码") { config.inputCharset = it.value }.apply { value = config.inputCharset }
        val outputCharset = TextField("输出编码") { config.outputCharset = it.value }.apply { value = config.inputCharset }
        val launchCommandLine = TextField("运行命令") { config.launchCommandLine = it.value }.apply { value = config.launchCommandLine }
        val stopCommand = TextField("终止指令") { config.stopCommand = it.value }.apply { value = config.stopCommand }
        val charsets = HorizontalLayout().apply { add(inputCharset, outputCharset) }
        val commands = HorizontalLayout().apply { add(launchCommandLine, stopCommand) }
        val save = Button("保存").apply {
            addClickListener {
                instance.config = config
                UI.getCurrent().navigate("/apps/$instanceId/manage")
            }
        }
        add(name, charsets, commands, save)
    }

}