package minerslab.mcsp.service

import minerslab.mcsp.app.instance.Instance
import minerslab.mcsp.repository.InstanceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import java.util.*

@Component
class InstanceService(private val instanceRepository: InstanceRepository) : DisposableBean {

    companion object {
        val log: Logger = LoggerFactory.getLogger(InstanceService::class.java)
    }

    override fun destroy() {
        instanceRepository.findAll().forEach {
            stop(it)
            log.info("Disposed: ${it.getName()}")
        }
    }

    private val processPool = mutableMapOf<UUID, Process>()

    fun get(instance: Instance) = processPool[instance.id]

    fun run(instance: Instance): Process {
        instance.config = instance.config.copy(lastLaunchTime = System.currentTimeMillis())
        val tokenizer = StringTokenizer(instance.config.launchCommandLine)
        val command = Array(tokenizer.countTokens()) { tokenizer.nextToken() }
        val process = ProcessBuilder(command.toList())
            .directory(instance.path.toFile())
            .redirectOutput(ProcessBuilder.Redirect.appendTo(instance.log))
            .redirectError(ProcessBuilder.Redirect.appendTo(instance.log))
            .start()
        processPool[instance.id] = process
        return process
    }

    fun stop(instance: Instance, force: Boolean = false) {
        if (force || instance.config.stopCommand == "<mcsp:force-stop>") {
            processPool[instance.id]?.destroyForcibly()
            processPool.remove(instance.id)
        } else if (instance.config.stopCommand == "<mcsp:stop>") {
            processPool[instance.id]?.destroy()
            processPool.remove(instance.id)
        } else {
            processPool[instance.id]?.outputStream?.write((instance.config.stopCommand + "\n").toByteArray())
            processPool[instance.id]?.outputStream?.flush()
            processPool[instance.id]?.onExit()?.handle { _, _ -> processPool.remove(instance.id) }
        }
    }

}
