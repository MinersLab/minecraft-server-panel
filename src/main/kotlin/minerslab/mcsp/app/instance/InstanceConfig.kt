package minerslab.mcsp.app.instance

import kotlinx.serialization.Serializable

@Serializable
data class InstanceConfig(val name: String = "新建实例", val launchCommand: String = "", val users: List<String> = mutableListOf())