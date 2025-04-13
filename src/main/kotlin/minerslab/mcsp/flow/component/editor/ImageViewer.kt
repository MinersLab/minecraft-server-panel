package minerslab.mcsp.flow.component.editor

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Image
import java.io.File

@Tag("mcsp-image-viewer")
class ImageViewer(
    private val fileUrl: String,
) : Div(),
    Editor {
    private var isLoaded = false

    override fun isLoaded() = isLoaded

    override fun getName() = "图片"

    override fun loadFrom(file: File) {
        val image = Image()
        image.src = fileUrl
        image.setAlt(file.name)
        add(image)
        isLoaded = true
    }

    override fun saveTo(file: File) = null
}
