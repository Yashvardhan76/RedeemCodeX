plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
//    kotlin("kapt") version "2.0.21"
}

group = "me.justlime"
version = "1.0-EAP"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Room dependencies
//    implementation("androidx.room:room-runtime:2.5.0")  // Room runtime
//    kapt("androidx.room:room-compiler:2.5.0")           // Room compiler for annotation processing

    // SQLite JDBC driver
    implementation("org.xerial:sqlite-jdbc:3.42.0.1")

    // Coroutines for asynchronous Room operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
}


val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
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
    from("$buildDir/libs/RedeemX-${project.version}-all.jar")  // Source jar file
    into("E:/Minecraft/servers/Plugin-Maker/plugins")      // Destination folder
}

// Combined task to build and copy
tasks.register("buildAndCopy") {
    dependsOn("build", "copyToServerPlugins")
}
