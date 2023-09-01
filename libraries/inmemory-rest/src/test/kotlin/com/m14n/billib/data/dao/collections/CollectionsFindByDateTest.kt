package com.m14n.billib.data.dao.collections

import com.m14n.billib.data.dao.FindByDate
import com.m14n.billib.data.dao.HasId
import com.m14n.billib.data.week.Week
import org.junit.Test
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@ExperimentalTime
class CollectionsFindByDateTest {

    @Test
    fun `test benchmark FindByDateCollectionStrategy and FindByDateMapStrategy for 100k records`() {
        `test benchmark FindByDateCollectionStrategy and FindByDateMapStrategy for n records`(100_000)
    }

    @Test
    fun `test benchmark FindByDateCollectionStrategy and FindByDateMapStrategy for 1m records`() {
        `test benchmark FindByDateCollectionStrategy and FindByDateMapStrategy for n records`(1_000_000)
    }

    private fun `test benchmark FindByDateCollectionStrategy and FindByDateMapStrategy for n records`(n: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = 0
        }
        val weeks = sequence<Week> {
            repeat(n) {
                calendar.add(Calendar.DATE, 1)
                Week(it.toLong(), calendar.time)
            }
        }.toList()
        val findById = measureTimedValue {
            FindByDateCollectionStrategy(weeks)
        }.let {
            println("FindByDateCollectionStrategy create $n - ${it.duration.inWholeMilliseconds} ms.")
            it.value
        }
        val findByIdWithMap = measureTimedValue {
            FindByDateMapStrategy(weeks)
        }.let {
            println("FindByDateMapStrategy create $n - ${it.duration.inWholeMilliseconds} ms.")
            it.value
        }
        println("FindByDateCollectionStrategy access $n - ${measureFindByDate(findById, 0..n).inWholeMilliseconds} ms.")
        println("FindByDateMapStrategy access $n - ${measureFindByDate(findByIdWithMap, 0..n).inWholeMilliseconds} ms.")
    }

    private fun <T : HasId> measureFindByDate(
        findById: FindByDate<T>,
        range: IntRange,
        times: Int = 10
    ): Duration = measureTime {
        repeat(times) {
            findById.findByDate(Calendar.getInstance().apply {
                timeInMillis = 0
                add(Calendar.DATE, range.random())
            }.time)
        }
    }
}