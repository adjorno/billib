package com.m14n.billib.data.artist

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

class ArtistDaoTest {

    private val dataSource = h2InMemoryDataSource()
    private lateinit var artistDao: ArtistDao

    @Before
    fun `init database`() {
        dataSource.execute("INSERT INTO ARTIST VALUES(1, 'Metallica'), (2, 'Disclosure'), (300, 'Mozart')")
        artistDao = SqlDataSourceReader()
            .read(dataSource)
            .artistDao
    }

    @After
    fun `drop database`() {
        dataSource.drop()
    }

    @Test
    fun `findAll should return all artists`() {
        artistDao
            .findAll() `should contain same`
                listOf(
                    Artist(1, "Metallica"),
                    Artist(2, "Disclosure"),
                    Artist(300, "Mozart")
                )
    }

    @Test
    fun `findById should return existing artist if id exists`() {
        artistDao
            .findById(1) `should be equal to` Artist(1, "Metallica")
    }

    @Test
    fun `findById should return null artist if id does not exist`() {
        artistDao
            .findById(3).`should be null`()
    }

    @Test
    fun `findByName should return existing artist if name exists`() {
        artistDao
            .findByName("Mozart") `should be equal to` Artist(300, "Mozart")
    }

    @Test
    fun `findByName should return null if name does not exist`() {
        artistDao
            .findByName("Pendulum").`should be null`()
    }

}
