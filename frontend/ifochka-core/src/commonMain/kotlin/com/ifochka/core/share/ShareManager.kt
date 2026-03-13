package com.ifochka.core.share

interface ShareManager {
    val hasNativeShare: Boolean

    fun nativeShare(text: String)
}
