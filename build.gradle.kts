plugins {
	kotlin("jvm") version "2.1.10"
	kotlin("plugin.spring") version "2.1.10"
	kotlin("plugin.serialization") version "2.1.10"
	id("org.springframework.boot") version "3.4.4-SNAPSHOT"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.vaadin") version "24.6.6"
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
	implementation("org.springframework.boot:spring-boot-starter-websocket")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.vaadin:vaadin-spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	api("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.8.0")
}

dependencyManagement {
	imports {
		mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
