package com.ifochka.m14n.data.auth

// GitLive firebase-auth 2.4.0 does not publish a wasmJs artifact.
// Firebase JS SDK initialization for wasmJs is wired in Iteration 2 via JS interop.
actual fun initFirebase() = Unit
