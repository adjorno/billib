package com.m14n.billib.data.chart

import com.m14n.billib.data.billboard.date
import com.m14n.billib.data.drop
import com.m14n.billib.data.execute
import com.m14n.billib.data.journal.Journal
import com.m14n.billib.data.reader.h2InMemoryDataSource
import com.m14n.billib.reader.SqlDataSourceReader
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should contain same`
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*

class ChartDaoTest {

    private val dataSource = h2InMemoryDataSource()
    private lateinit var chartDao: ChartDao

    private val billboardJournal = Journal(1, "Billboard")

    init {
        billboardJournal.charts = listOf(Chart(1, "", billboardJournal, 0, Date(), null))
    }

    @Before
    fun `init database`() {
        dataSource.execute(
            """
            |INSERT INTO JOURNAL VALUES (1,'Billboard');
            |INSERT INTO CHART VALUES (1,'Hot 100',1,100,'1958-08-09',NULL),
            |(2,'Country',1,50,'1962-01-06','2020-03-28'),
            |(3,'Hip-Hop',1,50,'1962-01-06',NULL);
            """.trimMargin()
        )
        chartDao = SqlDataSourceReader()
            .read(dataSource)
            .chartDao
    }

    @After
    fun `drop database`() {
        dataSource.drop()
    }

    @Test
    fun `findAll should return all journals`() {
        chartDao.findAll() `should contain same` listOf(
            Chart(1, "Hot 100", billboardJournal, 100, "1958-08-09".date),
            Chart(
                2,
                "Country",
                billboardJournal,
                50,
                "1962-01-06".date,
                "2020-03-28".date
            ),
            Chart(3, "Hip-Hop", billboardJournal, 50, "1962-01-06".date)
        )
    }

    @Test
    fun `findById should return existing chart if id exists`() {
        chartDao.findById(1) `should be equal to` Chart(
            1,
            "Hot 100",
            billboardJournal,
            100,
            "1958-08-09".date
        )
    }

    @Test
    fun `findById should return null if id does not exist`() {
        chartDao.findById(4).`should be null`()
    }

    @Test
    fun `findByName should return existing chart if name exists`() {
        chartDao.findByName("Country") `should be equal to` Chart(
            2,
            "Country",
            billboardJournal,
            50,
            "1962-01-06".date,
            "2020-03-28".date
        )
    }

    @Test
    fun `findByName should return null if name does not exist`() {
        chartDao.findByName("Rock").`should be null`()
    }
}