package minerslab.mcsp.entity.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val password: String,
    val roles: List<Role> = listOf(Role.USER),
)
