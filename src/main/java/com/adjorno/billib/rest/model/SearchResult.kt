package com.adjorno.billib.rest.model

class SearchResult<T> {
    var total = 0
    var offset = 0
    var results: List<T>? = null
}
