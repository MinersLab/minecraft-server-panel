package minerslab.mcsp.layout

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.router.HasDynamicTitle
import com.vaadin.flow.router.Layout
import com.vaadin.flow.spring.security.AuthenticationContext

@Layout
class MainLayout(
    authContext: AuthenticationContext,
) : AppLayout(),
    HasDynamicTitle {
    override fun getPageTitle() = "Minecraft Server Panel"

    init {
        val title =
            H1("Minecraft Server Panel").apply {
                style["font-size"] = "var(--lumo-font-size-l)"
                style["margin"] = "var(--lumo-space-m)"
                addClickListener { UI.getCurrent().navigate("/") }
            }
        val myAppsButton =
            Button("我的应用").apply {
                isVisible = authContext.hasAnyRole("OWNER", "ADMIN")
                style["margin-right"] = "var(--lumo-space-m)"
                addClickListener {
                    UI.getCurrent().navigate("/apps")
                }
            }
        val profileButton =
            Button(Icon("lumo:user")).apply {
                style["margin-right"] = "var(--lumo-space-m)"
                isVisible = authContext.hasAnyRole("OWNER", "ADMIN", "USER")
                addClickListener { UI.getCurrent().navigate("/profile") }
            }
        addToNavbar(title, myAppsButton, profileButton)
    }
}
