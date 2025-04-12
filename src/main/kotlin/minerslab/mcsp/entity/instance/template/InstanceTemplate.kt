package minerslab.mcsp.entity.instance.template

import minerslab.mcsp.entity.instance.Instance

interface InstanceTemplate<T : TemplateArgument> {
    suspend fun applyTo(
        data: T,
        instance: Instance,
    )

    fun getName(): String

    fun createArgument(): T
}
