<h1 align="center">ComposeSignWithGoogle</h1><br>

<div align="center">
<a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
<a href="https://android-arsenal.com/api?level=21" rel="nofollow">
    <img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" style="max-width: 100%;">
</a>
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
  <img src="https://img.shields.io/badge/iOS-12%2B-blue.svg?logo=apple" alt="iOS 12+ Badge" />

<a href="https://github.com/the-best-is-best/"><img alt="Profile" src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white" height="20"/></a>
</div>

### The ComposeSignWithGoogle package for Compose Multiplatform enables seamless Google sign-in integration for both Android and iOS platforms. It simplifies the process of implementing Google authentication in apps built with Jetpack Compose and targets multiple platforms

<hr>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.the-best-is-best/compose-google-sigin-in)](https://central.sonatype.com/artifact/io.github.the-best-is-best/compose-google-sigin-in)

ComposeSignWithGoogle is available on `mavenCentral()`.

```kotlin
implementation("io.github.the-best-is-best:compose-google-sigin-in:1.1.0")
```

### ✅ **Install via SPM

You can install **GoogleSignIn** directly using **Swift Package Manager (SPM)** from:  
🔗 [GoogleSignIn-iOS on GitHub](https://github.com/google/GoogleSignIn-iOS)

## How to use it

### First in androidMain

<br>

```kotlin
AndroidGoogleSignIn.initialization(this)
AndroidKMMCrypto.init("key0")
```

### Second in iosMain

```kotlin
fun MainViewController(): UIViewController {
    ....
    KGoogleSignIn().initialize("com.googleusercontent.apps.204571788770-rhck30jdb0321gosbi07h94s50midf52")
    ...
    return ComposeUIViewController { App() }
}
```

### Third in commonMain

- Note i use firebase

```kotlin
    val googleSign = KGoogleSignIn()
    Button(onClick = {
        scope.launch {
            val clientId:String = if(PlatformData().name == "android" ) "608756724133-mj7pg7pcrlvjjdrib1g050vgap51a1s3.apps.googleusercontent.com" else "608756724133-omnidr2brkle3pp9d2s30rd87olvj7d9.apps.googleusercontent.com"
            val cred = googleSign.getCredential(clientId)
            cred.onSuccess {
                Firebase.auth.signInWithCredential(authCredential = GoogleAuthProvider.credential(
                    idToken = it.idToken, accessToken = it.accessToken))
            }
    
        }
    }){
        Text("Sign in with google")
    }
    Button(onClick = {
        scope.launch {
            println("user id is ${googleSign.getUserData()?.userId}")
        }
    }){
        Text("Get user data")
    }
    
    Button(onClick = {
        scope.launch {
            googleSign.signOut()
            Firebase.auth.signOut()
        }
    }){
        Text("Logout")
    }
```

### Functionality

```kotlin
package io.github.sign_in_with_google

expect class KGoogleSignIn() {
    suspend fun getCredential(
        clientId: String,
        setFilterByAuthorizedAccounts: Boolean = false
    ): Result<GoogleCredential>

    suspend fun getUserData(): UserData?
    suspend fun signOut()

}
```
