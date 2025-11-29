import com.kageiit.jacobo.JacoboTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.net.URI
import java.time.LocalDate

plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.2"
    kotlin("plugin.spring") version "2.0.20"
    kotlin("jvm") version "2.0.20"
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.kageiit.jacobo") version "2.1.0"
    `maven-publish`
    signing
    jacoco
}

group = "group.phorus"
description = "Library containing common Spring WebFlux exception handling logic."
version = "0.0.377"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "phorus-group/exception-handling"}")
        credentials {
            username = project.findProperty("readUsername") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("readPassword") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework:spring-tx")

    // Kotlin
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.platform:junit-platform-suite:1.11.0")
    testImplementation("io.cucumber:cucumber-java:7.18.1")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.18.1")
    testImplementation("io.cucumber:cucumber-spring:7.18.1")
    testImplementation("junit:junit")
}

val repoUrl = System.getenv("GITHUB_REPOSITORY")?.let { "https://github.com/$it" }
    ?: "https://github.com/phorus-group/exception-handling"

tasks {
    getByName<BootJar>("bootJar") {
        enabled = false
    }

    // Jacoco config
    jacocoTestReport {
        executionData.setFrom(fileTree(project.layout.buildDirectory).include("/jacoco/*.exec"))

        reports {
            xml.required.set(true)
            csv.required.set(true)
        }

        finalizedBy("jacobo")
    }

    withType<Test> {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    register<JacoboTask>("jacobo") {
        description = "Transforms jacoco xml report to cobertura"
        group = "verification"

        jacocoReport = file("${project.layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml")
        coberturaReport = file("${project.layout.buildDirectory.get()}/reports/cobertura/cobertura.xml")
        includeFileNames = emptySet()

        val field = JacoboTask::class.java.getDeclaredField("srcDirs")
        field.isAccessible = true
        field.set(this, sourceSets["main"].allSource.srcDirs.map { it.path }.toTypedArray())

        dependsOn(jacocoTestReport)
    }

    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.fromTarget(java.targetCompatibility.toString()))
        }
    }

    dokkaHtml.configure {
        val branch = System.getenv("GITHUB_REF_NAME") ?: "main"
        dokkaSourceSets {
            configureEach {
                reportUndocumented.set(true)
                platform.set(org.jetbrains.dokka.Platform.jvm)
                sourceRoot(file("src"))
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URI("$repoUrl/tree/$branch/src/main/kotlin").toURL())
                    remoteLineSuffix.set("#L")
                }
            }
        }
        val currentYear = LocalDate.now().year
        pluginsMapConfiguration.set(
            mapOf(
                "org.jetbrains.dokka.base.DokkaBase" to
                        """{"footerMessage":"© $currentYear Phorus Group - Licensed under the <a target=\"_blank\" href=\"$repoUrl/blob/$branch/LICENSE\">Apache 2 license</a>."}"""
            )
        )
    }

    named<Jar>("javadocJar") {
        from(dokkaHtml)
        dependsOn(dokkaHtml)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            pom {
                name.set(project.name)
                description.set(project.description ?: "")
                url.set(repoUrl)

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("irios.phorus")
                        name.set("Martin Rios")
                        email.set("irios@phorus.group")
                        organization.set("Phorus Group")
                        organizationUrl.set("https://phorus.group")
                    }
                }

                scm {
                    url.set(repoUrl)
                    connection.set("scm:git:$repoUrl.git")
                    developerConnection.set("scm:git:$repoUrl.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "phorus-group/exception-handling"}")
            credentials {
                username = project.findProperty("publishUsername") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("publishPassword") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

//signing {
//    val signingKey = project.findProperty("signingInMemoryKey") as String?
//    val signingPassword = project.findProperty("signingInMemoryKeyPassword") as String?
//
//    if (signingKey != null && signingPassword != null) {
//        useInMemoryPgpKeys(signingKey, signingPassword)
//        sign(publishing.publications["maven"])
//    }
//}