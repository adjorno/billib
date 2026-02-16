package com.ifochka.billib.rest

interface IDuplicateController {
    fun checkLastWeek(
        from: Long,
        size: Long,
    )
}
