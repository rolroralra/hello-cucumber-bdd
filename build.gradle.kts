plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// Cucumber BOM 버전 상수
val cucumberVersion = "7.20.1"
val testcontainersVersion = "1.20.4"
// val restAssuredVersion = "5.4.0" // REST Assured는 Groovy 기반으로 Java 21 호환성 문제 있음

dependencies {
    // ── Spring Boot ─────────────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // ── Docker Compose Support (개발 편의) ────────────────────────
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // ── Database ─────────────────────────────────────────────────
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    // Spring Boot 4에서 Flyway 자동 설정이 별도 모듈로 분리됨
    implementation("org.springframework.boot:spring-boot-flyway")

    // ── Test: Spring Boot ─────────────────────────────────────────
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // ── Test: Cucumber BOM ────────────────────────────────────────
    testImplementation(platform("io.cucumber:cucumber-bom:$cucumberVersion"))
    testImplementation("io.cucumber:cucumber-java")
    testImplementation("io.cucumber:cucumber-spring")
    testImplementation("io.cucumber:cucumber-junit-platform-engine")

    // ── Test: JUnit Platform Suite ────────────────────────────────
    testImplementation("org.junit.platform:junit-platform-suite")

    // ── Test: Testcontainers ──────────────────────────────────────
    testImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    // ── Test: Jackson (JSON 파싱) ─────────────────────────────────
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    // 없음
}
