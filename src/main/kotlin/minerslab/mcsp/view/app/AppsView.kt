package minerslab.mcsp.view.app

import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.Route
import jakarta.annotation.security.RolesAllowed
import minerslab.mcsp.layout.MainLayout

@Route("/apps", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class AppsView : Div()