package com.m14n.billib.data.dao.collections

import com.m14n.billib.data.artist.Artist
import com.m14n.billib.data.dao.FindById
import com.m14n.billib.data.dao.HasId
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@ExperimentalTime
class CollectionsFindByIdTest {

    @Test
    fun `test benchmark FindByIdCollectionStrategy and FindByIdMapStrategy for 100k records`() {
        `test benchmark FindByIdCollectionStrategy and FindByIdMapStrategy for n records`(100_000L)
    }

    @Test
    fun `test benchmark FindByIdCollectionStrategy and FindByIdMapStrategy for 1m records`() {
        `test benchmark FindByIdCollectionStrategy and FindByIdMapStrategy for n records`(1_000_000L)
    }

    private fun `test benchmark FindByIdCollectionStrategy and FindByIdMapStrategy for n records`(n: Long) {
        val artists = sequence<Artist> {
            repeat(n.toInt()) {
                Artist(it.toLong(), "testArtistName")
            }
        }.toList()
        val findById = measureTimedValue {
            FindByIdCollectionStrategy(artists)
        }.let {
            println("FindByIdCollectionStrategy create $n - ${it.duration.inWholeMilliseconds} ms.")
            it.value
        }
        val findByIdWithMap = measureTimedValue {
            FindByIdMapStrategy(artists)
        }.let {
            println("FindByIdMapStrategy create $n - ${it.duration.inWholeMilliseconds} ms.")
            it.value
        }
        println("FindByIdCollectionStrategy access $n - ${measureFindById(findById, 0..n).inWholeMilliseconds} ms.")
        println("FindByIdMapStrategy access $n - ${measureFindById(findByIdWithMap, 0..n).inWholeMilliseconds} ms.")
    }

    private fun <T : HasId> measureFindById(
        findById: FindById<T>,
        range: LongRange,
        times: Int = 10
    ): Duration = measureTime {
        repeat(times) {
            findById.findById(range.random())
        }
    }
}