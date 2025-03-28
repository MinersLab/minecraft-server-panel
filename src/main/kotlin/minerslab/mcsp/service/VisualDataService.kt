package minerslab.mcsp.service

import com.vaadin.flow.server.ServiceInitEvent
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.concurrent.TimeUnit


@Component
class VisualDataService : HandlerInterceptor {

    private var requestCount: Int = 0
    private val requestCountData = generateSequence { 0 }.take(20).toMutableList()

    fun getRequestCountData() = requestCountData

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        requestCount++
        return true
    }

    @Scheduled(fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    fun processRequestCount() {
        requestCountData.add(requestCount)
        requestCount = 0
        if (requestCountData.size > 20) requestCountData.removeFirst()
    }

    @EventListener
    fun handleVaadinRequest(event: ServiceInitEvent) {
        event.addRequestHandler { _, _, _ ->
            requestCount++
            false
        }
    }

}