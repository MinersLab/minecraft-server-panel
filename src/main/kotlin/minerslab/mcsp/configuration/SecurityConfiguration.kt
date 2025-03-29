package minerslab.mcsp.configuration

import com.vaadin.flow.spring.security.VaadinWebSecurity
import minerslab.mcsp.MinecraftServerPanelApplication
import minerslab.mcsp.view.LoginView
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@EnableWebSecurity
@Configuration
class SecurityConfiguration(
    private val appConfig: MinecraftServerPanelApplication.Config,
) : VaadinWebSecurity() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.rememberMe {
            it.alwaysRemember(true)
        }

        http.authorizeHttpRequests { auth ->
            auth
                .requestMatchers(AntPathRequestMatcher("/public/**"))
                .permitAll()
        }

        super.configure(http)

        setLoginView(http, LoginView::class.java)
    }

    @Throws(Exception::class)
    public override fun configure(web: WebSecurity) {
        super.configure(web)
    }

    @Bean
    fun userDetailsService(): UserDetailsManager {
        val users =
            appConfig.users.map {
                User
                    .withUsername(it.name)
                    .roles(*it.roles.map(Enum<*>::name).toTypedArray())
                    .password("{noop}${it.password}")
                    .build()
            }
        return InMemoryUserDetailsManager(*users.toTypedArray())
    }
}
