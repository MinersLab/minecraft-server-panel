package minerslab.mcsp.service.instance

import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import minerslab.mcsp.app.instance.Instance
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.service.InstanceService
import minerslab.mcsp.util.createIfNotExists
import org.springframework.beans.factory.BeanFactory
import org.springframework.stereotype.Component

@Component
class InstanceEventService(
    private val instanceRepository: InstanceRepository,
    private val beanFactory: BeanFactory,
) {
    private val instanceService by lazy { beanFactory.getBean(InstanceService::class.java) }

    @Serializable
    data class InstanceEventConfig(
        var autoRestart: Boolean = false,
        var autoStart: Boolean = false,
    )

    fun getConfigFile(instance: Instance) =
        instance.path.resolve(".mcsp.event.json").toFile().createIfNotExists {
            Json.encodeToString(InstanceEventConfig()).toByteArray()
        }

    @OptIn(ExperimentalSerializationApi::class)
    fun getConfig(instance: Instance) = Json.decodeFromStream<InstanceEventConfig>(getConfigFile(instance).inputStream())

    fun setConfig(
        instance: Instance,
        config: InstanceEventConfig,
    ) = getConfigFile(instance).writeBytes(Json.encodeToString(config).toByteArray())

    fun start() {
        instanceRepository.findAll().forEach {
            if (getConfig(it).autoStart) instanceService.run(it)
        }
    }

    suspend fun restart(instance: Instance) {
        if (getConfig(instance).autoRestart) {
            instanceService.setStatus(instance, InstanceService.InstanceStatus.RESTARTING)
            delay(5000)
            instanceService.run(instance)
        } else {
            instanceService.setStatus(instance, InstanceService.InstanceStatus.STOPPED)
        }
    }
}
