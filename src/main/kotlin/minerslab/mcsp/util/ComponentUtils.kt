package minerslab.mcsp.util

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.shared.SlotUtils
import kotlin.reflect.KProperty

class Slot(
    private val name: String? = null,
) {
    operator fun getValue(
        thisRef: HasElement,
        property: KProperty<*>,
    ): Component = SlotUtils.getChildInSlot(thisRef, name ?: property.name)

    operator fun setValue(
        thisRef: HasElement,
        property: KProperty<*>,
        value: Component,
    ) {
        SlotUtils.setSlot(thisRef, name ?: property.name, value)
    }
}

fun slot(name: String? = null) = Slot(name)

fun alignRow(
    left: Component,
    right: Component,
    spacing: Boolean = true,
    fullWidth: Boolean = true,
) = row(spacing = spacing, fullWidth = fullWidth).apply {
    addToStart(left)
    addToEnd(right)
}

fun alignRow(
    left: Component,
    middle: Component,
    right: Component,
    spacing: Boolean = true,
    fullWidth: Boolean = true,
) = row(spacing = spacing, fullWidth = fullWidth).apply {
    addToStart(left)
    addToMiddle(middle)
    addToEnd(right)
}

fun row(
    vararg components: Component?,
    spacing: Boolean = true,
    fullWidth: Boolean = true,
) = HorizontalLayout().apply {
    isSpacing = spacing
    if (fullWidth) setWidthFull()
    add(*components.filterNotNull().toTypedArray())
}

fun column(vararg components: Component?) = VerticalLayout().apply { add(*components.filterNotNull().toTypedArray()) }

fun createIcon(vaadinIcon: VaadinIcon): Icon {
    val icon = Icon(vaadinIcon)
    icon.style.set("padding", "var(--lumo-space-xs)")
    return icon
}

fun createSelectionCheckbox(labelText: String) =
    Select<Boolean>().apply {
        setItems(true, false)
        label = labelText
        setItemLabelGenerator { if (it) "是" else "否" }
        setItemEnabledProvider { true }
    }
