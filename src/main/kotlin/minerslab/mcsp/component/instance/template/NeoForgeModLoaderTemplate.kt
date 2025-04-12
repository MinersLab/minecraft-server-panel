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
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import minerslab.mcsp.entity.instance.Instance
import minerslab.mcsp.entity.instance.template.InstanceTemplate
import minerslab.mcsp.entity.instance.template.ModLoaderTemplate.Companion.SERVER_INSTALLER_JAR
import minerslab.mcsp.entity.instance.template.TemplateArgument
import minerslab.mcsp.util.getChildFile
import org.springframework.stereotype.Component
import java.util.*

@Component
class NeoForgeModLoaderTemplate : InstanceTemplate<NeoForgeModLoaderTemplate.Argument> {

    companion object {
        const val VERSION_API = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge"
        const val LEGACY_VERSION_API = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/forge"
        const val DOWNLOAD_API = "https://maven.neoforged.net/releases/net/neoforged/{package}/{version}/{package}-{version}-installer.jar"

        const val INSTALL_COMMAND = "java -jar {file} --install-server"

        fun neoVersionToVanilla(version: String): String {
            val split = version.split(".")
            return "1.${split[0]}.${split[1]}"
        }

        fun legacyVersionToVanilla(version: String) = version.split("-")[0]

    }

    inner class Argument : TemplateArgument {

        var version: Pair<String, Boolean>? = null

        override fun createConfiguration() = VerticalLayout().apply {
            val gameVersion = Select<String>()
            val loaderVersion = Select<Pair<String, Boolean>>()
            val gameVersions = mappedVersions.keys.sorted().reversed()
            gameVersion.setItems(gameVersions)
            gameVersion.addValueChangeListener {
                loaderVersion.setItems(mappedVersions[it.value])
                loaderVersion.value = mappedVersions[it.value]?.first()
            }
            loaderVersion.setItemLabelGenerator { it.first }
            loaderVersion.addValueChangeListener { version = it.value }
            gameVersion.label = "游戏版本"
            loaderVersion.label = "加载器版本"
            gameVersion.value = gameVersions.first()
            add(gameVersion, loaderVersion)
        }

        override fun getTemplate() = this@NeoForgeModLoaderTemplate

    }

    @Serializable
    data class Versions(val isSnapshot: Boolean, val versions: List<String>)

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    override fun getName() = "NeoForge"
    override fun createArgument() = Argument()

    suspend fun listNeoVersions() = httpClient.get(VERSION_API).body<Versions>()
    suspend fun listLegacyVersions() = httpClient.get(LEGACY_VERSION_API).body<Versions>()

    suspend fun fetchInstaller(version: Pair<String, Boolean>): HttpResponse {
        val packageName = if (version.second) "forge" else "neoforge"
        val versionName = version.first
        val url = DOWNLOAD_API
            .replace("{package}", packageName)
            .replace("{version}", versionName)
        return httpClient.get(url)
    }

    // Pair(版本名称，是否为旧版)
    val versions by lazy {
        runBlocking {
            listOf(
                async { listNeoVersions().versions.map { Pair(it, false) } }, async { listLegacyVersions().versions.filter { it.contains('-') }.map { Pair(it, true) } }
            ).awaitAll().flatten()
        }
    }

    val mappedVersions by lazy {
        versions
            .groupBy { if (it.second) legacyVersionToVanilla(it.first) else neoVersionToVanilla(it.first) }
            .mapKeys { it.key.removeSuffix(".0") }
    }

    override suspend fun applyTo(data: Argument, instance: Instance) {
        val file = instance.path.toFile().getChildFile(SERVER_INSTALLER_JAR)
        val response = fetchInstaller(data.version ?: return)
        response.bodyAsChannel().copyTo(file.writeChannel())
        val tokenizer = StringTokenizer(INSTALL_COMMAND.replace("{file}", SERVER_INSTALLER_JAR))
        val command = Array(tokenizer.countTokens()) { tokenizer.nextToken() }
        val processBuilder = ProcessBuilder(command.toList())
            .redirectOutput(ProcessBuilder.Redirect.appendTo(instance.log))
            .redirectError(ProcessBuilder.Redirect.appendTo(instance.log))
            .directory(instance.path.toFile())
        withContext(Dispatchers.IO) {
            val process = processBuilder.start()
            instance.log("[MCSP] 安装程序已启动！\n")
            process.onExit().handle { _, _ ->
                instance.log("[MCSP] 安装程序已退出(${process.exitValue()})！\n")
            }
        }
        instance.config = instance.config.copy(launchCommandLine = "./run")
    }

}