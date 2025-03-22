package minerslab.mcsp.component

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.VerticalLayout

@Tag("mcsp-breadcrumb")
class Breadcrumb(vararg components: Component, val icon: Boolean = true) : VerticalLayout() {

    init {
        components
            .flatMap {
                listOf(
                    it,
                    (if (icon) Icon(VaadinIcon.ARROW_RIGHT) else Span(">")).apply {
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