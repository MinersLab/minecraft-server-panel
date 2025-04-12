package minerslab.mcsp.flow.component

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.react.ReactAdapterComponent

@Tag("mcsp-util-interval")
@JsModule("./components/util/Interval.tsx")
class Interval : ReactAdapterComponent() {
    var timeout: Long
        get() = getState("timeout", Long::class.java)
        set(value) = setState("timeout", value)

    fun timeout() = timeout

    fun timeout(value: Long) = apply { timeout = value }

    fun once(
        just: Boolean = false,
        callback: () -> Unit,
    ) = apply {
        if (just) callback()
        addStateChangeListener("times", Long::class.java) {
            callback()
        }
    }
}
