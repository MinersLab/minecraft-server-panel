package minerslab.mcsp.component

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout

@Tag("mcsp-breadcrumb")
class Breadcrumb(vararg components: Component?, val icon: () -> Component = { Icon(VaadinIcon.ARROW_RIGHT) }) :
    VerticalLayout() {

    init {
        components
            .filterNotNull()
            .flatMap {
                listOf(
                    it,
                    icon().apply {
                        style["font-weight"] = "bold"
                        style["margin-left"] = "0.5rem"
                        style["margin-right"] = "0.5rem"
                    }
                )
            }
            .dropLast(1)
            .forEach { add(it) }
    }

}