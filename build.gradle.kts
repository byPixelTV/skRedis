plugins {
    kotlin("jvm") version "1.9.24"
    id("io.papermc.paperweight.userdev") version "1.7.0"
    id("xyz.jpenilla.run-paper") version "1.1.0"
}

group = "de.bypixeltv"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.skriptlang.org/releases")
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    implementation("dev.jorel", "commandapi-bukkit-shade", "9.4.0")
    implementation("dev.jorel", "commandapi-bukkit-kotlin", "9.4.0")
    implementation("net.axay:kspigot:1.20.3")

    implementation("com.github.SkriptLang:Skript:2.9.0-beta1-pre")

    implementation("org.json:json:20240303")
    implementation("org.cryptomator:siv-mode:1.5.2")
    implementation("org.apache.commons:commons-pool2:2.12.0")
    implementation("redis.clients:jedis:4.2.3")
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
    assemble {
        dependsOn(reobfJar)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
        options.compilerArgs.add("-Xlint:deprecation")
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
}

kotlin {
    jvmToolchain(17)
}