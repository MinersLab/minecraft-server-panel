package minerslab.mcsp.view

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.PermitAll
import minerslab.mcsp.layout.MainLayout

@Route("/profile", layout = MainLayout::class)
@PermitAll
class ProfileView(
    authContext: AuthenticationContext,
) : VerticalLayout() {
    init {
        style["padding"] = "2rem"
        add(
            H2("当前用户"),
            Text(authContext.principalName.get()),
            H2("权限组"),
            HorizontalLayout().apply {
                authContext.grantedRoles.forEach {
                    add(Span(it).apply { element.themeList.add("badge") })
                }
            },
            Button("退出登录").apply {
                addClickListener { authContext.logout() }
                addThemeVariants(ButtonVariant.LUMO_ERROR)
            },
        )
    }
}
