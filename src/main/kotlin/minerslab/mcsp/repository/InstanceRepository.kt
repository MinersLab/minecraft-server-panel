package minerslab.mcsp.repository

import minerslab.mcsp.app.instance.Instance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.name

@Repository
class InstanceRepository {

    @Autowired
    lateinit var appPath: Path

    private fun path() = appPath.resolve("instances").also { it.toFile().mkdirs() }

    fun findAll(): List<Instance> = findAllPaths().map { Instance(it) }

    fun findAllPaths(): List<Path> = path().toFile().also { it.mkdirs() }.listFiles()?.mapNotNull {
        if (runCatching { UUID.fromString(it.name) }.getOrNull() != null && it.isDirectory) it.toPath() else null
    } ?: emptyList()

    fun findAllIds(): List<UUID> = findAllPaths().map { UUID.fromString(it.name) }

    fun findById(id: UUID): Instance {
        val file = path().resolve(id.toString()).toFile()
        if (!file.isDirectory) throw IllegalArgumentException("Instance not found")
        return Instance(file.toPath())
    }

    fun addInstance(): Instance {
        var uuid = UUID.randomUUID()
        while (path().resolve(uuid.toString()).exists()) uuid = UUID.randomUUID()
        return Instance(path().resolve(uuid.toString()).toFile().also { it.mkdirs() }.toPath())
    }

}