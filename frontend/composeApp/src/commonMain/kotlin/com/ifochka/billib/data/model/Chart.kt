package com.ifochka.billib.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Chart(
    val id: Long? = null,
    val name: String? = null,
    val journal: Journal? = null,
    val listSize: Int? = null,
    val startDate: String? = null,
    val endDate: String? = null,
)
