package minerslab.mcsp.flow.component.editor

import com.vaadin.flow.component.Component
import java.io.File

interface Editor {

    fun isLoaded(): Boolean
    fun getName(): String
    fun loadFrom(file: File)
    fun saveTo(file: File): Boolean

    fun asComponent() = this as Component

}