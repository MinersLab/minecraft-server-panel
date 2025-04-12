package minerslab.mcsp.entity.instance.template

import com.vaadin.flow.component.html.Div
import minerslab.mcsp.entity.instance.Instance
import org.springframework.stereotype.Component

@Component
class EmptyTemplate : InstanceTemplate<EmptyTemplate.EmptyArgument> {
    inner class EmptyArgument : TemplateArgument {
        override fun createConfiguration() = Div("无内容")

        override fun getTemplate() = this@EmptyTemplate
    }

    override fun createArgument() = EmptyArgument()

    override suspend fun applyTo(
        data: EmptyArgument,
        instance: Instance,
    ) {}

    override fun getName() = "空"
}
