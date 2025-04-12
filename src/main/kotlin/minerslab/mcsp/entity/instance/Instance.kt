package minerslab.mcsp.entity.instance

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import minerslab.mcsp.util.createIfNotExists
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*
import kotlin.io.path.name

class Instance(val path: Path) {

    val id: UUID = UUID.fromString(path.name)
    val configFile: File = path.resolve(".mcsp.json").toFile().createIfNotExists {
        Json.encodeToString(InstanceConfig()).toByteArray()
    }
    val log = path.resolve(".mcsp.log").toFile().createIfNotExists()
    var config: InstanceConfig
        get() = Json.decodeFromString<InstanceConfig>(configFile.readText())
        set(value) = configFile.writeText(Json.encodeToString(value))
    fun log(text: String) {
        val logStream = FileOutputStream(log, true)
        logStream.flush()
        logStream.write(text.toByteArray(Charset.forName(config.outputCharset)))
    }

    fun getName() = config.name

}