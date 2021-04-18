package com.m14n.billib.data.dao

interface FindById<T : HasId> {
    /**
     * @param id Identifier to search for the data object
     * @return [T] object with the given id, null if not found
     */
    fun findById(id: Long?): T?
}

/**
 * Extension function to request for an object with the given id.
 * @throws IllegalArgumentException if object is not found.
 */
fun <T : HasId> FindById<T>.requestById(id: Long?) = findById(id)
    ?: throw IllegalArgumentException("Could not find data object with id $id")

/**
 * Generic interface for model objects with ids
 */
interface HasId {
    val id: Long
}
