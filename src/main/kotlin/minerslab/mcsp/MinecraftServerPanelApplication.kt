package minerslab.mcsp

import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import kotlinx.serialization.Serializable
import minerslab.mcsp.service.instance.InstanceEventService
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
@NpmPackage("eslint", version = "9.23.0", dev = true)
@NpmPackage("@eslint/js", version = "9.23.0", dev = true)
@NpmPackage("typescript-eslint", version = "8.28.0", dev = true)
class MinecraftServerPanelApplication :
    ApplicationRunner,
    AppShellConfigurator {
    @Serializable
    data class Config(
        val users: List<User> = listOf(),
    ) {
        @Serializable
        data class User(
            val name: String,
            val password: String,
            val roles: List<Role> = listOf(Role.USER),
        ) {
            @Serializable
            enum class Role { OWNER, ADMIN, USER }
        }
    }

    @Autowired
    private lateinit var instanceEventService: InstanceEventService
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var config: Config

    override fun run(args: ApplicationArguments) {
        for (user in config.users) {
            logger.info("User: ${user.roles} ${user.name}")
        }
        instanceEventService.start()
    }
}

fun main(args: Array<String>) {
    runApplication<MinecraftServerPanelApplication>(*args)
}
