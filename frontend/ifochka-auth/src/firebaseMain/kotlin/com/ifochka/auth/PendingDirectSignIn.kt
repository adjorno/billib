package com.ifochka.auth

/**
 * Email set by JVM sign-in helpers after a direct custom-token sign-in, where
 * firebase-java-sdk does not populate FirebaseUser.email on the resulting currentUser.
 * Consumed (cleared) by FirebaseAuthRepository.syncStateFromCurrentUser().
 */
internal var pendingDirectSignInEmail: String? = null
