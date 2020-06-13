package com.adjorno.billib.rest.db


import javax.persistence.*

@Entity
@Table(name = "CHART_TRACK")
data class ChartTrack(
        @Id
        @Column(name = "_id")
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long? = null,

        @OneToOne
        @JoinColumn(name = "TRACK_ID")
        var track: Track? = null,

        @OneToOne
        @JoinColumn(name = "CHART_LIST_ID")
        var chartList: ChartList? = null,

        @Column(name = "RANK")
        var rank: Int = 0,

        @Column(name = "LAST_WEEK_RANK")
        var lastWeekRank: Int = 0
)
