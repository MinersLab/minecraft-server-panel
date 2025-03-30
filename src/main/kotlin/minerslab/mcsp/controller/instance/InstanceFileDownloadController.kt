package minerslab.mcsp.controller.instance

import com.vaadin.flow.spring.security.AuthenticationContext
import jakarta.servlet.http.HttpServletResponse
import minerslab.mcsp.MinecraftServerPanelApplication
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.util.getChildFile
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.jvm.optionals.getOrNull

private typealias Role = MinecraftServerPanelApplication.Config.User.Role

@RestController
class InstanceFileDownloadController(private val instanceRepository: InstanceRepository) {

    @GetMapping("/api/instance/{id}/download/{name}")
    fun download(
        @PathVariable id: String,
        @PathVariable name: String,
        @RequestParam path: String,
        authContext: AuthenticationContext,
        response: HttpServletResponse
    ): ResponseEntity<InputStreamResource> = runCatching {
        if (!authContext.isAuthenticated) return ResponseEntity.status(401).build()
        if (!authContext.hasAnyRole(Role.ADMIN.name, Role.OWNER.name)) return ResponseEntity.status(403).build()
        val instance = instanceRepository.findById(UUID.fromString(id))
        if (!instance.config.users.contains(authContext.principalName.getOrNull())) return ResponseEntity.status(403)
            .build()
        val file = instance.path.toFile().getChildFile("$path/$name")
        response.contentType = MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE
        ResponseEntity.ok(InputStreamResource(file.inputStream()))
    }.getOrNull() ?: ResponseEntity.notFound().build()

}
