package minerslab.mcsp

import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableScheduling
@EnableWebMvc
@Theme("mcsp", variant = Lumo.DARK)
@CssImport("./styles/index.css")
class MinecraftServerPanelApplication : ApplicationRunner, AppShellConfigurator {

    @Serializable
    data class Config(val users: List<User> = listOf()) {
        @Serializable
        data class User(val name: String, val password: String, val roles: List<Role> = listOf(Role.USER)) {
            @Serializable
            enum class Role { OWNER, ADMIN, USER }
        }
    }

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var config: Config

    override fun run(args: ApplicationArguments) {
        for (user in config.users) {
            logger.info("User: ${user.roles} ${user.name}")
        }
    }

}

fun main(args: Array<String>) {
    runApplication<MinecraftServerPanelApplication>(*args)
}
