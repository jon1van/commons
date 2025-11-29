rootProject.name = "commons"

include("commons-utils")
include("commons-ids")
include("commons-units")
include("commons-maps")
include("commons-collect")


// Check that every subproject has a custom build file, based on the project name.
//   Got this idiom from org.junit.jupiter's gradle setup.
//   It avoids having numerous identically named files.
rootProject.children.forEach { project ->
    project.buildFileName = "${project.name}.gradle.kts"
    require(project.buildFile.isFile) {
        "${project.buildFile} must exist"
    }
}