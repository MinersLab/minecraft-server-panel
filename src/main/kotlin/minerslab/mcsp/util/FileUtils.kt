package minerslab.mcsp.util

import java.io.File
import java.io.IOException
import java.text.DecimalFormat



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
    val file = File(this, path).normalize()
    if (!file.startsWith(this)) throw IOException()
    else return file
}

object FileSizeUtil {

    private const val K_SIZE: Int = 1024
    private const val M_SIZE: Int = 1048576
    private const val G_SIZE: Int = 1073741824
    private const val B: String = " B"
    private const val K: String = " KiB"
    private const val M: String = " MiB"
    private const val G: String = " GiB"

    fun formatFileSize(fileSize: Long): String {
        if (fileSize == 0L) return "0$B"
        val decimalFormat = DecimalFormat("#.00")
        return if (fileSize < K_SIZE) {
            decimalFormat.format(fileSize.toDouble()) + B
        } else if (fileSize < M_SIZE) {
            decimalFormat.format(fileSize.toDouble() / K_SIZE) + K
        } else if (fileSize < G_SIZE) {
            decimalFormat.format(fileSize.toDouble() / M_SIZE) + M
        } else {
            decimalFormat.format(fileSize.toDouble() / G_SIZE) + G
        }
    }
}
