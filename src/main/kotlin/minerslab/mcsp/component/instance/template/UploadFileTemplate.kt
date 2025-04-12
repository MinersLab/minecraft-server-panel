package minerslab.mcsp.component.instance.template

import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.FileBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import minerslab.mcsp.entity.instance.Instance
import minerslab.mcsp.entity.instance.template.InstanceTemplate
import minerslab.mcsp.entity.instance.template.TemplateArgument
import minerslab.mcsp.util.UPLOAD_I18N
import minerslab.mcsp.util.extractTo
import minerslab.mcsp.util.getChildFile
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@Component
class UploadFileTemplate(
    @Qualifier("cachePath") internal val cachePath: Path
) : InstanceTemplate<UploadFileTemplate.Argument> {

    inner class Argument : TemplateArgument {

        var file: File? = null
        var uploaded: Boolean = false

        override fun getTemplate() = this@UploadFileTemplate
        override fun createConfiguration(): Upload {
            val buffer = FileBuffer()
            val upload = Upload(buffer)
            upload.i18n = UPLOAD_I18N
            upload.setAcceptedFileTypes("application/zip", ".zip")
            upload.addSucceededListener {
                val tempFile = cachePath.toFile().getChildFile(UUID.nameUUIDFromBytes(it.fileName.toByteArray()).toString())
                Files.copy(buffer.inputStream, tempFile.toPath())
                file = tempFile
                uploaded = true
            }
            return upload
        }

    }

    override fun getName() = "<上传文件>"

    override fun createArgument() = Argument()

    override suspend fun applyTo(data: Argument, instance: Instance) = withContext(Dispatchers.IO) {
        if (!data.uploaded || data.file == null) return@withContext
        val zipInputStream = ZipArchiveInputStream(data.file!!.inputStream())
        zipInputStream.extractTo(instance.path)
        zipInputStream.close()
        data.file!!.delete()
    }

}