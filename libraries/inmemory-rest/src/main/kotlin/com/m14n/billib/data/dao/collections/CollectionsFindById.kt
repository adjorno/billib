package com.m14n.billib.data.dao.collections

import com.m14n.billib.data.dao.FindById
import com.m14n.billib.data.dao.HasId

/**
 * [FindById] implementation based on a simple search in a given collection
 */
class FindByIdCollectionStrategy<T : HasId>(
    private val data: Collection<T>
) : FindById<T> {
    override fun findById(id: Long?): T? = data.find { it.id == id }
}

/**
 * [FindById] implementation based on a map with an id as a key
 */
class FindByIdMapStrategy<T : HasId>(
    private val idToObject: Map<Long, T>
) : FindById<T> {
    constructor(data: Collection<T>) : this(data.associateBy { it.id })

    override fun findById(id: Long?): T? = idToObject[id]
}
