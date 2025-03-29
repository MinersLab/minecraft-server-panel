package minerslab.mcsp.view

import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.HasDynamicTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import minerslab.mcsp.layout.MainLayout

@Route("login", layout = MainLayout::class)
@AnonymousAllowed
class LoginView :
    VerticalLayout(),
    HasDynamicTitle,
    BeforeEnterObserver {
    override fun getPageTitle() = "登录"

    private val login =
        LoginForm(
            LoginI18n.createDefault().apply {
                errorMessage.apply {
                    title = "登录失败"
                    username = "未输入用户名"
                    password = "未输入密码"
                    message = "请检查您是否输入了正确的用户名和密码，然后重试。"
                }
                form.apply {
                    title = "登录"
                    password = "密码"
                    username = "用户名"
                    submit = "提交"
                }
            },
        )

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
