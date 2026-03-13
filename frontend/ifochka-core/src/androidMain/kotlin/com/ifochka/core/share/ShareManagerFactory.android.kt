package com.ifochka.core.share

actual fun createShareManager(): ShareManager = AndroidShareManager()
