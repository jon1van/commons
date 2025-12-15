plugins {
    `java-library`
    // plugin docs are here: https://vanniktech.github.io/gradle-maven-publish-plugin/central/
    id("com.vanniktech.maven.publish") version "0.35.0"
}

// Set's the parent and ALL child modules' group & Version
allprojects {
    group = "io.github.jon1van"
    version = "1.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.vanniktech.maven.publish")

    dependencies {
        implementation("com.google.guava:guava:33.5.0-jre")

        testImplementation("org.assertj:assertj-core:3.26.0")
        testImplementation("org.junit.platform:junit-platform-launcher:1.8.2")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    }

//  This idiom completely mutes the "javadoc" task from java-library.
//  Javadoc is still produced, but you won't get warnings OR build failures due to javadoc
//  I decided to turn warning off because the amount of javadoc required for builder was too much.
    tasks {
        javadoc {
            options {
                (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
            }
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    mavenPublishing {
        publishToMavenCentral(validateDeployment = true)
        signAllPublications()

        // Set the POM content that is shared btw all modules
        pom {
            name.set(project.name)
            inceptionYear.set("2025")
            url.set("https://github.com/jon1van/commons.git")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("jon1van")
                    name.set("Jon Parker")
                    url.set("https://github.com/jon1van")
                }
            }
            scm {
                url.set("https://github.com/jon1van/commons.git")
                connection.set("scm:git:https://github.com/jon1van/commons.git")
                developerConnection.set("scm:git:ssh://git@github.com/jon1van/commons.git")
            }
        }
    }
}