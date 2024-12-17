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
    // Minecraft Spigot API for plugin development
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")

    // Kotlin Standard Library for JDK 8 features
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // HikariCP for efficient JDBC connection pooling
    implementation("com.zaxxer:HikariCP:4.0.3")

    // SQLite JDBC Driver
    compileOnly("org.xerial:sqlite-jdbc:3.47.0.0")

    // Java Discord API (JDA) for Discord integration
    compileOnly("net.dv8tion:JDA:5.2.1")

    // Gson for JSON serialization and deserialization
    compileOnly("com.google.code.gson:gson:2.10.1")
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
    from("$buildDir/libs/RedeemX-${project.version}-all.jar")  // Source jar file
    into("D:/yashv/server1.21/plugins")      // Destination folder
}
 
// Combined task to build and copy
tasks.register("buildAndCopy") {
    dependsOn("build", "copyToServerPlugins")
}
