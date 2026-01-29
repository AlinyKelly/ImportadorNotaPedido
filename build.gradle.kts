plugins {
    id("java")
    kotlin("jvm") version "1.9.22" // Caso não queira utilizar kotlin, só remover/comentar este plugin
    id("com.gradleup.shadow") version "8.3.9"
}

group = "br.com.sankhya.ce"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repository.jboss.org/nexus/content/repositories/thirdparty-releases/")
    }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly("com.github.DevSankhya:snk-wrapper:1.1.5")
    compileOnly("org.wildfly:wildfly-spec-api:16.0.0.Final")

    // Tratar CSV
    implementation("org.apache.commons:commons-csv:1.10.0")
    // Status HTTP / Apoio as Servlets
    implementation("com.squareup.okhttp3:okhttp:3.9.0")
    // https://mvnrepository.com/artifact/com.squareup.okio/okio
    implementation("com.squareup.okio:okio:1.13.0")
    implementation("com.github.DevSankhya:utils-snk:1.0.10")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileKotlin {
    compilerOptions {
        // Target JDK 8
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

tasks.shadowJar {
    // Mantem uma versão do kotlin caso não exista nenhuma na base garantindo o funcionamento
    relocate("kotlin", "br.com.sankhya.ce.shadow.kotlin")

    // Exclui os arquivos de versão para evitar erro no dicionario("...class file version 53.0)..."
    exclude("META-INF/versions/**")
    exclude("**/module-info.class")
}

kotlin {
    jvmToolchain(8)
}