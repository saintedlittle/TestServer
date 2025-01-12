plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.graalvm.buildtools.native") version "0.10.4"
    id("application") // Исправлено
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Рекурсивно подключаем JAR-файлы из core и libraries
    val coreDir = file("core")
    val librariesDir = file("libraries")

    listOf(coreDir, librariesDir).forEach { dir ->
        if (dir.exists() && dir.isDirectory) {
            dir.walkTopDown().filter { it.isFile && it.extension == "jar" }.forEach { jarFile ->
                try {
                    // Пытаемся подключить как обычную зависимость
                    implementation(files(jarFile))
                } catch (e: Exception) {
                    println("⚠️ Failed to add JAR: ${jarFile.name}. Skipping. Error: ${e.message}")
                }
            }
        }
    }
}



application {
    mainClass.set("org.example.Main") // Убедитесь, что имя класса корректно
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("my-native-app") // Имя итогового бинарного файла
            buildArgs.add("--no-fallback") // Исключает механизм fallback
            buildArgs.add("--initialize-at-build-time=org.example") // Ускоряет запуск
        }
    }
    binaries.all {
        resources.autodetect()
    }
}
