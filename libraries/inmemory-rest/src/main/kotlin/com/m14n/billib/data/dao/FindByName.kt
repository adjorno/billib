package com.m14n.billib.data.dao

interface FindByName<T> {
    /**
     * @param name Name to search for a data object
     * @return [T] object with the given name, null if not found
     */
    fun findByName(name: String): T?
}

/**
 * Generic interface for model objects with names
 */
interface HasName {
    val name: String
}
