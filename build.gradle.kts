plugins {
    kotlin("jvm") version "2.0.0"
    id("me.champeau.jmh") version "0.7.2"
}

group = "com.makarytskyi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

jmh {
    includeTests = false
    duplicateClassesStrategy = DuplicatesStrategy.WARN
}

dependencies {
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")

}
