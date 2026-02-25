package com.ifochka.m14n.rest.search.domain

class SearchResult<T> {
    var total = 0
    var offset = 0
    var results: List<T>? = null
}
