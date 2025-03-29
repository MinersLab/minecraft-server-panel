plugins {
	kotlin("jvm") version "2.1.10"
	kotlin("plugin.spring") version "2.1.10"
	kotlin("plugin.serialization") version "2.1.10"
	id("org.springframework.boot") version "3.4.4-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.vaadin") version "24.7.1"
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

	api("com.github.oshi:oshi-core:6.8.0")
	api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
	api("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.8.0")
	api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}

dependencyManagement {
	imports {
		mavenBom("com.vaadin:vaadin-bom:${property("vaadin.version")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
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
