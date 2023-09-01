package com.m14n.billib.data.dao.collections

import com.m14n.billib.data.artist.Artist
import com.m14n.billib.data.dao.FindByName
import com.m14n.billib.data.dao.HasId
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@ExperimentalTime
class CollectionsFindByNameTest {

    @Test
    fun `test benchmark FindByNameCollectionStrategy and FindByNameMapStrategy for 100k records`() {
        `test benchmark FindByNameCollectionStrategy and FindByNameMapStrategy for n records`(100_000L)
    }

    @Test
    fun `test benchmark FindByNameCollectionStrategy and FindByNameMapStrategy for 1m records`() {
        `test benchmark FindByNameCollectionStrategy and FindByNameMapStrategy for n records`(1_000_000L)
    }

    private fun `test benchmark FindByNameCollectionStrategy and FindByNameMapStrategy for n records`(n: Long) {
        val artists = sequence<Artist> {
            repeat(n.toInt()) {
                Artist(it.toLong(), it.toString())
            }
        }.toList()
        val findById = measureTimedValue {
            FindByNameCollectionStrategy(artists)
        }.let {
            println("FindByNameCollectionStrategy create $n - ${it.duration.inWholeMilliseconds} ms.")
            it.value
        }
        val findByIdWithMap = measureTimedValue {
            FindByNameMapStrategy(artists)
        }.let {
            println("FindByNameMapStrategy create $n - ${it.duration.inWholeMilliseconds} ms.")
            it.value
        }
        println("FindByNameCollectionStrategy access $n - ${measureFindByName(findById, 0..n).inWholeMilliseconds} ms.")
        println("FindByNameMapStrategy access $n - ${measureFindByName(findByIdWithMap, 0..n).inWholeMilliseconds} ms.")
    }

    private fun <T : HasId> measureFindByName(
        findById: FindByName<T>,
        range: LongRange,
        times: Int = 10
    ): Duration = measureTime {
        repeat(times) {
            findById.findByName(range.random().toString())
        }
    }
}