package com.m14n.billib.data.dao

import java.util.*

interface FindByDate<T> {
    /**
     * @param date Date to search for a data object
     * @return [T] object with the given date, null if not found
     */
    fun findByDate(date: Date): T?
}

/**
 * Generic interface for model objects with dates
 */
interface HasDate {
    val date: Date
}
