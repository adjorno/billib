package com.ifochka.m14n.share

import android.content.Intent
import com.ifochka.m14n.AppContext

class AndroidShareManager : ShareManager {
    override val hasNativeShare = true

    override fun nativeShare(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        AppContext.applicationContext.startActivity(chooser)
    }
}
