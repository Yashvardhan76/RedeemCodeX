plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
//    kotlin("kapt") version "2.0.21"
}

group = "me.justlime"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven (
        url = "https://repo.extendedclip.com/releases/"){
        name = "extendedclip"
    }
}

dependencies {
    // Minecraft Spigot API for plugin development
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.11.6")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-bukkit:1.7.3") TODO

    // Kotlin Standard Library for JDK 8 features
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") TODO
    // HikariCP for efficient JDBC connection pooling
    implementation("com.zaxxer:HikariCP:4.0.3")

    // SQLite JDBC Driver
    compileOnly("org.xerial:sqlite-jdbc:3.47.0.0")

    // Gson for JSON serialization and deserialization
    compileOnly("com.google.code.gson:gson:2.10.1")
    implementation(kotlin("reflect"))

}

val targetJavaVersion = 17
kotlin {
    jvmToolchain(targetJavaVersion)
}
tasks.shadowJar {
    minimize()
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
// Task to copy the jar to the server plugins folder
tasks.register<Copy>("copyToServerPlugins") {
    dependsOn("shadowJar")  // Ensure shadowJar completes before copying
    from(layout.buildDirectory.dir("libs/RedeemX-${project.version}-all.jar"))  // Use layout.buildDirectory
    into("E:/Minecraft/servers/Plugin-Maker/plugins")  // Destination folder
}

// Combined task to build and copy
tasks.register("buildAndCopy") {
    dependsOn("build", "copyToServerPlugins")
}
