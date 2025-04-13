package minerslab.mcsp.flow.component.editor

import arrow.core.serialization.ArrowModule
import com.vaadin.flow.component.Focusable
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.binder.Binder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.util.*


@Tag("mcsp-nbt-editor")
class PropertiesEditor : Div(), Editor {

    private var isLoaded = false
    override fun isLoaded() = isLoaded
    override fun getName() = "Properties"

    private lateinit var properties: Properties
    private lateinit var items: List<Entry>
    private val json = Json { serializersModule = ArrowModule }

    @OptIn(ExperimentalSerializationApi::class)
    fun getSchema(file: File): Map<String, String>? {
        val resource = this::class.java.getResourceAsStream("/schema/properties/${file.name}.json") ?: return null
        return runCatching {
            json.decodeFromStream<Map<String, String>>(resource)
        }.getOrNull()
    }

    data class Entry(
        var key: String,
        var value: String,
        var schema: String?
    )

    private fun addCloseListener(textField: TextField, editor: com.vaadin.flow.component.grid.editor.Editor<*>) {
        textField.getElement().addEventListener("keydown") { editor.cancel() }
            .setFilter("event.code === 'Enter'")
    }

    override fun loadFrom(file: File) {
        properties = Properties()
        properties.load(file.inputStream())
        val grid = Grid(Entry::class.java, false)
        val schema = getSchema(file)
        val keyField = TextField()
        val valueField = TextField()
        grid.addColumn { it.key }.setHeader("键").setEditorComponent(keyField)
        grid.addColumn { it.value }.setHeader("值").setEditorComponent(valueField)
        val binder = Binder(Entry::class.java)
        val editor = grid.editor
        addCloseListener(keyField, editor)
        addCloseListener(valueField, editor)
        editor.binder = binder
        binder.forField(keyField).bind(Entry::key, Entry::value::set)
        binder.forField(valueField).bind(Entry::value, Entry::value::set)
        if (schema != null) {
            grid.addColumn { it.schema ?: "" }.setHeader("注释")
        }
        grid.addItemDoubleClickListener {
            editor.editItem(it.item)
            val editorComponent = it.column.editorComponent
            if (editorComponent is Focusable<*>) editorComponent.focus()
        }
        items = properties.entries.map {
            Entry(
                it.key.toString(),
                it.value.toString(),
                schema?.get(it.key)
            )
        }
        grid.setItems(items)
        add(grid)
    }

    override fun saveTo(file: File) = runCatching {
        for (entry in items) properties[entry.key] = entry.value
        properties.store(file.outputStream(), null)
    }.isSuccess

}
