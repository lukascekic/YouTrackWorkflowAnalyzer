plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.youtrack.analyzer"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Koog AI Framework (includes OpenAI support)
    implementation("ai.koog:koog-agents:0.5.2")

    // Ktor for HTTP client and server
    implementation("io.ktor:ktor-server-core:3.0.1")
    implementation("io.ktor:ktor-server-netty:3.0.1")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
    implementation("io.ktor:ktor-client-core:3.0.1")
    implementation("io.ktor:ktor-client-cio:3.0.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
    implementation("io.ktor:ktor-client-logging:3.0.1")
    implementation("io.ktor:ktor-client-auth:3.0.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Configuration
    implementation("com.typesafe:config:1.4.3")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Redis and Lettuce
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.youtrack.analyzer.MainKt")
}
