package com.adjorno.billib.rest.db

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
@Table(name = "CHART_LIST")
data class ChartList(
    @Id
    @Column(name = "_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "CHART_ID")
    var chart: Chart? = null,

    @OneToOne
    @JoinColumn(name = "WEEK_ID")
    var week: Week? = null,

    @Column(name = "NUMBER")
    @JsonIgnore
    var number: Int? = null,

    @Column(name = "PREVIOUS_CHART_LIST_ID")
    @JsonIgnore
    var previousChartListId: Long? = null,

    @Transient
    var chartTracks: List<ChartTrack>? = null
) {
    override fun toString() = "$number. ${chart.toString()} ${week.toString()}"
}
