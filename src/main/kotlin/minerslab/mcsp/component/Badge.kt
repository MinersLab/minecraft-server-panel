package minerslab.mcsp.component

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.html.Span

@Tag("mcsp-badge")
class Badge : Span {
    constructor(vararg components: Component, classes: String = "") : super(*components) {
        element.themeList.add("badge $classes")
    }

    constructor(text: String, classes: String = "") : super(text) {
        element.themeList.add("badge $classes")
    }
}
