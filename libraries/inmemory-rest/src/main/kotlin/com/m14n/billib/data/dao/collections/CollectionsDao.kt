package com.m14n.billib.data.dao.collections

import com.m14n.billib.data.dao.*
import java.util.*

fun <T : HasId> collectionsDao(data: Collection<T>): Dao<T> =
    DelegateDao(
        FindByIdMapStrategy(data),
        CollectionsFindAll(data)
    )

class CollectionsFindAll<T>(
    private val data: Collection<T>
) : FindAll<T> {
    override fun findAll(): Collection<T> = Collections.unmodifiableCollection(data)
}
