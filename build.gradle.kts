plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    id("org.springframework.boot") version "3.4.5-SNAPSHOT"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vaadin") version "24.7.2"
    id("com.diffplug.spotless") version "7.0.3"
}

group = "minerslab.mcsp"
version = property("app.version").toString()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.vaadin:vaadin-card-flow:${property("vaadin.version")}")

    implementation("io.arrow-kt:arrow-core:2.1.1")
    implementation("io.arrow-kt:arrow-core-serialization:2.1.1")
    implementation("com.github.oshi:oshi-core:6.8.1")
    implementation("org.apache.commons:commons-compress:1.27.1")

    api("net.kyori:adventure-nbt:4.20.0")
    api("net.kyori:regionfile:1.0.1")

    api("io.ktor:ktor-server-core-jvm:3.1.2")
    api("io.ktor:ktor-client-content-negotiation:3.1.2")
    api("io.ktor:ktor-serialization-kotlinx-json:3.1.2")
    api("io.ktor:ktor-client-cio:3.1.2")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.8.1")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:${property("vaadin.version")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

tasks.withType<ProcessResources> {
    filesMatching("application.yml") {
        expand(
            mapOf(
                "project" to project,
                "rootProject" to rootProject,
                "gradle" to gradle
            )
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    format("misc") {
        target("*.gradle", "*.gradle.kts", ".gitattributes", ".gitignore")

        trimTrailingWhitespace()
        leadingTabsToSpaces()
        endWithNewline()
    }
    kotlin {
        ktlint()
        suppressLintsFor {
            step = "ktlint"
            shortCode = "standard:no-wildcard-imports"
        }
    }
}
