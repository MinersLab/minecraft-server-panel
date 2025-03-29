package minerslab.mcsp.controller

import com.vaadin.flow.spring.security.AuthenticationContext
import minerslab.mcsp.service.VisualDataService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VisualDataController(
    private val visualDataService: VisualDataService,
) {
    @RequestMapping("/api/visual-data/request-count", consumes = [MediaType.ALL_VALUE])
    fun getRequestCountData(authContext: AuthenticationContext): ResponseEntity<MutableList<Int>> =
        if (authContext.hasAnyRole("OWNER", "ADMIN")) {
            ResponseEntity.ok(visualDataService.getRequestCountData())
        } else {
            ResponseEntity.status(403).build()
        }
}
