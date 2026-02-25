package com.ifochka.m14n.rest.duplicate.rest

interface IDuplicateController {
    fun checkLastWeek(
        from: Long,
        size: Long,
    )
}
