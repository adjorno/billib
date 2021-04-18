package com.m14n.billib.data.week

import com.m14n.billib.data.billboard.DateSerializer
import com.m14n.billib.data.dao.HasDate
import com.m14n.billib.data.dao.HasId
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Week(
    override val id: Long,
    @Serializable(with= DateSerializer::class)
    override val date: Date
) : HasId,
    HasDate
