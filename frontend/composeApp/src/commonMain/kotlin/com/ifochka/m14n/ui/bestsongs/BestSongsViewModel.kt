package com.ifochka.m14n.ui.bestsongs

import androidx.lifecycle.ViewModel
import com.ifochka.m14n.data.api.M14nApi
import com.ifochka.m14n.data.artwork.ArtworkRepository

@Suppress("UnusedPrivateProperty")
class BestSongsViewModel(
    private val api: M14nApi,
    private val artworkRepository: ArtworkRepository,
) : ViewModel()
