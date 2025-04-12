package minerslab.mcsp.view

import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dependency.NpmPackage
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.react.ReactAdapterComponent
import com.vaadin.flow.router.Route
import jakarta.annotation.security.PermitAll
import minerslab.mcsp.component.Card
import minerslab.mcsp.component.Interval
import minerslab.mcsp.entity.user.Role
import minerslab.mcsp.layout.MainLayout
import minerslab.mcsp.repository.InstanceRepository
import minerslab.mcsp.security.McspAuthenticationContext
import minerslab.mcsp.service.InstanceService
import minerslab.mcsp.util.FileSizeUtil
import minerslab.mcsp.util.alignRow
import minerslab.mcsp.util.row
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.system.JavaVersion
import oshi.SystemInfo
import java.net.InetAddress
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Tag("mcsp-home-request-counting-chart")
@JsModule("./components/view/home/RequestCountingChart.tsx")
@NpmPackage("echarts", version = "5.6.0")
@NpmPackage("echarts-for-react", version = "3.0.3")
class RequestCountingChart : ReactAdapterComponent()

@Route("/", layout = MainLayout::class)
@PermitAll
class HomeView(
    private val authContext: McspAuthenticationContext,
    private val instanceService: InstanceService,
    private val instanceRepository: InstanceRepository,
    @Value("\${spring.application.version}") private val version: String,
) : VerticalLayout() {
    private val systemInfo = SystemInfo()
    private val operatingSystem = systemInfo.operatingSystem
    private val layer = systemInfo.hardware
    private val cpu = layer.processor
    private val ram = layer.memory
    private val decimalFormat = DecimalFormat("0.00")

    init {
        style["padding"] = "2rem"
        setHeightFull()
        setWidthFull()
        add(alignRow(createSystemResourceCard(), createWelcomeCard(), createInstanceCard()))
        add(row(createRequestCountingCard().takeIf { authContext.isAccess(Role.ADMIN) }))
        add(createDataOverviewCard())
    }

    private fun createSystemResourceCard(): Card =
        createCard().apply {
            title = Div("系统资源")
            subtitle = Div("CPU, RAM")
            val content = Div("0%, 0%")
            add(
                content,
                Interval().timeout(2000).once(just = true) {
                    val cpuLoadPercent = cpu.getSystemCpuLoad(1000) * 100
                    val ramUsedPercent = (ram.total - ram.available).toDouble() / ram.total * 100
                    content.text = "${decimalFormat.format(cpuLoadPercent)}%, ${decimalFormat.format(ramUsedPercent)}%"
                },
            )
        }

    private fun createWelcomeCard() =
        createCard().apply {
            add(H1("欢迎使用 Minecraft Server Panel"))
        }

    private fun createInstanceCard() =
        createCard().apply {
            val instances = instanceRepository.findAll().map { instanceService.get(it)?.isAlive == true }
            title = Div("实例")
            subtitle = Div("正在运行实例 / 全部实例")
            add(Div("${instances.count { it }} / ${instances.size}"))
            setWidthFull()
        }

    private fun createRequestCountingCard() =
        Card().apply {
            setWidthFull()
            title = Div("接口请求")
            add(RequestCountingChart().apply { setWidthFull() })
        }

    private fun createDataOverviewCard(): HorizontalLayout {
        val overview = VerticalLayout()
        add(Interval().timeout(1000).once { dataOverview(overview) })
        return HorizontalLayout().apply {
            isSpacing = true
            setWidthFull()
            add(
                createCard().apply {
                    setWidthFull()
                    title = Div("数据概览")
                    add(overview)
                },
            )
        }
    }

    private fun dataOverview(overview: VerticalLayout) {
        overview.removeAll()
        val dataOverview =
            setOf(
                "Java 版本" to { JavaVersion.getJavaVersion().toString() },
                "面板版本" to { version },
                "进程用户名" to { System.getProperty("user.name") },
                "面板用户名" to { authContext.userName },
                "面板时间" to { LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) },
                "浏览器时间" to {
                    UI
                        .getCurrent()
                        .page
                        .executeJs("return new Date().toISOString()")
                        .toCompletableFuture()
                        .thenApply { it.asString() }
                },
                "内存" to {
                    FileSizeUtil.formatFileSize(ram.total - ram.available) + " / " + FileSizeUtil.formatFileSize(ram.total)
                },
                "负载" to {
                    cpu.getSystemLoadAverage(3).joinToString(" / ") { if (it < 0) "N/A" else decimalFormat.format(it) }
                },
                "主机名" to { InetAddress.getLocalHost().hostName },
                "面板内存占用" to { FileSizeUtil.formatFileSize(Runtime.getRuntime().totalMemory()) },
                "系统类型" to { operatingSystem.family },
                "系统版本" to { operatingSystem },
            ).chunked(4)
        for (chunk in dataOverview) {
            overview.add(createDataOverviewRow(chunk))
        }
    }

    private fun createDataOverviewRow(chunk: List<Pair<String, () -> Any>>): HorizontalLayout =
        HorizontalLayout().apply {
            isSpacing = true
            setWidthFull()
            for (item in chunk) {
                add(
                    VerticalLayout().apply {
                        val first = Div(item.first)
                        val second = Div()
                        val secondValue = item.second()
                        if (secondValue is CompletableFuture<*>) {
                            secondValue.thenAccept { second.text = it.toString() }
                        } else {
                            second.text = secondValue.toString()
                        }
                        add(first, second)
                    },
                )
            }
        }

    private fun createCard(): Card = Card()
}
