package com.ifochka.billib.data.error

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException

/**
 * Maps exceptions to typed ChartError.
 */
object ErrorMapper {
    fun mapError(throwable: Throwable): ChartError =
        when (throwable) {
            is IOException -> ChartError.NetworkError

            is ClientRequestException -> {
                when (throwable.response.status) {
                    HttpStatusCode.NotFound -> ChartError.Unknown("Resource not found")
                    else -> ChartError.ServerError(
                        code = throwable.response.status.value,
                        message = throwable.message,
                    )
                }
            }

            is ServerResponseException -> ChartError.ServerError(
                code = throwable.response.status.value,
                message = throwable.message,
            )

            else -> ChartError.Unknown(throwable.message ?: "Unknown error")
        }

    fun mapEmptyCharts(): ChartError = ChartError.NoChartsAvailable

    fun mapChartNotFound(chartId: Long): ChartError = ChartError.ChartNotFound(chartId)
}
