package com.app.minder.util

import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object EmailVerifier {
    private val auth = FirebaseAuth.getInstance()

    suspend fun createAndSendVerification(email: String, password: String) {
        suspendCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }

        suspendCoroutine { continuation ->
            auth.currentUser?.sendEmailVerification()
                ?.addOnSuccessListener { continuation.resume(Unit) }
                ?.addOnFailureListener { continuation.resumeWithException(it) }
                ?: continuation.resumeWithException(Exception("Пользователь не найден"))
        }
    }

    suspend fun isEmailVerified(): Boolean {
        suspendCoroutine { continuation ->
            auth.currentUser?.reload()
                ?.addOnSuccessListener { continuation.resume(Unit) }
                ?.addOnFailureListener { continuation.resumeWithException(it) }
                ?: continuation.resumeWithException(Exception("Пользователь не найден"))
        }

        return auth.currentUser?.isEmailVerified == true
    }

    fun cleanup() {
        auth.currentUser?.delete()
    }

    fun signOut() {
        auth.signOut()
    }
}