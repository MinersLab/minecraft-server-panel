package minerslab.mcsp.component

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.VerticalLayout

@Tag("mcsp-breadcrumb")
class Breadcrumb(vararg components: Component) : VerticalLayout() {

    init {
        components
            .flatMap {
                listOf(
                    it,
                    Span(">").apply {
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