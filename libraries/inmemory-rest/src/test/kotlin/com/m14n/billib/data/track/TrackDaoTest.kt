package com.m14n.billib.data.track

import com.m14n.billib.data.artist.Artist
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

class TrackDaoTest {

    private val dataSource = h2InMemoryDataSource()
    private lateinit var trackDao: TrackDao

    private val drakeArtist = Artist(1, "Drake")
    private val queenArtist = Artist(2, "Queen")

    @Before
    fun `init database`() {
        dataSource.execute(
            """
            INSERT INTO ARTIST VALUES(1, 'Drake'),(2, 'Queen');
            INSERT INTO TRACK VALUES (1, 1, 'Hotline Bling'),
            (2, 2, 'Bohemian Rhapsody'),
            (3, 2, 'We Will Rock You');
            """
        )
        trackDao = SqlDataSourceReader()
            .read(dataSource)
            .trackDao
    }

    @After
    fun `drop database`() {
        dataSource.drop()
    }

    @Test
    fun `findAll should return all tracks`() {
        trackDao.findAll() `should contain same`
                listOf(
                    Track(1, "Hotline Bling", drakeArtist),
                    Track(2, "Bohemian Rhapsody", queenArtist),
                    Track(3, "We Will Rock You", queenArtist)
                )
    }

    @Test
    fun `findById should return existing track if id exists`() {
        trackDao.findById(1) `should be equal to` Track(1, "Hotline Bling", drakeArtist)
    }

    @Test
    fun `findById should return null track if id does not exist`() {
        trackDao.findById(4).`should be null`()
    }

    @Test
    fun `findByArtistAndTitle should return existing track if artist and title exists`() {
        trackDao.findByArtistAndTitle(queenArtist, "We Will Rock You") `should be equal to` Track(
            3,
            "We Will Rock You",
            queenArtist
        )
    }

    @Test
    fun `findByArtistAndTitle should return null if artist does not exist`() {
        trackDao.findByArtistAndTitle(Artist(3, "Pendulum"), "Another Planet").`should be null`()
    }

    @Test
    fun `findByArtistAndTitle should return null if title does not exist`() {
        trackDao.findByArtistAndTitle(queenArtist, "Back To Black").`should be null`()
    }

}
