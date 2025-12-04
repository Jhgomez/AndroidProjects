plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

android {
    namespace = "okik.tech.coachmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 18

        aarMetadata {
            minCompileSdk = 18
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "okik.tech.coachmark"
            artifactId = "coachmark"
            version = "1.0"

            afterEvaluate {
                from(components["release"])
            }
        }
    }

    repositories {
        maven {
            name = "coachmark"
            url = uri(layout.buildDirectory.dir("coachmarklib"))
        }
    }
}

dependencies {
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
}