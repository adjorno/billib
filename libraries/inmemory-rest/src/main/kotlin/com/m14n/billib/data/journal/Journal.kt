package com.m14n.billib.data.journal

import com.m14n.billib.data.chart.Chart
import com.m14n.billib.data.dao.HasId
import com.m14n.billib.data.dao.HasName
import kotlinx.serialization.Serializable

@Serializable
data class Journal(
    override val id: Long,
    override val name: String,
    var charts: Collection<Chart>? = null
) : HasId,
    HasName {

    override fun toString() = "Journal $name"
}
