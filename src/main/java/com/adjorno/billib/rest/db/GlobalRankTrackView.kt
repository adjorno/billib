package com.adjorno.billib.rest.db

import org.hibernate.annotations.Immutable
import java.time.LocalDate
import javax.persistence.*

@Entity
@Immutable
@Table(name = "GLOBAL_RANK_TRACK")
data class GlobalRankTrackView(
    @Id
    @Column(name = "TRACK_ID")
    var trackId: Long,

    @Column(name = "RANK")
    var rank: Long,

    @Column(name = "TOTAL_APPEARANCES")
    var totalAppearances: Long,

    @Column(name = "PEAK_POSITION")
    var peakPosition: Int,

    @Column(name = "FIRST_CHART_DATE")
    var firstChartDate: LocalDate,

    @Column(name = "LAST_CHART_DATE")
    var lastChartDate: LocalDate
) {
    override fun toString() = "Track #$trackId: Global Rank #$rank ($totalAppearances appearances, peak #$peakPosition)"
}
