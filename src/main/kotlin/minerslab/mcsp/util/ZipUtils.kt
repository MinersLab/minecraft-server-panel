package minerslab.mcsp.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import java.nio.file.Files
import java.nio.file.Path

suspend fun ZipArchiveInputStream.extractTo(to: Path) =
    withContext(Dispatchers.IO) {
        var entry = nextEntry
        while (entry != null) {
            val file = to.toFile().getChildFile(entry.name)
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile.mkdirs()
                Files.copy(this@extractTo, file.toPath())
            }
            entry = nextEntry
        }
    }
