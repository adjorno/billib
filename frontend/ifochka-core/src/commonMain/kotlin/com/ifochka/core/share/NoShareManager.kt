package com.ifochka.core.share

class NoShareManager : ShareManager {
    override val hasNativeShare = false

    override fun nativeShare(text: String) = Unit
}
