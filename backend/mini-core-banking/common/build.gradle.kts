dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

// common is a pure library module: no executable bootJar needed.
tasks.named<Jar>("jar") {
    enabled = true
}
