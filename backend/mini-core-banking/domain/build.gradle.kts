dependencies {
    implementation(project(":common"))

    // @Transactional, 트랜잭션 경계 선언용 — spring-tx 만 (JPA/Hibernate 없이 순수 트랜잭션 추상화)
    implementation("org.springframework:spring-tx")

    // QueryDSL은 Phase 5에서 추가
}

tasks.named<Jar>("jar") {
    enabled = true
}
