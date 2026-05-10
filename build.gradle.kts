import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("com.gradleup.shadow") version "9.4.1"
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
    kotlin("kapt") version "2.3.10"
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
        resources {
            srcDir("res")
        }
    }
}

group = "gecko10000.geckoanvils"
version = "0.1"

paper {
    name = "GeckoAnvils"
    main = "$group.$name"
    apiVersion = "1.19"
    bootstrapper = "$group.MendingRemovalBootstrap"
    bootstrapDependencies {
        register("GeckoLib")
    }
    serverDependencies {
        register("GeckoLib") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://redempt.dev/")
    maven("https://repo.nexomc.com/releases")
    maven("https://eldonexus.de/repository/maven-public/")
    mavenLocal()
}

dependencies {
    compileOnly(kotlin("stdlib", version = "2.3.10"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("gecko10000.geckolib:GeckoLib:1.1")
    compileOnly("net.strokkur.commands:annotations-paper:2.0.2")
    kapt("net.strokkur.commands:processor-paper:2.0.2")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

// simple script to sync the plugin to my test server
tasks.register("update", Exec::class) {
    dependsOn(tasks.build)
    commandLine("../../dot/local/bin/update.sh")
}
