package com.ifochka.m14n.ui.chart.components

sealed class PositionBadge {
    data object None : PositionBadge()

    data object Same : PositionBadge()

    data object New : PositionBadge()

    data class Up(
        val change: Int,
    ) : PositionBadge()

    data class Down(
        val change: Int,
    ) : PositionBadge()
}
