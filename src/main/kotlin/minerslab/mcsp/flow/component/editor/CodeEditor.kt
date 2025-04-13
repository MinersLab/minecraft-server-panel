package minerslab.mcsp.flow.component.editor

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.react.ReactAdapterComponent
import java.io.File

@Tag("mcsp-code-editor")
@JsModule("./components/monaco/CodeEditor.tsx")
@NpmPackage("@monaco-editor/react", version = "4.7.0")
@NpmPackage("monaco-yaml", version = "5.3.1")
class CodeEditor :
    ReactAdapterComponent(),
    Editor {
    private var isLoaded = false

    override fun isLoaded() = isLoaded

    override fun getName() = "文本"

    override fun loadFrom(file: File) {
        setCode(file.readText())
        setFileName(file.name)
        isLoaded = true
    }

    private var code: String = ""

    init {
        addStateChangeListener("value", String::class.java) {
            code = it
        }
    }

    override fun saveTo(file: File): Boolean {
        file.writeText(getCode())
        return true
    }

    fun setCode(it: String) {
        code = it
        setState("value", code)
    }

    fun getCode(): String = code

    fun setFileName(fileName: String) {
        var processedFileName = fileName
        if (processedFileName.endsWith(".snbt") || processedFileName.endsWith(".properties")) processedFileName += ".toml"
        setState("fileName", processedFileName)
    }

    fun getFileName(): String = getState("fileName", String::class.java)

    fun setWidth(width: String) = setState("width", width)

    fun getWidth(): String = getState("width", String::class.java)

    fun setHeight(height: String) = setState("height", height)

    fun getHeight(): String = getState("height", String::class.java)
}
