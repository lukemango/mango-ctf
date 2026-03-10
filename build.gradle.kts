plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "8.1.1"

    val indraVersion = "3.1.3"
    id("net.kyori.indra") version indraVersion
    id("net.kyori.indra.git") version indraVersion

    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1"
}

group = "com.lukemango"
version = "1.0.0".decorateVersion()
description = "A Capture The Flag plugin for Minecraft servers"

bukkitPluginYaml {
    main = "com.lukemango.ctf.CTFPlugin"
    apiVersion = "1.21"
    authors = listOf("lukemango")
}

repositories {
    mavenCentral()
    maven ("https://jitpack.io")
    maven ("https://repo.papermc.io/repository/maven-public/")
    maven ("https://oss.sonatype.org/content/groups/public/")
    maven ("https://repo.mattstudios.me/artifactory/public/") // TriumphGui
    maven ("https://raw.githubusercontent.com/TheBlackEntity/PlugMan/repository/") // PlugMan
}

dependencies {
    // Spigot
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    // Cloud
    implementation("org.incendo:cloud-paper:2.0.0-beta.8")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.8")
    implementation("org.incendo:cloud-annotations:2.0.0-rc.2")
}

indra {
    javaVersions().target(21)
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    jar {
        archiveClassifier.set("noshade")
    }
    shadowJar {
        archiveFileName.set("${project.name}.jar") //-${project.version}
        sequenceOf(
            "org.incendo",
            "io.papermc.lib",
            "io.leangen.geantyref",
        ).forEach {
            relocate(it, "com.lukemango.ctf.lib.$it")
        }
    }
    processResources {
        val tokens = mapOf(
            "project.version" to project.version
        )
        inputs.properties(tokens)
        filesMatching("**/*.yml") {
            // Some of our files are too large to use Groovy templating
            filter { string ->
                var result = string
                for ((key, value) in tokens) {
                    result = result.replace("\${$key}", value.toString())
                }
                result
            }
        }
    }
    compileJava {
        options.compilerArgs.add("-Xlint:-classfile,-processing")
    }
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7)
    ?: error("Could not determine commit hash")

fun String.decorateVersion(): String = if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this