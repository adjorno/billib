package com.m14n.billib.data.artist

import com.m14n.billib.data.dao.HasId
import com.m14n.billib.data.dao.HasName
import kotlinx.serialization.Serializable

/**
 * Artist data primitive
 */
@Serializable
data class Artist(
    override val id: Long,
    override val name: String
) : HasId,
    HasName
