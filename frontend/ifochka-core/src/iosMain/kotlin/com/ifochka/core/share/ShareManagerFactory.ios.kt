package com.ifochka.core.share

// Native iOS share sheet via UIActivityViewController — tracked as follow-up.
actual fun createShareManager(): ShareManager = NoShareManager()
