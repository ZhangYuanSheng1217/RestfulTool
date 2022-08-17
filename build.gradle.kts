plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.32"
    id("org.jetbrains.intellij") version "1.8.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies{
    implementation("cn.hutool:hutool-all:5.7.16")
    implementation("org.dom4j:dom4j:2.1.3")

}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    updateSinceUntilBuild.set(true)
    version.set("2022.2")
    type.set("IC") // Target IDE Platform
    pluginName.set("RestfulTool")
    plugins.set(listOf("java","properties","yaml","Kotlin"))

}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("213")
        untilBuild.set("223.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
