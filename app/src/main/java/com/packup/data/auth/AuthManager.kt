package com.packup.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    private val auth: FirebaseAuth
) {
    suspend fun ensureSignedIn(): FirebaseUser {
        auth.currentUser?.let { return it }
        return auth.signInAnonymously().await().user
            ?: throw IllegalStateException("Anonymous sign-in returned null user")
    }

    val uid: String? get() = auth.currentUser?.uid
}
