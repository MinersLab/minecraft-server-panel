package minerslab.mcsp.view

import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.HasDynamicTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.util.LOGIN_I18N

@Route("login", layout = MainLayout::class)
@AnonymousAllowed
class LoginView :
    VerticalLayout(),
    HasDynamicTitle,
    BeforeEnterObserver {
    override fun getPageTitle() = "登录"

    private val login = LoginForm(LOGIN_I18N)

    init {
        addClassName("login-view")
        setSizeFull()

        justifyContentMode = JustifyContentMode.CENTER
        alignItems = FlexComponent.Alignment.CENTER

        login.isForgotPasswordButtonVisible = false
        login.action = "login"

        add(H1("Minecraft Server Panel"), login)
    }

    override fun beforeEnter(beforeEnterEvent: BeforeEnterEvent) {
        if (beforeEnterEvent.location.queryParameters.parameters
                .containsKey("error")
        ) {
            login.isError = true
        }
    }
}
