package minerslab.mcsp.view

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.react.ReactAdapterComponent
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.annotation.security.PermitAll
import minerslab.mcsp.layout.MainLayout

@Tag("mcsp-home-request-counting-chart")
@JsModule("./components/view/home/RequestCountingChart.tsx")
@NpmPackage("echarts", version = "5.6.0")
@NpmPackage("echarts-for-react", version = "3.0.3")
class RequestCountingChart : ReactAdapterComponent()

@Route("/", layout = MainLayout::class)
@PermitAll
class HomeView(authContext: AuthenticationContext) : Div() {

    init {
        Div().apply {
            style["padding"] = "2rem"
            RequestCountingChart().apply {
                isVisible = authContext.hasAnyRole("OWNER", "ADMIN")
            }.also { add(it) }
        }.also { add(it) }
    }

}