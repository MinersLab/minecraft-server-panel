package minerslab.mcsp.app.instance

import kotlinx.serialization.Serializable

@Serializable
data class InstanceConfig(
    var name: String = "新建实例",
    var launchCommandLine: String = "",
    var stopCommand: String = "/stop",
    val users: MutableList<String> = mutableListOf(),
    var lastLaunchTime: Long? = null,
    var inputCharset: String = "UTF-8",
    var outputCharset: String = "UTF-8"
)