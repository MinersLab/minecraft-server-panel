package minerslab.mcsp.security

import arrow.core.None
import arrow.core.Some
import com.vaadin.flow.router.AccessDeniedException
import com.vaadin.flow.spring.security.AuthenticationContext
import minerslab.mcsp.entity.user.Role
import org.springframework.security.web.authentication.session.SessionAuthenticationException
import org.springframework.stereotype.Component

@Component
class McspAuthenticationContext(val authenticationContext: AuthenticationContext) {

    fun isAuthenticated() = authenticationContext.isAuthenticated && authenticationContext.principalName.isPresent
    fun isAccess(role: Role): Boolean = role.hasPermission()
    fun <T> withAuthenticated(role: Role? = null, block: McspAuthenticationContext.() -> T) =
        if (isAuthenticated() && (role == null || isAccess(role))) Some(block())
        else None

    fun checkAccess(role: Role? = null, users: Collection<String>? = null) = apply {
        if (!isAuthenticated())
            throw SessionAuthenticationException("Not authenticated")
        if (role != null && !isAccess(role))
            throw AccessDeniedException()
        if (users != null && !users.containsAll(listOf(userName)))
            throw AccessDeniedException()
    }

    val userName: String
        get() = authenticationContext.principalName.get()

    val roles: List<Role>
        get() = authenticationContext.grantedRoles.mapNotNull { runCatching { Role.of(it) }.getOrNull() }

    fun logout() = authenticationContext.logout()

}