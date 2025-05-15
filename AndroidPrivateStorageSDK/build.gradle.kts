 plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
     alias(libs.plugins.serialization)
     id("maven-publish")
}

 val libraryVersion = "1.0.0"

android {
    namespace = "tech.scytale.security.androidprivatestoragesdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    implementation(libs.security.crypto)
    implementation(libs.serializaiton)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

 publishing {
     publications {
         create<MavenPublication>("aar") {
             groupId = "tech.scytale"
             artifactId = project.name
             version = libraryVersion

             artifact(
                 if (libraryVersion.endsWith("DEBUG")) {
                     File("$buildDir/outputs/aar/${project.name}-codedebug.aar")
                 } else {
                     File("$buildDir/outputs/aar/${project.name}-release.aar")
                 }
             )

             pom.withXml {
                 val dependenciesNode = asNode().appendNode("dependencies")
                 configurations.getByName("releaseCompileClasspath")
                     .resolvedConfiguration
                     .firstLevelModuleDependencies
                     .forEach {
                         val depNode = dependenciesNode.appendNode("dependency")
                         depNode.appendNode("groupId", it.moduleGroup)
                         depNode.appendNode("artifactId", it.moduleName)
                         depNode.appendNode("version", it.moduleVersion)
                     }
             }
         }
     }
 }