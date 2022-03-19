package com.adjorno.billib.rest

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Artist not found")
class ArtistNotFoundException : RuntimeException()