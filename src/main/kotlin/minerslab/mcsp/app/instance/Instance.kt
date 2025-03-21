package minerslab.mcsp.app.instance

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import minerslab.mcsp.util.createIfNotExists
import java.io.File
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.name

class Instance(path: Path) {

    val id: UUID = UUID.fromString(path.name)
    val configFile: File = path.resolve(".mcsp.json").toFile().createIfNotExists {
        Json.encodeToString(InstanceConfig()).toByteArray()
    }
    var config: InstanceConfig
        get() = Json.decodeFromString<InstanceConfig>(configFile.readText())
        set(value) = configFile.writeText(Json.encodeToString(value))


    fun getName() = config.name

}