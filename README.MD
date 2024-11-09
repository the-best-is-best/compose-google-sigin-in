<h1 align="center">ComposeSignWithGoogle</h1><br>

<div align="center">
<a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
<a href="https://android-arsenal.com/api?level=21" rel="nofollow">
    <img alt="API" src="https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat" style="max-width: 100%;">
</a>
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
  <img src="https://img.shields.io/badge/Platform-iOS%20%2F%20macOS-lightgrey.svg?logo=apple" alt="Badge iOS" />

<a href="https://github.com/the-best-is-best/"><img alt="Profile" src="https://img.shields.io/badge/github-%23181717.svg?&style=for-the-badge&logo=github&logoColor=white" height="20"/></a>
</div>

### The ComposeSignWithGoogle package for Compose Multiplatform enables seamless Google sign-in integration for both Android and iOS platforms. It simplifies the process of implementing Google authentication in apps built with Jetpack Compose and targets multiple platforms.

<hr>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.the-best-is-best/compose-google-sigin-in)](https://central.sonatype.com/artifact/io.github.the-best-is-best/compose-google-sigin-in)

ComposeSignWithGoogle is available on `mavenCentral()`.

```kotlin
implementation("io.github.the-best-is-best:compose-google-sigin-in:1.0.0")
```

## Need add this in pod file if not exist run ` pod init `

```pod
  #  use_frameworks!
   use_modular_headers!
  pod "GoogleSignIn" , "8.0.0"
  pod "GoogleSignInSwift" , "0.2.0"
```

## How to use it

### First in androidMain

<br>

```kotlin
AndroidGoogleSignIn.initialization(this)
```

### Second in commonMain

- Note i use firebase

```kotlin
    val googleSign = KGoogleSignIn()
Button(onClick = {
    scope.launch {
        val clientId: String =
            if (PlatformData().name == "android") "608756724133-mj7pg7pcrlvjjdrib1g050vgap51a1s3.apps.googleusercontent.com" else "608756724133-omnidr2brkle3pp9d2s30rd87olvj7d9.apps.googleusercontent.com"
        val cred = googleSign.getCredential(clientId)
        cred.onSuccess {
            Firebase.auth.signInWithCredential(
                authCredential = GoogleAuthProvider.credential(
                    idToken = it.idToken, accessToken = it.accessToken
                )
            )
        }

    }
}) {
    Text("Sign in with google")
}
Button(onClick = {
    scope.launch {
        println("user id is ${googleSign.getUserData()?.userId}")
    }
}) {
    Text("Get user data")
}
    
Button(onClick = {
    scope.launch {
        googleSign.signOut()
        Firebase.auth.signOut()
    }
}) {
    Text("Logut")
}
```