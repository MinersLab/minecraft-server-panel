package minerslab.mcsp.flow.component.editor

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.html.Div
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.TagStringIO
import net.kyori.regionfile.RegionFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

fun RegionFile.getChunks(): CompoundBinaryTag {
    val result = mutableMapOf<Int, MutableMap<Int, CompoundBinaryTag>>()
    for (x in 0..<32) {
        for (z in 0..<32) {
            if (has(x, z)) {
                val bytes = read(x, z)?.readAllBytes()
                if (bytes != null) {
                    val tag = BinaryTagIO.unlimitedReader().read(ByteArrayInputStream(bytes))
                    result.getOrPut(x, ::mutableMapOf)[z] = tag
                }
            }
        }
    }
    val tag = CompoundBinaryTag.builder()
    for ((x, current) in result) {
        val currentTag = CompoundBinaryTag.builder()
        for ((z, chunk) in current) {
            currentTag.put(z.toString(), chunk)
        }
        tag.put(x.toString(), currentTag.build())
    }
    return tag.build()
}

@Tag("mcsp-region-editor")
class RegionEditor :
    Div(),
    Editor {
    private var isLoaded = false

    override fun isLoaded() = isLoaded

    val codeEditor = CodeEditor()

    init {
        setHeightFull()
        setWidthFull()
        add(codeEditor)
    }

    override fun getName() = "Region"

    override fun loadFrom(file: File) {
        val region = RegionFile(file)
        val chunks = region.getChunks()
        region.close()
        val code =
            TagStringIO
                .builder()
                .indent(2)
                .build()
                .asString(chunks)
        codeEditor.setCode(code)
        isLoaded = true
    }

    override fun saveTo(file: File) =
        runCatching {
            val tag = TagStringIO.get().asCompound(codeEditor.getCode())
            val region = RegionFile(file)
            for (x in tag.keySet()) {
                val current = tag.getCompound(x)
                for (z in current.keySet()) {
                    val chunk = current.getCompound(z)
                    val writer = region.write(x.toInt(), z.toInt())
                    val stream = ByteArrayOutputStream()
                    BinaryTagIO.writer().write(chunk, stream)
                    writer!!.write(stream.toByteArray())
                    writer.close()
                }
            }
            region.close()
        }.isSuccess
}
