dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))

    // JPA + PostgreSQL + Flyway (Phase 2 에서 활성화)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // 통합 테스트용 Testcontainers (Phase 2 이후)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")

    // Redis / Redisson 은 Phase 3 에서 활성화
    // implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // implementation("org.redisson:redisson-spring-boot-starter:3.33.0")
}

tasks.named<Jar>("jar") {
    enabled = true
}
