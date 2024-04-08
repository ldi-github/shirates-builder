import org.beryx.jlink.JlinkZipTask

plugins {
    id("java")
    id("application")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "shirates"
version = "0.9.0-SNAPSHOT"

val shiratesCoreVersion = "6.7.6-SNAPSHOT"
val appiumClientVersion = "9.1.0"

val userHome = System.getProperty("user.home")

repositories {
    mavenCentral()

    maven(url = "file:/$userHome/github/ldi-github/shirates-core/build/repository")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass = "shirates.builder.BuilderApplicationExecute"
}

kotlin {
    jvmToolchain(17)
}

javafx {
    version = "17.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("com.dlsc.formsfx:formsfx-core:11.6.0") {
        exclude(group = "org.openjfx")
    }

    implementation(kotlin("reflect"))

    // JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")

    // shirates-core
    implementation("io.github.ldi-github:shirates-core:$shiratesCoreVersion")
    testImplementation("io.github.ldi-github:shirates-core:$shiratesCoreVersion")

    // Appium
    testImplementation("io.appium:java-client:$appiumClientVersion")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    testImplementation("org.apache.logging.log4j:log4j-core:2.22.1")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-nop
    testImplementation("org.slf4j:slf4j-nop:2.0.9")

    // Apache Commons IO
    testImplementation("commons-io:commons-io:2.15.1")

    // Assert J
    testImplementation("org.assertj:assertj-core:3.24.2")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-javafx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.0")


}

tasks.withType<Test> {
    useJUnitPlatform()
}

jlink {
    imageZip = project.file("${buildDir}/distributions/app-${javafx.platform.classifier}.zip")
    options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = "app"
    }
}

tasks.withType<JlinkZipTask> {
    group = "distribution"
}