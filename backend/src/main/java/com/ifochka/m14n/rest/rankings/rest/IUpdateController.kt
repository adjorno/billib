package com.ifochka.m14n.rest.rankings.rest

import com.ifochka.m14n.rest.rankings.rest.dtos.UpdateResult

interface IUpdateController {
    fun updateDB(): UpdateResult
}
