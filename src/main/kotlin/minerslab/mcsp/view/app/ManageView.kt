package minerslab.mcsp.view.app

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.html.H4
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.*
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.component.Breadcrumb
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import java.util.*

@Route("/apps/:id/manage", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class ManageView(private val instanceRepository: InstanceRepository, private val authContext: AuthenticationContext) : VerticalLayout(), BeforeEnterObserver, RouterLayout {

    init {
        isPadding = true
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        val instanceId = event.routeParameters.get("id").get()
        val instance = instanceRepository.findById(UUID.fromString(instanceId))
        if (!instance.config.users.contains(authContext.principalName.get())) {
            event.rerouteToError(AccessDeniedException::class.java)
        }
        val output = TextArea().apply {
            isReadOnly = true
            setHeightFull()
            setWidthFull()
            minRows = 10
            value = "无内容！"
        }
        val input = TextField().apply {
            setWidthFull()
            placeholder = "输入命令"
            prefixComponent = VaadinIcon.ARROW_RIGHT.create()
        }
        add(
            Breadcrumb(RouterLink("我的应用", AppsView::class.java), Text(instance.getName())),
            H4(Icon(VaadinIcon.COG).also { it.style["margin-right"] = "0.5rem" }, Text(instance.getName())),
            output,
            input
        )
    }

}