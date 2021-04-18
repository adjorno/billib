package com.m14n.billib.data.dao

interface Dao<T : HasId> :
    FindAll<T>,
    FindById<T>

class DelegateDao<T : HasId>(
    findById: FindById<T>,
    findAll: FindAll<T>
) : Dao<T>,
    FindById<T> by findById,
    FindAll<T> by findAll

interface FindAll<T> {
    fun findAll(): Collection<T>
}
