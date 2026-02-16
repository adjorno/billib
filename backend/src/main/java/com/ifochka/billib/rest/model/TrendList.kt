package com.ifochka.billib.rest.model

import com.ifochka.billib.rest.db.Track
import java.util.ArrayList

data class TrendList(
    val name: String,
) {
    val tracks: MutableList<Track> = ArrayList()
}
