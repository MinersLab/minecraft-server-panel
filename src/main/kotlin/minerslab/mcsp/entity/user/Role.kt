package minerslab.mcsp.entity.user

import kotlinx.serialization.Serializable
import minerslab.mcsp.security.McspAuthenticationContext

@Serializable
enum class Role {
    OWNER, ADMIN, USER;

    companion object {
        fun of(name: String) = Role.entries.first { it.name == name }
    }

    context(McspAuthenticationContext)
    fun hasPermission(): Boolean = roles.any {
        it.ordinal >= ordinal
    }

}