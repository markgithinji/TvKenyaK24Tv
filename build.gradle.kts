// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Declare plugin versions here, but don't apply android application plugin in root
    id("com.android.application") version "8.11.1" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
}

// Register clean task in root project
tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
