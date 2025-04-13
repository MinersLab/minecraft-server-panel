package minerslab.mcsp.flow.component.editor

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.html.Div
import minerslab.mcsp.flow.component.editor.NbtEditor.Companion.COMPRESSION_TYPES
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

fun BinaryTagIO.Reader.readFromStream(stream: InputStream): Pair<CompoundBinaryTag, BinaryTagIO.Compression> {
    val bytes = stream.readAllBytes()
    return COMPRESSION_TYPES.firstNotNullOf {
        runCatching {
            read(ByteArrayInputStream(bytes), it) to it
        }.getOrNull()
    }
}

@Tag("mcsp-nbt-editor")
class NbtEditor :
    Div(),
    Editor {
    private var isLoaded = false

    override fun isLoaded() = isLoaded

    override fun getName() = "NBT"

    companion object {
        @JvmField
        val COMPRESSION_TYPES = arrayOf(BinaryTagIO.Compression.NONE, BinaryTagIO.Compression.GZIP, BinaryTagIO.Compression.ZLIB)
    }

    val codeEditor = CodeEditor()
    private lateinit var file: File
    private lateinit var compressedFileType: BinaryTagIO.Compression

    init {
        setHeightFull()
        setWidthFull()
        add(codeEditor)
    }

    override fun loadFrom(file: File) {
        val (nbt, type) = BinaryTagIO.unlimitedReader().readFromStream(file.inputStream())
        compressedFileType = type
        val code =
            TagStringIO
                .builder()
                .indent(2)
                .build()
                .asString(nbt)
        codeEditor.setCode(code)
        codeEditor.setFileName("${file.name}.snbt")
        isLoaded = true
    }

    override fun saveTo(file: File) =
        runCatching {
            val nbt = TagStringIO.get().asCompound(codeEditor.getCode())
            BinaryTagIO.writer().write(nbt, file.toPath())
        }.isSuccess
}
