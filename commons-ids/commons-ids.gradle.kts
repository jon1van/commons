plugins {
    id("jacoco")
    id("com.diffplug.spotless") version "8.0.0"
    id("com.vanniktech.maven.publish") version "0.35.0"
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


// Enforces a consistent code style with the "spotless" plugin
//
// See https://github.com/diffplug/spotless
// And https://github.com/diffplug/spotless/tree/main/plugin-gradle
spotless {
    java {
        // This formatter is "Good" but not "Perfect"
        // But, using this formatter allows multiple contributors to work in a "common style"
        palantirJavaFormat()

        // import order: static, JDK, 3rd Party
        importOrder("\\#", "java|javax", "")
        removeUnusedImports()
    }
}

// Optional: Override module-specific details for this module's pom
mavenPublishing {

    pom {
        name.set("commons-ids")
        description.set("Small library for TimeId")
    }
}