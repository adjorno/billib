package com.m14n.billib.data.dao.collections

import com.m14n.billib.data.dao.FindByName
import com.m14n.billib.data.dao.HasName

/**
 * [FindByName] implementation based on a simple search in a given collection
 */
class FindByNameCollectionStrategy<T : HasName>(
    private val data: Collection<T>
) : FindByName<T> {
    override fun findByName(name: String): T? = data.find { it.name == name }
}

/**
 * [FindByName] implementation based on a map with an id as a name
 */
class FindByNameMapStrategy<T : HasName>(
    private val nameToObject: Map<String, T>
) : FindByName<T> {
    constructor(data: Collection<T>) : this(data.associateBy { it.name })

    override fun findByName(name: String): T? = nameToObject[name]
}
