package minerslab.mcsp.layout

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.router.HasDynamicTitle
import com.vaadin.flow.router.Layout
import minerslab.mcsp.entity.user.Role
import minerslab.mcsp.security.McspAuthenticationContext

@Layout
class MainLayout(authContext: McspAuthenticationContext): AppLayout(),
    HasDynamicTitle {
    override fun getPageTitle() = "Minecraft Server Panel"

    init {
        with(authContext) {
            val title =
                H1("Minecraft Server Panel").apply {
                    style["font-size"] = "var(--lumo-font-size-l)"
                    style["margin"] = "var(--lumo-space-m)"
                    addClickListener { UI.getCurrent().navigate("/") }
                }
            val myAppsButton =
                Button("我的应用").apply {
                    isVisible = Role.ADMIN.hasPermission()
                    style["margin-right"] = "var(--lumo-space-m)"
                    addClickListener {
                        UI.getCurrent().navigate("/apps")
                    }
                }
            val profileButton =
                Button(Icon("lumo:user")).apply {
                    style["margin-right"] = "var(--lumo-space-m)"
                    isVisible = isAuthenticated()
                    addClickListener { UI.getCurrent().navigate("/profile") }
                }
            addToNavbar(title, myAppsButton, profileButton)
        }
    }
}
