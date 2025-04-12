package minerslab.mcsp.entity.instance

import kotlinx.serialization.Serializable

@Serializable
data class InstanceConfig(
    var lastLaunchTime: Long? = null,
    var launchTimes: Int = 0,
    var createdAt: Long = System.currentTimeMillis(),
    var name: String = "新建实例",
    val users: MutableList<String> = mutableListOf(),
    var launchCommandLine: String = "",
    var stopCommand: String = "/stop",
    var inputCharset: String = "UTF-8",
    var outputCharset: String = "UTF-8",
    var coloredTerminal: Boolean = true,
    var headless: Boolean = true,
)
