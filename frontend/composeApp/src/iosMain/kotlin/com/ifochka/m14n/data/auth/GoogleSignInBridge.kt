package com.ifochka.m14n.data.auth

interface GoogleSignInDelegate {
    fun signIn(completion: (idToken: String?, accessToken: String?) -> Unit)
}

object GoogleSignInDelegateHolder {
    var delegate: GoogleSignInDelegate? = null
}
