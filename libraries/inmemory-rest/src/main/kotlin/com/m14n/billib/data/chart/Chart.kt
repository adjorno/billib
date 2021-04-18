package com.m14n.billib.data.chart

import com.m14n.billib.data.billboard.DateSerializer
import com.m14n.billib.data.chartlist.ChartList
import com.m14n.billib.data.dao.HasId
import com.m14n.billib.data.dao.HasName
import com.m14n.billib.data.journal.Journal
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Chart(
    override val id: Long,
    override val name: String,
    val journal: Journal,
    val listSize: Int,
    @Serializable(with = DateSerializer::class)
    val startDate: Date,
    @Serializable(with = DateSerializer::class)
    val endDate: Date? = null,
    var chartLists: Collection<ChartList>? = null
) : HasId,
    HasName {

    override fun toString() = "Chart $name"

    /**
     * Comparing journals with just id, default implementation will fail with StackOverflowError.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chart

        if (id != other.id) return false
        if (name != other.name) return false
        if (journal.id != other.journal.id) return false
        if (listSize != other.listSize) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (chartLists != other.chartLists) return false

        return true
    }

    /**
     * Generating journal hashcode with just id, default implementation will fail with StackOverflowError.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + journal.id.hashCode()
        result = 31 * result + listSize
        result = 31 * result + startDate.hashCode()
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + (chartLists?.hashCode() ?: 0)
        return result
    }

}
