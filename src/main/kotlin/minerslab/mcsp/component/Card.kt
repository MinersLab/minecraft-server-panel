package minerslab.mcsp.component

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.card.CardVariant
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.shared.HasThemeVariant

@Tag("vaadin-card")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "^24.7")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/card", version = "^24.7")
@JsModule("@vaadin/card/src/vaadin-card.js")
class Card : Component(), HasSize, HasThemeVariant<CardVariant>, HasComponents {

    private fun getSlot(slot: String) = children.toList().first { it.element.hasAttribute("slot") && it.element.getAttribute("slot") == slot }
    private fun setSlot(slot: String, component: Component) {
        if (children.toList().any { it.element.hasAttribute("slot") && it.element.getAttribute("slot") == slot }) {
            remove(getSlot(slot))
        }
        add(component)
        component.element.setAttribute("slot", slot)
    }

    var title: Component
        get() = getSlot("title")
        set(value) = setSlot("title", value)

    var subtitle: Component
        get() = getSlot("subtitle")
        set(value) = setSlot("subtitle", value)

    var media: Component
        get() = getSlot("media")
        set(value) = setSlot("media", value)

    var headerPrefix: Component
        get() = getSlot("header-prefix")
        set(value) = setSlot("header-prefix", value)

    var footer : Component
        get() = getSlot("footer")
        set(value) = setSlot("footer", value)

    var headerSuffix: Component
        get() = getSlot("header-suffix")
        set(value) = setSlot("header-suffix", value)

    var header: Component
        get() = getSlot("header")
        set(value) = setSlot("header", value)

}