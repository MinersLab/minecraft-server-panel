package minerslab.mcsp.util

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.contextmenu.HasMenuItems
import com.vaadin.flow.component.contextmenu.MenuItem
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.shared.SlotUtils
import com.vaadin.flow.component.shared.Tooltip
import com.vaadin.flow.component.textfield.TextField
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

fun HasComponents.showDialog(builder: Dialog.() -> Unit) {
    val dialog = Dialog()
    add(dialog)
    dialog.addOpenedChangeListener {
        if (!it.isOpened) remove(dialog)
    }
    builder(dialog)
    dialog.open()
}

fun HasComponents.showConfirmDialog(
    title: String,
    text: String = "确定执行此操作?",
    callback: (accept: Boolean) -> Unit,
) = showDialog {
    headerTitle = title
    add(text)
    val footer =
        row(
            Button("关闭").apply {
                addClickListener {
                    close()
                    callback(false)
                }
            },
            Button("确定").apply {
                addThemeVariants(ButtonVariant.LUMO_WARNING)
                addClickListener {
                    close()
                    callback(true)
                }
            },
        )
    this.footer.add(footer)
}

fun HasComponents.showPromptDialog(
    title: String,
    prompt: String,
    defaultValue: String? = null,
    validator: (oldValue: String, newValue: String) -> Boolean = { _, _ -> true },
    builder: Dialog.(textField: TextField) -> Unit = {},
    callback: (value: String) -> Unit,
) = showDialog {
    val input =
        TextField()
            .apply {
                placeholder = prompt
                setWidthFull()
            }.also { add(it) }
    input.setManualValidation(true)
    if (defaultValue != null) input.value = defaultValue
    val oldValue = input.value
    input.addValueChangeListener {
        if (!validator(oldValue, it.value)) {
            input.isInvalid = true
            input.errorMessage = "无效输入"
        } else {
            input.isInvalid = false
        }
    }
    builder(input)
    footer.apply {
        Button("确认")
            .apply {
                addClickListener {
                    val newValue = input.value
                    if (validator(oldValue, newValue)) {
                        callback(newValue)
                        close()
                    }
                }
            }.also { add(it) }
        Button("取消")
            .apply {
                addClickListener { close() }
            }.also { add(it) }
    }
    headerTitle = title
}

fun HasMenuItems.createIconItem(
    icon: Icon,
    label: String? = null,
    ariaLabel: String? = null,
    tooltip: String? = null,
    isChild: Boolean = false,
): MenuItem {
    if (isChild) {
        icon.style["width"] = "var(--lumo-icon-size-s)"
        icon.style["height"] = "var(--lumo-icon-size-s)"
        icon.style["marginRight"] = "var(--lumo-space-s)"
    }

    val item = addItem(icon) {}

    if (ariaLabel != null) item.setAriaLabel(ariaLabel)
    if (label != null) item.add(Text(label))
    if (tooltip != null) {
        Tooltip.forComponent(item).apply {
            text = tooltip
            hoverDelay = 0
        }
    }

    return item
}
