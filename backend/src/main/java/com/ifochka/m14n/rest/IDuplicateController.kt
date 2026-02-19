package com.ifochka.m14n.rest

interface IDuplicateController {
    fun checkLastWeek(
        from: Long,
        size: Long,
    )
}
