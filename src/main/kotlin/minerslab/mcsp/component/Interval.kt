package minerslab.mcsp.component

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.react.ReactAdapterComponent

@Tag("mcsp-util-interval")
@JsModule("./components/util/Interval.tsx")
class Interval : ReactAdapterComponent() {

    var timeout: Long
        get() = getState("timeout", Long::class.java)
        set(value) = setState("timeout", value)

    fun once(callback: () -> Unit) = apply {
        addStateChangeListener("times", Long::class.java) {
            callback()
        }
    }

}

