plugins {
    java
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

allprojects {
    group = "com.banking"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            // Spring Boot 3.3.5 의 testcontainers BOM(1.19.8) 이 Docker 29.x 응답과
            // 충돌하는 문제가 있어 1.20.4 로 승격.
            mavenBom("org.testcontainers:testcontainers-bom:1.20.4")
        }
    }

    dependencies {
        "implementation"("org.springframework.boot:spring-boot-starter")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.junit.jupiter:junit-jupiter")
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        // Windows + Docker Desktop 에서 Testcontainers 가 기본 pipe('docker_engine')를
        // 찾지 못하는 문제 대응. desktop-linux 컨텍스트의 파이프를 명시적으로 지정.
        // 다른 환경에서는 DOCKER_HOST 를 외부에서 주입하면 override 됨.
        if (System.getProperty("os.name").lowercase().contains("windows")
            && System.getenv("DOCKER_HOST") == null) {
            environment("DOCKER_HOST", "npipe:////./pipe/dockerDesktopLinuxEngine")
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }
}
