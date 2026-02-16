package com.ifochka.billib.data.error

/**
 * Typed errors for chart operations.
 * Provides specific error types for better user feedback.
 */
sealed interface ChartError {
    /**
     * Network connectivity issue.
     * User should check their internet connection.
     */
    data object NetworkError : ChartError

    /**
     * No charts available from the API.
     * This is unexpected and may indicate a backend issue.
     */
    data object NoChartsAvailable : ChartError

    /**
     * Requested chart ID does not exist.
     */
    data class ChartNotFound(
        val chartId: Long,
    ) : ChartError

    /**
     * Server returned an error response.
     */
    data class ServerError(
        val code: Int,
        val message: String?,
    ) : ChartError

    /**
     * Unknown error occurred.
     * Fallback for unexpected errors.
     */
    data class Unknown(
        val message: String,
    ) : ChartError

    /**
     * Get user-friendly error message.
     */
    fun toDisplayMessage(): String =
        when (this) {
            is NetworkError -> "No internet connection. Check your network and try again."
            is NoChartsAvailable -> "No charts available. Please try again later."
            is ChartNotFound -> "Chart not found (ID: $chartId)."
            is ServerError -> "Server error ($code). Please try again later."
            is Unknown -> message.ifBlank { "An unexpected error occurred. Please try again." }
        }
}
