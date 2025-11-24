plugins {
    `java-library`
    `maven-publish`
}


subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
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
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {

        implementation("com.google.guava:guava:33.5.0-jre")
//        implementation("com.google.guava:guava:32.1.2-jre")


//        implementation("com.google.code.gson:gson:2.8.9")
//
//        implementation("org.apache.commons:commons-math3:3.6.1")
//
//        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")
//        implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
//        implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
//        implementation("org.yaml:snakeyaml:2.4")
//
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")

        testImplementation("org.assertj:assertj-core:3.26.0")
    }
}