# SecureStorage

`SecureStorage` is a lightweight and secure key-value storage wrapper for Android.  
It replaces `EncryptedSharedPreferences` with a custom, testable, and reusable solution suitable for projects requiring secure local persistence.

## Features

- AES-based encryption support
- Fully tested with instrumented tests
- Supports primitive types (`String`, `Int`, `Boolean`) and Kotlin-serializable objects
- Multi-client support through `clientId` for isolated storage per user or environment
- No heavy dependencies

## Installation

### 1. Add JitPack to your repositories

In your `settings.gradle.kts` or `build.gradle.kts` (root level):

```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
}

### 2. Add the library dependency

```kotlin
dependencies {
    implementation("com.github.YOUR_GITHUB_USERNAME:securestorage:VERSION_TAG")
}
``

### 3. Basic Usage

```kotlin
val helper = SecurePreferencesManager.getHelper(context, clientId = "default")

helper.putString("token", "abc123")
val token = helper.getString("token")
```
Supports other types:

```kotlin
helper.putInt("launchCount", 5)
helper.putBoolean("isLoggedIn", true)
```

Storing and retrieving Kotlin-serialized objects:

```kotlin
@Serializable
data class User(val id: Int, val name: String)

val user = User(1, "Mario")
helper.putSerializable("user", user, User.serializer())

val restored = helper.getSerializable("user", User.serializer())
```

#### Clearing Data

```kotlin
helper.remove("token")                           // Removes a specific key
SecurePreferencesManager.clearHelper("default")  // Clears cached helper instance
SecurePreferencesManager.clearAll()              // Clears all cached instances (data is not deleted)
```

Requirements
	•	Android 7.0+ (API 24 or higher)
	•	Kotlin 1.9+
	•	Kotlin DSL for Gradle recommended
