package minerslab.mcsp.component.instance.template

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.tabs.TabSheet
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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import minerslab.mcsp.entity.instance.Instance
import minerslab.mcsp.entity.instance.template.ModLoaderTemplate
import minerslab.mcsp.entity.instance.template.ModLoaderTemplate.Companion.SERVER_JAR
import minerslab.mcsp.entity.instance.template.TemplateArgument
import minerslab.mcsp.util.getChildFile

@org.springframework.stereotype.Component
class VanillaTemplate : ModLoaderTemplate<VanillaTemplate.Argument> {

    companion object {
        const val VERSION_API = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
    }

    inner class Argument : TemplateArgument {

        var version: VersionMetadata.Version? = null

        override fun getTemplate() = this@VanillaTemplate
        override fun createConfiguration(): Component {
            val tabSheet = TabSheet()
            for (type in VersionMetadata.Version.Type.entries) {
                val versions = mappedVersions[type]!!
                val select = Select<VersionMetadata.Version>()
                select.setItemLabelGenerator { it.id }
                select.setItems(versions)
                select.value = versions.first()
                select.addValueChangeListener { version = it.value }
                val button = Button("选择")
                button.addClickListener { version = select.value }
                tabSheet.add(type.typeName, HorizontalLayout(select, button))
            }
            return tabSheet
        }

    }

    @Serializable
    data class VersionMetadata (val latest: Latest, val versions: List<Version>) {

        @Serializable
        data class Latest(val release: String, val snapshot: String)

        @Serializable
        data class Version(val id: String, val type: Type, val url: String, val time: String, val releaseTime: String) {
            @Serializable
            enum class Type(@Transient val typeName: String) {
                @SerialName("release") RELEASE("正式版"),
                @SerialName("snapshot") SNAPSHOT("快照版"),
                @SerialName("old_alpha") OLD_ALPHA("远古版 A"),
                @SerialName("old_beta") OLD_BETA("远古版 B");
            }
        }

    }

    @Serializable
    data class VersionManifest(val downloads: Map<String, Download>) {

        @Serializable
        data class Download(val url: String)

    }

    suspend fun fetchVersionMetadata() = httpClient.get(VERSION_API).body<VersionMetadata>()
    val versionMetadata by lazy { runBlocking { fetchVersionMetadata() } }
    val mappedVersions by lazy {
        versionMetadata.versions.groupBy { it.type }
    }

    suspend fun fetchVersionUrl(version: VersionMetadata.Version)
        = httpClient.get(version.url).body<VersionManifest>().downloads["server"]?.url

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override fun getName() = "原版"

    override suspend fun applyTo(data: Argument, instance: Instance) {
        val versionUrl = fetchVersionUrl(data.version ?: return) ?: return
        val file = instance.path.toFile().getChildFile(SERVER_JAR)
        httpClient.get(versionUrl).bodyAsChannel().copyTo(file.writeChannel())
        instance.config = instance.config.apply {
            launchCommandLine = "java -jar $SERVER_JAR"
        }
    }

    override fun createArgument() = Argument()

}