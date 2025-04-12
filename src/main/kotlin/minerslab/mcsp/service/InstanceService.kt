package minerslab.mcsp.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import minerslab.mcsp.entity.instance.Instance
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.service.instance.InstanceEventService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Component
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Component
class InstanceService(
    private val instanceRepository: InstanceRepository,
    private val beanFactory: BeanFactory
) : DisposableBean {

    private val instanceEventService by lazy { beanFactory.getBean(InstanceEventService::class.java) }

    enum class InstanceStatus(val statusName: String, val color: String, val isRunning: Boolean = false) {
        STOPPED("未运行", "contrast"),
        RUNNING("运行中", "success", true),
        STOPPING("关闭中", "error"),
        RESTARTING("重启中", "warning");
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(InstanceService::class.java)
    }

    override fun destroy(): Unit = runBlocking {
        instanceRepository.findAll().map {
            async {
                val name = it.getName()
                log.info("Disposing: $name")
                stop(it)
                log.info("Disposed: $name")
            }
        }.awaitAll()
    }

    private val processPool = mutableMapOf<UUID, Process>()
    private val instanceStatus = mutableMapOf<UUID, InstanceStatus>()

    fun getStatus(instance: Instance) = instanceStatus[instance.id] ?: InstanceStatus.STOPPED
    fun setStatus(instance: Instance, status: InstanceStatus) = instanceStatus.put(instance.id, status)
    fun get(instance: Instance) = processPool[instance.id]

    fun run(instance: Instance): Process {
        instance.config = instance.config.run {
            copy(lastLaunchTime = System.currentTimeMillis(), launchTimes = launchTimes + 1)
        }
        val tokenizer = StringTokenizer(instance.config.launchCommandLine)
        val command = Array(tokenizer.countTokens()) { tokenizer.nextToken() }
        val process = ProcessBuilder(command.toList())
            .directory(instance.path.toFile())
            .redirectOutput(ProcessBuilder.Redirect.appendTo(instance.log))
            .redirectError(ProcessBuilder.Redirect.appendTo(instance.log))
            .apply {
                var javaToolOptions = ""
                if (instance.config.coloredTerminal) {
                    environment()["TERM"] = "xterm-256color"
                    javaToolOptions += "-Dterminal.ansi=true -Dspring.output.ansi.enabled=always "
                }
                if (instance.config.headless) javaToolOptions += "-Djava.awt.headless=true "
                environment()["JAVA_TOOL_OPTIONS"] = javaToolOptions
            }
            .start()
        instanceStatus[instance.id] = InstanceStatus.RUNNING
        process.onExit().handle { _, _ ->
            log.info("Instance ${instance.getName()} @ ${process.pid()} exited")
            val status = instanceStatus[instance.id]?.isRunning == true
            instanceStatus[instance.id] = InstanceStatus.STOPPED
            if (status) runBlocking {
                instanceEventService.restart(instance)
            }
        }
        processPool[instance.id] = process
        return process
    }

    suspend fun restart(instance: Instance) {
        stop(instance, restart = true)
        instanceStatus[instance.id] = InstanceStatus.RESTARTING
        delay(500)
        run(instance)
    }

    suspend fun stop(instance: Instance, force: Boolean = false, restart: Boolean = false) =
        if (getStatus(instance) != InstanceStatus.RUNNING && getStatus(instance) != InstanceStatus.RESTARTING) Unit else
            suspendCoroutine { continuation ->
                instanceStatus[instance.id] = if (restart) InstanceStatus.RESTARTING else InstanceStatus.STOPPING
                if (force || instance.config.stopCommand == "<mcsp:force-stop>") {
                    processPool[instance.id]?.destroyForcibly()
                    processPool.remove(instance.id)
                    instanceStatus[instance.id] = InstanceStatus.STOPPED
                    continuation.resume(Unit)
                } else if (instance.config.stopCommand == "<mcsp:stop>") {
                    processPool[instance.id]?.destroy()
                    processPool.remove(instance.id)
                    instanceStatus[instance.id] = InstanceStatus.STOPPED
                    continuation.resume(Unit)
                } else {
                    processPool[instance.id]?.outputStream?.write((instance.config.stopCommand + "\n").toByteArray())
                    processPool[instance.id]?.outputStream?.flush()
                    processPool[instance.id]?.onExit()?.handle { _, _ ->
                        processPool.remove(instance.id)
                        instanceStatus[instance.id] = if (restart) InstanceStatus.RESTARTING else InstanceStatus.STOPPED
                        continuation.resume(Unit)
                    }
                }
            }

}
