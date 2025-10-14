plugins {
    id("jacoco")
}

dependencies {


    implementation("org.apache.commons:commons-math3:3.6.1")

    testImplementation("org.assertj:assertj-core:3.26.0")

    testImplementation("org.junit.platform:junit-platform-launcher:1.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")

    testImplementation("org.apache.avro:avro:1.11.0")
}


tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events("SKIPPED", "FAILED") // Options are: "PASSED", "SKIPPED", "FAILED"
    }
}


tasks {
    withType<JavaCompile> {
        description = "A place to set javac options, e.g -Xlint."

//        options.compilerArgs.add("-Xdoclint:all,-missing")
//        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "commons-units"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(project.name)
                description.set("Units like LatLong, Distance, Speed")
                url.set("https://github.com/jon1van/commons")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("jparker")
                        name.set("Jon Parker")
//                        email.set("")
                    }
                }

                //REQUIRED! To publish to maven central (from experience the leading "scm:git" is required too)
                scm {
                    connection.set("scm:git:https://github.com/jon1van/commons.git")
                    developerConnection.set("scm:git:ssh://github.com/jon1van/commons.git")
                    url.set("https://github.com/jon1van/commons")
                }
            }
        }
    }
}