package io.github.sign_in_with_google


data class UserData(
    val userId: String,
    val email: String,
    val idToken: String?,
    val displayName: String?,
    val profilePictureUrl: String?,
    val phoneNumber: String?,
    val familyName: String?,
    val givenName: String?
)

data class GoogleCredential(
    val idToken: String,
    val accessToken: String?
)