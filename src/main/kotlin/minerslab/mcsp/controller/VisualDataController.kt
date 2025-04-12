package minerslab.mcsp.controller

import arrow.core.getOrElse
import minerslab.mcsp.entity.user.Role
import minerslab.mcsp.security.McspAuthenticationContext
import minerslab.mcsp.service.VisualDataService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VisualDataController(
    private val visualDataService: VisualDataService,
    private val authContext: McspAuthenticationContext
) {

    @RequestMapping("/api/visual-data/request-count", consumes = [MediaType.ALL_VALUE])
    fun getRequestCountData() = authContext.withAuthenticated(role = Role.ADMIN) {
        ResponseEntity.ok(visualDataService.getRequestCountData())
    }.getOrElse { ResponseEntity.status(403).build() }

}
