plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21" apply false
    id("com.gradleup.shadow") version "8.3.5" apply false
}

allprojects {
    group = "xuanmo.aubade"
    version = property("version") as String

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.extendedclip.com/releases/")
        maven("https://maven.seventeen.artist/priv-seventeen-artist")
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:unchecked", "-Xlint:deprecation"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// 根项目无源码，禁用空 jar，避免与 aubade-core 插件 jar 同名混淆
tasks.named<Jar>("jar") { enabled = false }
