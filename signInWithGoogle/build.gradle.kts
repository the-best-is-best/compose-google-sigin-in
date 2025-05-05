import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.maven.publish)
}


apply(plugin = "maven-publish")
apply(plugin = "signing")


tasks.withType<PublishToMavenRepository> {
    val isMac = getCurrentOperatingSystem().isMacOsX
    onlyIf {
        isMac.also {
            if (!isMac) logger.error(
                """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
            )
        }
    }
}



extra["groupId"] = "io.github.the-best-is-best"
extra["artifactId"] = "compose-google-sigin-in"
extra["version"] = "1.1.0"
extra["packageName"] = "ComposeSignWithGoogle"
extra["packageUrl"] = "https://github.com/the-best-is-best/compose-google-sigin-in"
extra["packageDescription"] =
    "The ComposeSignWithGoogle package for Compose Multiplatform enables seamless Google sign-in integration for both Android and iOS platforms. It simplifies the process of implementing Google authentication in apps built with Jetpack Compose and targets multiple platforms."
extra["system"] = "GITHUB"
extra["issueUrl"] = "https://github.com/the-best-is-best/compose-google-sigin-in/issues"
extra["connectionGit"] = ".git"

extra["developerName"] = "Michelle Raouf"
extra["developerNameId"] = "MichelleRaouf"
extra["developerEmail"] = "eng.michelle.raouf@gmail.com"


mavenPublishing {
    coordinates(
        extra["groupId"].toString(),
        extra["artifactId"].toString(),
        extra["version"].toString()
    )

    publishToMavenCentral(SonatypeHost.S01, true)
    signAllPublications()

    pom {
        name.set(extra["packageName"].toString())
        description.set(extra["packageDescription"].toString())
        url.set(extra["packageUrl"].toString())
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        issueManagement {
            system.set(extra["system"].toString())
            url.set(extra["issueUrl"].toString())
        }
        scm {
            connection.set(extra["connectionGit"].toString())
            url.set(extra["packageUrl"].toString())
        }
        developers {
            developer {
                id.set(extra["developerNameId"].toString())
                name.set(extra["developerName"].toString())
                email.set(extra["developerEmail"].toString())
            }
        }
    }

}


signing {
    useGpgCmd()
    sign(publishing.publications)
}


kotlin {

// Target declarations - add or remove as needed below. These define
// which platforms this KMP module supports.
// See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "io.github.sign_in_with_google"
        compileSdk = 35
        minSdk = 21


    }

// For iOS targets, this is also where you should
// configure native binary output. For more information, see:
// https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

// A step-by-step guide on how to include this library in an XCode
// project can be found here:
// https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "signInWithGoogleKit"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
//        macosX64(),
//        macosArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
        it.compilations.getByName("main") {
            val defFileName = when (it.name) {
                "iosX64" -> "iosX64.def"
                "iosArm64" -> "iosArm64.def"
                "iosSimulatorArm64" -> "iosSimulatorArm64.def"
                "macosX64" -> "macosX64.def"
                "macosArm64" -> "macosArm64.def"

                else -> throw IllegalStateException("Unsupported target: ${target.name}")
            }

            val defFile = project.file("interop/$defFileName")
            if (defFile.exists()) {
                cinterops.create("SignInWithGoogle") {
                    defFile(defFile)
                    packageName = "io.github.native.sign_in_with_google"
                }
            } else {
                logger.warn("Def file not found for target ${target.name}: ${defFile.absolutePath}")
            }
        }

    }

// Source set declarations.
// Declaring a target automatically creates a source set with the same name. By default, the
// Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
// common to share sources between related targets.
// See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        all {
            languageSettings.apply {
                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                if (name.lowercase().contains("ios")) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                    optIn("kotlinx.cinterop.BetaInteropApi")
                }
            }
        }

        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                // Add KMP dependencies here
                implementation(libs.kotlinx.coroutines.core)
                api(libs.kmm.crypto)

            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.

                implementation(libs.play.services.auth)
                implementation(libs.androidx.credentials)
                implementation(libs.androidx.credentials.play.services.auth)
                implementation(libs.googleid)
            }
        }


        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }

}



abstract class GenerateDefFilesTask : DefaultTask() {

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val interopDir: DirectoryProperty

    @TaskAction
    fun generate() {
        // Ensure the directory exists
        interopDir.get().asFile.mkdirs()

        // Constants
        val headers =
            "GoogleSignIn.h"

        // Map targets to their respective paths
        val targetToPath = mapOf(
            "iosX64" to "ios-arm64_x86_64-simulator",
            "iosArm64" to "ios-arm64",
            "iosSimulatorArm64" to "ios-arm64_x86_64-simulator",
            "macosX64" to "macos-arm64_x86_64",
            "macosArm64" to "macos-arm64_x86_64",
        )

        // Helper function to generate header paths
        fun headerPath(target: String): String {
            return interopDir.dir("src/$headers")
                .get().asFile.absolutePath
        }

        // Generate headerPaths dynamically
        val headerPaths = targetToPath.mapValues { (target, _) ->
            headerPath(target)
        }

        // List of targets derived from targetToPath keys
        val iosTargets = targetToPath.keys.toList()

        // Loop through the targets and create the .def files
        iosTargets.forEach { target ->
            val headerPath = headerPaths[target] ?: return@forEach
            val defFile = File(interopDir.get().asFile, "$target.def")

            // Generate the content for the .def file
            val content = """
                language = Objective-C
                package = ${packageName.get()}
                headers = $headerPath
            """.trimIndent()

            // Write content to the .def file
            defFile.writeText(content)
            println("Generated: ${defFile.absolutePath} with headers = $headerPath")
        }
    }
}
// Register the task within the Gradle build
tasks.register<GenerateDefFilesTask>("generateDefFiles") {
    packageName.set("io.github.native.sign_in_with_google")
    interopDir.set(project.layout.projectDirectory.dir("interop"))
}

tasks.named("build") {
    dependsOn(tasks.named("generateDefFiles"))
}