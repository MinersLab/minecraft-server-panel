package minerslab.mcsp.view.app.create

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.details.Details
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.router.Route
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.router.RouterLayout
import jakarta.annotation.security.RolesAllowed
import kotlinx.coroutines.runBlocking
import minerslab.mcsp.entity.instance.template.EmptyTemplate
import minerslab.mcsp.entity.instance.template.InstanceTemplate
import minerslab.mcsp.entity.instance.template.TemplateArgument
import minerslab.mcsp.flow.component.Card
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.security.McspAuthenticationContext
import minerslab.mcsp.util.Reference
import minerslab.mcsp.util.mutableRef
import minerslab.mcsp.view.app.ManageView

@Route("app/new/", layout = MainLayout::class)
@RolesAllowed("ADMIN")
class CreateView(
    private val authContext: McspAuthenticationContext,
    private val templates: List<InstanceTemplate<*>>,
    private val instanceRepository: InstanceRepository,
    emptyTemplate: EmptyTemplate,
) : VerticalLayout(),
    RouterLayout {
    val name =
        TextField("实例名称").apply {
            placeholder = "默认名称"
            isRequired = true
            isRequiredIndicatorVisible = true
        }

    val templateRef = mutableRef<TemplateArgument>(emptyTemplate.createArgument())

    init {
        isPadding = true

        val confirm =
            Button("创建实例").apply {
                addClickListener { runBlocking { confirm() } }
                addThemeVariants(ButtonVariant.LUMO_WARNING)
            }
        add(name, Details("模板", createTemplateSelection(templateRef)), confirm)
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun confirm() {
        val instance = instanceRepository.addInstance()

        // 配置文件
        if (name.value.trim().isEmpty()) name.value = instance.getName()
        val config = instance.config
        config.users.add(authContext.userName)
        config.name = name.value
        config.createdAt = System.currentTimeMillis()
        instance.config = config

        // 应用模板
        val templateArgument = templateRef.getValue()
        val template = templateArgument.getTemplate() as InstanceTemplate<TemplateArgument>
        template.applyTo(templateRef.getValue(), instance)

        // 重定向
        UI.getCurrent().navigate(ManageView::class.java, RouteParameters("id", instance.id.toString()))
    }

    fun createTemplateSelection(ref: Reference<TemplateArgument>) =
        VerticalLayout().apply {
            setWidthFull()
            val section = Card().apply { setWidthFull() }
            val configurations = mutableMapOf<TemplateArgument, Component>()
            val select = Select<TemplateArgument>()
            select.addValueChangeListener {
                section.removeAll()
                section.add(configurations.getOrPut(it.value) { it.value.createConfiguration() })
                ref.setValue(it.value)
            }
            select.setItemLabelGenerator { it.getTemplate().getName() }
            val items = listOf(ref.getValue()) +
                    templates.map { it.createArgument() }
                        .filterNot { it is EmptyTemplate.EmptyArgument }
                        .sortedBy { it.getTemplate().getName() }.reversed()
            select.setItems(items)
            select.value = ref.getValue()
            add(select, section)
        }
}
