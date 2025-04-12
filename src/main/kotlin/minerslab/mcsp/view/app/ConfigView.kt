package minerslab.mcsp.view.app

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.*
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.security.McspAuthenticationContext
import minerslab.mcsp.service.InstanceService
import minerslab.mcsp.util.createSelectionCheckbox
import minerslab.mcsp.util.row
import java.util.*

@Route("/apps/:id/config", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class ConfigView(
    private val authContext: McspAuthenticationContext,
    private val instanceRepository: InstanceRepository,
    private val instanceService: InstanceService
) : VerticalLayout(), BeforeEnterObserver, RouterLayout {

    override fun beforeEnter(event: BeforeEnterEvent) {
        val instanceId = event.routeParameters.get("id").get()
        val instance = instanceRepository.findById(UUID.fromString(instanceId))
        authContext.checkAccess(users = instance.config.users)
        val config = instance.config
        val name = TextField("实例名") { config.name = it.value }.apply { value = config.name }

        val inputCharset =
            TextField("输入编码") { config.inputCharset = it.value }.apply { value = config.inputCharset }
        val outputCharset =
            TextField("输出编码") { config.outputCharset = it.value }.apply { value = config.inputCharset }
        val coloredTerminal = createSelectionCheckbox("彩色终端").apply {
            value = config.coloredTerminal
            addValueChangeListener { config.coloredTerminal = it.value }
        }
        val headlessMode = createSelectionCheckbox("无头模式").apply {
            value = config.headless
            addValueChangeListener { config.headless = it.value }
        }

        val launchCommandLine =
            TextField("运行命令") { config.launchCommandLine = it.value }.apply { value = config.launchCommandLine }
        val stopCommand = TextField("终止指令") { config.stopCommand = it.value }.apply { value = config.stopCommand }

        val backButton = Button("返回").apply {
            addClickListener { UI.getCurrent().navigate("/apps/$instanceId/manage") }
        }
        val save = Button("保存").apply {
            addClickListener {
                instance.config = config
                UI.getCurrent().navigate("/apps/$instanceId/manage")
            }
            addThemeVariants(ButtonVariant.LUMO_SUCCESS)
        }
        add(
            name,
            row(inputCharset, outputCharset),
            row(coloredTerminal, headlessMode),
            row(launchCommandLine, stopCommand),
            row(backButton, save)
        )
    }

}