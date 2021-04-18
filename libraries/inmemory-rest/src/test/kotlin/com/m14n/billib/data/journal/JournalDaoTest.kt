package com.m14n.billib.data.journal

import com.m14n.billib.data.billboard.date
import com.m14n.billib.data.chart.Chart
import com.m14n.billib.data.drop
import com.m14n.billib.data.execute
import com.m14n.billib.data.reader.h2InMemoryDataSource
import com.m14n.billib.reader.SqlDataSourceReader
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should contain same`
import org.junit.After
import org.junit.Before
import org.junit.Test

class JournalDaoTest {

    private val dataSource = h2InMemoryDataSource()
    private lateinit var journalDao: JournalDao

    @Before
    fun `init database`() {
        dataSource.execute(
            """
            |INSERT INTO JOURNAL VALUES(1, 'BillBoard'), (2, 'Rolling Stone');
            |INSERT INTO CHART VALUES (1,'Hot 100',1,100,'1958-08-09',NULL);
            """.trimMargin()
        )
        journalDao = SqlDataSourceReader()
            .read(dataSource)
            .journalDao
    }

    @After
    fun `drop database`() {
        dataSource.drop()
    }

    @Test
    fun `findAll should return all journals`() {
        journalDao.findAll() `should contain same` listOf(
            Journal(
                1, "BillBoard",
                listOf(
                    Chart(1, "Hot 100", Journal(1, ""), 100, "1958-08-09".date)
                )
            ),
            Journal(2, "Rolling Stone")
        )
    }

    @Test
    fun `findById should return existing journal if id exists`() {
        journalDao
            .findById(1) `should be equal to` Journal(
            1, "BillBoard",
            listOf(
                Chart(1, "Hot 100", Journal(1, ""), 100, "1958-08-09".date)
            )
        )
    }

    @Test
    fun `findById should return null if id does not exist`() {
        journalDao.findById(3).`should be null`()
    }

    @Test
    fun `findByName should return existing journal if name exists`() {
        journalDao.findByName("Rolling Stone") `should be equal to` Journal(2, "Rolling Stone")
    }

    @Test
    fun `findByName should return null if name does not exist`() {
        journalDao.findByName("UK-100").`should be null`()
    }

}