package com.m14n.billib.data.dao.collections

import com.m14n.billib.data.dao.FindByDate
import com.m14n.billib.data.dao.HasDate
import java.util.*

/**
 * [FindByDate] implementation based on a simple search in a given collection
 */
class FindByDateCollectionStrategy<T : HasDate>(
    private val data: Collection<T>
) : FindByDate<T> {
    override fun findByDate(date: Date): T? = data.find { it.date == date }
}

/**
 * [FindByDate] implementation based on a map with an id as a date
 */
class FindByDateMapStrategy<T : HasDate>(
    private val dateToObject: Map<Date, T>
) : FindByDate<T> {
    constructor(data: Collection<T>) : this(data.associateBy { it.date })

    override fun findByDate(date: Date): T? = dateToObject[date]
}
