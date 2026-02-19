package com.ifochka.m14n.share

class NoShareManager : ShareManager {
    override val hasNativeShare = false

    override fun nativeShare(text: String) = Unit
}
