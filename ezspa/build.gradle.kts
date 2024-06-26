val mvnGroup = "rip.kspar"
val mvnArtifact = "ezspa"
val mvnVersion = "0.4.0"
val repoUrl = "https://github.com/kspar/easy/tree/master/ezspa"

plugins {
    kotlin("js")
    id("com.jfrog.bintray") version "1.8.5"
    id("maven-publish")
}

group = mvnGroup
version = mvnVersion

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.2")
}

kotlin {
    js {
        browser {}
    }
}

bintray {
    user = System.getProperty("bintray.user")
    key = System.getProperty("bintray.key")
    setPublications("BintrayPublication")
    pkg.apply {
        repo = mvnArtifact
        name = mvnArtifact
        version.name = mvnVersion
        vcsUrl = repoUrl
        setLicenses("MIT")
    }
}

publishing {
    publications {
        create<MavenPublication>("BintrayPublication") {
            from(components["kotlin"])
            groupId = mvnGroup
            artifactId = mvnArtifact
            version = mvnVersion

            // Add sources too
            artifact(tasks["kotlinSourcesJar"])

            pom {
                name.set(mvnArtifact)
                description.set("Easy SPA microframework for Kotlin/JS")
                url.set(repoUrl)
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("kspar")
                        name.set("Kaspar Papli")
                    }
                }
            }
        }
    }
}
