package io.github.sign_in_with_google

expect class KGoogleSignIn() {
    suspend fun getCredential(
        clientId: String,
        setFilterByAuthorizedAccounts: Boolean = false
    ): Result<GoogleCredential>

    suspend fun getUserData(): UserData?
    suspend fun signOut()

}