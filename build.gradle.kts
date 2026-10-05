plugins {
    id("java-library")
    id("maven-publish")
    id("idea")
    id("net.neoforged.moddev") version "2.0.78"
}

val mod_id: String by project
val mod_group_id: String by project
val mod_version: String by project
val neo_version: String by project
val parchment_minecraft_version: String by project
val parchment_mappings_version: String by project
val minecraft_version: String by project
val create_version: String by project
val ponder_version: String by project
val flywheel_version: String by project
val registrate_version: String by project
val minecraft_version_range: String by project
val neo_version_range: String by project
val loader_version_range: String by project
val mod_name: String by project
val mod_license: String by project
val mod_authors: String by project
val mod_description: String by project

version = mod_version
group = mod_group_id

base {
    archivesName.set(mod_id)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
    mavenCentral()
    maven("https://maven.createmod.net")
    maven("https://maven.ithundxr.dev/snapshots")
}

neoForge {
    version = neo_version

    parchment {
        mappingsVersion = parchment_mappings_version
        minecraftVersion = parchment_minecraft_version
    }

    runs {
        register("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
            jvmArguments.addAll("-Xmx2G", "-Xms512M")
        }
        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.INFO
        }
    }

    mods {
        register(mod_id) {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    implementation("com.simibubi.create:create-$minecraft_version:$create_version:slim") {
        isTransitive = false
    }
    implementation("net.createmod.ponder:ponder-neoforge:$ponder_version+mc$minecraft_version")
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-$minecraft_version:$flywheel_version")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-$minecraft_version:$flywheel_version")
    implementation("com.tterrag.registrate:Registrate:$registrate_version")
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to minecraft_version_range,
        "neo_version" to neo_version,
        "neo_version_range" to neo_version_range,
        "loader_version_range" to loader_version_range,
        "mod_id" to mod_id,
        "mod_name" to mod_name,
        "mod_license" to mod_license,
        "mod_version" to mod_version,
        "mod_authors" to mod_authors,
        "mod_description" to mod_description
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

sourceSets.main.configure {
    resources.srcDir(generateModMetadata)
}

neoForge.ideSyncTask(generateModMetadata)

tasks.withType<ProcessResources>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
