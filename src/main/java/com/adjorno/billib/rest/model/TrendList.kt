package com.adjorno.billib.rest.model

import com.adjorno.billib.rest.db.Track
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class TrendList(val name: String) {
    val tracks: MutableList<Track> = ArrayList()
}
