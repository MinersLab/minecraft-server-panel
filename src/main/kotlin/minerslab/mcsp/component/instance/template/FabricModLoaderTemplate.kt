package minerslab.mcsp.component.instance.template

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import minerslab.mcsp.entity.instance.Instance
import minerslab.mcsp.entity.instance.template.ModLoaderTemplate
import minerslab.mcsp.entity.instance.template.TemplateArgument
import minerslab.mcsp.util.getChildFile
import org.springframework.http.ContentDisposition
import org.springframework.stereotype.Component

@Component
class FabricModLoaderTemplate : ModLoaderTemplate<FabricModLoaderTemplate.Argument> {

    override fun createArgument() = Argument()


    inner class Argument : TemplateArgument {

        override fun getTemplate() = this@FabricModLoaderTemplate

        var gameVersion: GameVersion? = null
        var loaderVersion: LoaderVersion? = null
        var installerVersion: InstallerVersion? = null

        override fun createConfiguration(): VerticalLayout {
            val layout = VerticalLayout()
            layout.setWidthFull()
            layout.isPadding = false
            val gameVersion = Select<GameVersion>()
            val loaderVersion = Select<LoaderVersion>()
            val installerVersion = Select<InstallerVersion>()
            gameVersion.label = "游戏版本"
            gameVersion.addValueChangeListener { this.gameVersion = it.value }
            gameVersion.setItems(gameVersions)
            gameVersion.setItemLabelGenerator { it.version }
            gameVersion.value = gameVersions.first()
            loaderVersion.label = "加载器版本"
            loaderVersion.addValueChangeListener { this.loaderVersion = it.value }
            loaderVersion.setItems(loaderVersions)
            loaderVersion.setItemLabelGenerator { it.version }
            loaderVersion.value = loaderVersions.first()
            installerVersion.label = "安装器版本"
            installerVersion.addValueChangeListener { this.installerVersion = it.value }
            installerVersion.setItems(installerVersions)
            installerVersion.setItemLabelGenerator { it.version }
            installerVersion.value = installerVersions.first()

            layout.add(gameVersion, loaderVersion, installerVersion)
            return layout
        }

    }

    companion object {
        const val INSTALLER_VERSION_API = "https://meta.fabricmc.net/v2/versions/installer"
        const val LOADER_VERSION_API = "https://meta.fabricmc.net/v2/versions/loader"
        const val GAME_VERSION_API = "https://meta.fabricmc.net/v2/versions/game"
        const val INSTALLER_DOWNLOAD_API = "https://meta.fabricmc.net/v2/versions/loader/{game}/{loader}/{installer}/server/jar"
    }

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    @Serializable
    data class LoaderVersion(
        val separator: String,
        val build: Int,
        val maven: String,
        val version: String,
        val stable: Boolean
    )

    @Serializable
    data class InstallerVersion(
        val url: String,
        val maven: String,
        val version: String,
        val stable: Boolean
    )

    @Serializable
    data class GameVersion(
        val version: String,
        val stable: Boolean
    )

    suspend fun listLoaderVersions(): List<LoaderVersion> = httpClient.get(LOADER_VERSION_API).body()
    suspend fun listInstallerVersions(): List<InstallerVersion> = httpClient.get(INSTALLER_VERSION_API).body()
    suspend fun listGameVersions(): List<GameVersion> = httpClient.get(GAME_VERSION_API).body()

    val loaderVersions by lazy { runBlocking { listLoaderVersions() } }
    val installerVersions by lazy { runBlocking { listInstallerVersions() } }
    val gameVersions by lazy { runBlocking { listGameVersions() } }

    suspend fun getInstaller(gameVersion: String, loaderVersion: String, installerVersion: String) =
        httpClient.get(
            INSTALLER_DOWNLOAD_API
                .replace("{game}", gameVersion)
                .replace("{loader}", loaderVersion)
                .replace("{installer}", installerVersion)
        )

    override suspend fun applyTo(data: Argument, instance: Instance) {
        val installer = getInstaller(data.gameVersion!!.version, data.loaderVersion!!.version, data.installerVersion!!.version)
        val fileName = installer.headers["Content-Disposition"]?.let { ContentDisposition.parse(it).filename } ?: ModLoaderTemplate.SERVER_JAR
        installer.bodyAsChannel().copyTo(instance.path.toFile().getChildFile(fileName).writeChannel())
        instance.config = instance.config.apply {
            launchCommandLine = "java -jar $fileName"
        }
    }

    override fun getName() = "Fabric"

}