package minerslab.mcsp.component

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.react.ReactAdapterComponent

@Tag("mcsp-chart")
@NpmPackage("echarts", version = "5.6.0")
@NpmPackage("echarts-for-react", version = "3.0.3")
@JsModule("./components/Chart.tsx")
class Chart : ReactAdapterComponent() {

    fun getOptions(): Map<*, *> = getState("options", Map::class.java)
    fun setOptions(options: Map<String, Any?>) = apply {
        setState("options", options)
    }

}
