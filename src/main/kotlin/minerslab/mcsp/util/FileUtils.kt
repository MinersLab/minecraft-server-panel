package minerslab.mcsp.util

import java.io.File
import java.io.IOException

fun File.createIfNotExists() = apply {
    if (!isFile) createNewFile()
}

fun File.createIfNotExists(content: () -> ByteArray) = apply {
    if (!isFile) {
        createNewFile()
        writeBytes(content())
    }
}

fun File.getChildFile(path: String): File {
    val file = File(this, path)
    if (!file.startsWith(this)) throw IOException()
    else return file
}