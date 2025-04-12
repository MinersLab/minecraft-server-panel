package minerslab.mcsp.entity.instance.template

import com.vaadin.flow.component.Component

interface TemplateArgument {
    fun getTemplate(): InstanceTemplate<*>

    fun createConfiguration(): Component
}
