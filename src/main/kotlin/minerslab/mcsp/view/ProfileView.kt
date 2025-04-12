package minerslab.mcsp.view

import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.security.McspAuthenticationContext

@Route("/profile", layout = MainLayout::class)
@PermitAll
class ProfileView(authContext: McspAuthenticationContext) : VerticalLayout() {
    init {
        style["padding"] = "2rem"
        add(
            H2("当前用户"),
            Text(authContext.userName),
            H2("权限组"),
            HorizontalLayout().apply {
                authContext.roles.forEach {
                    add(Span(it.name).apply { element.themeList.add("badge") })
                }
            },
            Button("退出登录").apply {
                addClickListener { authContext.logout() }
                addThemeVariants(ButtonVariant.LUMO_ERROR)
            },
        )
    }
}
