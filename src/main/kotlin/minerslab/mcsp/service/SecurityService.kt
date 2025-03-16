package minerslab.mcsp.service

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinServletRequest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.stereotype.Component

@Component
class SecurityService {

    val authenticatedUser: UserDetails?
        get() {
            val context: SecurityContext = SecurityContextHolder.getContext()
            val principal = context.authentication.principal
            if (principal is UserDetails) return principal
            return null
        }

    fun logout() {
        UI.getCurrent().page.setLocation(LOGOUT_SUCCESS_URL)
        val logoutHandler = SecurityContextLogoutHandler()
        logoutHandler.logout(
            VaadinServletRequest.getCurrent().httpServletRequest, null,
            null
        )
    }

    companion object {
        private const val LOGOUT_SUCCESS_URL = "/"
    }

}