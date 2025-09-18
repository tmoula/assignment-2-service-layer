plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "edu.trincoll"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
}

val isJacocoReportRequested =
    gradle.startParameter.taskNames.any {
        it.contains("jacocoTestReport", ignoreCase = true)
    }

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
    if (isJacocoReportRequested) {
        // When generating JaCoCo report explicitly, allow tests to fail but still produce coverage
        ignoreFailures = true
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            includes = listOf("edu.trincoll.hr.service.*")
            excludes = listOf("*Application", "*Config", "*Exception")
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}