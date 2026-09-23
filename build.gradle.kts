plugins {
	`java-library`
}

group = "br.gov.crateus.bcm"
version = "0.1.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

val springBootBom = "org.springframework.boot:spring-boot-dependencies:3.5.3"
val springdoc = "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9"

dependencies {
	api(project(":bcm-sdk-api"))

	// Fornecidos pelo host (Dev Host / BCM) em runtime
	compileOnly(platform(springBootBom))
	compileOnly("org.springframework.boot:spring-boot-starter-web")
	compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
	compileOnly("org.springframework.boot:spring-boot-starter-security")
	compileOnly("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	compileOnly("org.springframework.boot:spring-boot-starter-validation")
	compileOnly(springdoc)

	testImplementation(platform(springBootBom))
	testImplementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
	testImplementation("org.springframework.boot:spring-boot-starter-security")
	testImplementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	testImplementation("org.springframework.boot:spring-boot-starter-validation")
	testImplementation(springdoc)
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
