package minerslab.mcsp.configuration

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import minerslab.mcsp.MinecraftServerPanelApplication
import minerslab.mcsp.util.createIfNotExists
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ResourceUtils
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString

@Configuration
class MainConfiguration {

    @Bean
    fun appPath(): Path = Path.of(System.getProperty("user.dir"), ".mcsp").apply {
        if (!isDirectory()) createDirectory()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Bean
    fun appFileConfig(): MinecraftServerPanelApplication.Config {
        val path = appPath()
        val file = Path.of(path.pathString, "mcsp.conf")
            .toFile()
            .createIfNotExists {
                ResourceUtils.getFile("classpath:config/mcsp.conf").readBytes()
            }
        return Hocon.decodeFromConfig(ConfigFactory.parseFile(file))
    }
    
}
