package minerslab.mcsp.util

import java.io.File

fun File.createIfNotExists() = apply {
    if (!isFile) createNewFile()
}

fun File.createIfNotExists(content: () -> ByteArray) = apply {
    if (!isFile) {
        createNewFile()
        writeBytes(content())
    }
}