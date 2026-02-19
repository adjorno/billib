package com.ifochka.m14n.share

interface ShareManager {
    val hasNativeShare: Boolean

    fun nativeShare(text: String)
}
