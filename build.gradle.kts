plugins {
    kotlin("jvm") version "2.1.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.gradleup.shadow") version "8.3.5"
}

val versionString = "1.0.4"

group = "de.bypixeltv"
version = versionString

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.skriptlang.org/releases")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.3-R0.1-SNAPSHOT")

    bukkitLibrary("net.kyori:adventure-platform-bukkit:4.3.4")
    bukkitLibrary("net.kyori:adventure-text-minimessage:4.17.0")
    bukkitLibrary("net.axay:kspigot:1.21.0")

    compileOnly("com.github.SkriptLang:Skript:2.9.5")

    bukkitLibrary("redis.clients:jedis:5.2.0")

    implementation("com.github.technicallycoded:FoliaLib:main-SNAPSHOT")
}

sourceSets {
    getByName("main") {
        java {
            srcDir("src/main/java")
        }
        kotlin {
            srcDir("src/main/kotlin")
        }
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java)

    shadowJar {
        relocate("com.tcoded.folialib", "de.bypixeltv.skredis.lib.folialib")

        archiveBaseName.set("SkRedis")
        archiveVersion.set(version.toString())
        archiveClassifier.set("")
    }
}

bukkit {
    main = "de.bypixeltv.skredis.Main"

    version = versionString

    foliaSupported = true

    apiVersion = "1.13"

    authors = listOf("byPixelTV")

    website = "https://bypixeltv.de"

    description = "The modern way to use Redis with Skript."

    depend = listOf("Skript")

    prefix = "SkRedis"

    commands {
        register("skredis") {
            description = "Main command for SkRedis"
            permission = "skredis.admin"
            usage = "/skredis <info|version|reload|reloadredis>"
        }
    }
}

kotlin {
    jvmToolchain(21)
}