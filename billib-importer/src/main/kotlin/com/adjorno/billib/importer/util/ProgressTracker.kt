package com.adjorno.billib.importer.util

class ProgressTracker(private val enabled: Boolean = true) {

    fun track(phase: String, current: Int, total: Int) {
        if (!enabled) return

        val percent = if (total > 0) (current * 100.0 / total).toInt() else 0
        val bar = generateProgressBar(percent)
        print("\r[$phase] $bar $current/$total ($percent%)  ")

        if (current >= total) {
            println() // New line when complete
        }
    }

    fun log(message: String) {
        if (enabled) {
            println(message)
        }
    }

    fun section(title: String) {
        if (enabled) {
            println()
            println("=" * 80)
            println("  $title")
            println("=" * 80)
        }
    }

    private fun generateProgressBar(percent: Int, width: Int = 40): String {
        val filled = (percent * width) / 100
        val empty = width - filled
        return "█".repeat(filled) + "░".repeat(empty)
    }

    private operator fun String.times(n: Int) = repeat(n)
}
