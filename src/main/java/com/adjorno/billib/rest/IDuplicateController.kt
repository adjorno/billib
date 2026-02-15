package com.adjorno.billib.rest

interface IDuplicateController {
    fun checkLastWeek(from: Long, size: Long)
}
