plugins {
    kotlin("jvm") version "2.0.20"
    id("maven-publish")
}

group = "com.seblit.passgen"
version = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.seblit.passgen"
            artifactId = "passgen"
            version = "1.0.0"
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "Github_Packages"
            url = uri("https://maven.pkg.github.com/SebLit/PassGen")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.test {
    useJUnitPlatform()
}