package com.ifochka.m14n.rest.model

import com.ifochka.m14n.rest.catalog.domain.Track
import java.util.ArrayList

data class TrendList(
    val name: String,
) {
    val tracks: MutableList<Track> = ArrayList()
}
