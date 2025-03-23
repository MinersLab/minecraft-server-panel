package minerslab.mcsp.util

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.shared.SlotUtils
import kotlin.reflect.KProperty

class Slot(private val name: String? = null) {

    operator fun getValue(thisRef: HasElement, property: KProperty<*>): Component {
        return SlotUtils.getChildInSlot(thisRef, name ?: property.name)
    }

    operator fun setValue(thisRef: HasElement, property: KProperty<*>, value: Component) {
        SlotUtils.setSlot(thisRef, name ?: property.name, value)
    }

}

fun slot(name: String? = null) = Slot(name)
