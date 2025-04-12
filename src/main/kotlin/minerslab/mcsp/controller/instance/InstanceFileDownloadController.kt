package minerslab.mcsp.controller.instance

import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.security.McspAuthenticationContext
import minerslab.mcsp.util.getChildFile
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

private typealias Role = minerslab.mcsp.entity.user.Role

@RestController
class InstanceFileDownloadController(private val instanceRepository: InstanceRepository) {

    context(McspAuthenticationContext)
    @GetMapping("/api/instance/{id}/download/{name}")
    fun download(
        @PathVariable id: String,
        @PathVariable name: String,
        @RequestParam path: String
    ): ResponseEntity<InputStreamResource> = runCatching {
        if (!isAuthenticated()) return ResponseEntity.status(401).build()
        if (!Role.ADMIN.hasPermission()) return ResponseEntity.status(403).build()
        val instance = instanceRepository.findById(UUID.fromString(id))
        if (!instance.config.users.contains(userName)) return ResponseEntity.status(403).build()
        val file = instance.path.toFile().getChildFile("$path/$name")
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .headers { it.contentDisposition = ContentDisposition.attachment().filename(name).build() }
            .body(InputStreamResource(file.inputStream()))
    }.getOrElse { ResponseEntity.notFound().build() }

}
