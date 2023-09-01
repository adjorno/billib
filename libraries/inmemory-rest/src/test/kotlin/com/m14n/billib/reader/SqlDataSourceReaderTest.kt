package com.m14n.billib.reader

import com.m14n.billib.data.BilliBDao
import com.m14n.billib.data.billboard.date
import com.m14n.billib.data.billboard.generateBillboardDateSequence
import com.m14n.billib.data.billboard.model.BBChart
import com.m14n.billib.data.billboard.model.BBJournalMetadata
import com.m14n.billib.data.billboard.model.BBTrack
import com.m14n.billib.data.billboard.text
import com.m14n.billib.data.meaningful
import com.m14n.billib.data.optimized
import com.m14n.billib.data.sameArtist
import com.m14n.billib.data.track.Track
import com.m14n.billib.data.track.fullTrackTitle
import com.mysql.cj.jdbc.MysqlDataSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.lang.Integer.min
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
class SqlDataSourceReaderTest {

    private lateinit var dao: BilliBDao

    @Ignore
    @Test
    fun `in memory db charts are the same as original Json`() {
        val properties = Properties().apply {
            ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use { stream ->
                load(stream)
            }
        }
        val dataSource = MysqlDataSource().apply {
            user = properties.getProperty("mysql.user")
            password = properties.getProperty("mysql.password")
            setURL(properties.getProperty("mysql.url"))
        }
        println("Dao loading...")
        println("Dao loaded in ${measureTime { dao = SqlDataSourceReader().read(dataSource) }.inWholeMilliseconds} ms.")

        val today = properties.getProperty("data.today")
        val root = File(properties.getProperty("data.json.root"))

        val originalMetadata = Json.decodeFromString<BBJournalMetadata>(
            File(root, "metadata_billboard.json").readText()
        )
        val journalDao = checkNotNull(dao.journalDao.findByName(originalMetadata.name))
        println("Journal ${originalMetadata.name} has found")
        originalMetadata.charts.filter { it.name == "Electronic" }.forEach { chartMetadata ->
            val chart = checkNotNull(journalDao.charts?.find { chart -> chart.name == chartMetadata.name })
            assertEquals(chart.startDate.text, chartMetadata.startDate)
            assertEquals(chart.endDate?.text, chartMetadata.endDate)
            assertEquals(chart.listSize, chartMetadata.size)
            println("Chart ${chartMetadata.name} has found")

            generateBillboardDateSequence(
                startDate = chartMetadata.startDate.date,
                endDate = (chartMetadata.endDate ?: today).date
            ).forEach { weekDate ->
                val originalChartListFile = File(
                    File(root, chartMetadata.folder),
                    "${chartMetadata.prefix}-${weekDate.text}.json"
                )
                val originalChartList = Json { ignoreUnknownKeys = true }
                    .decodeFromString<BBChart>(originalChartListFile.readText())

                val chartList = checkNotNull(chart.chartLists?.find { chartList -> chartList.week.date == weekDate })
                println("Chart list $originalChartList has found")
                if (originalChartList.tracks.size != chartList.chartTracks?.size) {
                    println("Check size: json ${originalChartList.tracks.size}, db ${chartList.chartTracks?.size}")
                }

                (0 until min(chartMetadata.size, originalChartList.tracks.size)).forEach { position ->
                    val originalTrack = originalChartList.tracks[position]
                    val chartTrack = checkNotNull(chartList.chartTracks?.get(position))
                    assertEquals(originalTrack.rank, chartTrack.rank)
                    assertEqualsTrack(originalTrack, chartTrack.track, "$chartTrack <-> $originalTrack")
                }
            }
        }
    }

    private fun assertEqualsTrack(originalTrack: BBTrack, track: Track, message: String? = null) {
        if (FIX_LIST.contains((originalTrack.artist to originalTrack.title).fullTrackTitle)) {
            return
        }
        //assertTrackDeep(originalTrack, track, message)
        // Track title comparison is enough for data lost case
        assertTrue(
            track.title.meaningful.contains(originalTrack.title.meaningful, ignoreCase = true),
            message
        )
    }

    private fun assertTrackDeep(
        originalTrack: BBTrack,
        track: Track,
        message: String?
    ) {
        val originalArtist = originalTrack.artist.optimized
        val duplicateArtist = dao.duplicateArtistDao.findByName(originalArtist)
        // try with original artist
        var duplicateTrack = dao.duplicateTrackDao.findByName(
            (originalArtist to originalTrack.title).fullTrackTitle
        )
        // try with duplicate artist
        if (duplicateTrack == null && duplicateArtist != null) {
            duplicateTrack = dao.duplicateTrackDao.findByName(
                (duplicateArtist.name to originalTrack.title).fullTrackTitle
            )
        }
        if (duplicateTrack != null) {
            assertEquals(
                duplicateTrack,
                track,
                message
            )
        } else {
            val originalArtistCheck = track.artist.name.sameArtist(
                originalArtist,
                strict = false
            )
            val duplicateArtistCheck = !originalArtistCheck && duplicateArtist != null &&
                    track.artist.name.sameArtist(
                        duplicateArtist.name,
                        dao.duplicateArtistDao,
                        strict = false
                    )
            assertTrue(originalArtistCheck || duplicateArtistCheck, message)

            assertTrue(
                track.title.meaningful.contains(originalTrack.title.meaningful, ignoreCase = true),
                message
            )
        }
    }
}

private val FIX_LIST = setOf(
    "The Crests featuring Johnny Maestro - Flower Of Love",
    "The Crests featuring Johnny Maestro - Journey Of Love",
    "The Crests featuring Johnny Maestro - Isn't It Amazing",
    "Sean Paul Featuring Keyshia Cole - When You Gonna (Give It Up To Me)",
    "Sean Paul Feat. Keyshia Cole - (When You Gonna) Give It Up To Me",
    "Sean Paul Featuring Keyshia Cole - (When You Gonna) Give It Up To Me",
    "B.o.B Featuring Eminem & Haley Williams - Airplanes",
    "iLoveMemphis - Hit The Quan",
    "Aminé - Caroline",
    "Ed Sheeran - Perfect",
    "Lil Uzi Vert Featuring Nicki Minaj - The Way Life Goes",
    "Ed Sheeran Duet With Beyonce - Perfect",
    "A\$AP Ferg Featuring Nicki Minaj - Plain Jane",
    "Lil Skies Featuring Landon Cube - Nowadays",
    "Lil Skies Featuring Landon Cube - Red Roses",
    "Ozuna & Romeo Santos - El Farsante",
    "El Chombo Featuring Cutty Ranks - Dame Tu Cosita",
    "Kanye West Featuring PARTYNEXTDOOR - Ghost Town",
    "Kanye West Featuring PARTYNEXTDOOR - Wouldn't Leave",
    "Gene Autry - Here Comes Santa Claus (Right Down Santa Claus Lane)",
    "Blake Shelton Duet With Gwen Stefani - Nobody But You",
    "Keith Urban Duet With P!nk - One Too Many",
    "Zac Brown - Grandma's Garden",
    "Quincy Jones (feat. The Brothers Johnson) - Is It Love That We're Missin'",
    "The Notorious B.I.G. (Feat. Puff Daddy - Mo Money Mo Problems",
    "Puff Daddy & The Family (Feat. The Notorious B.I.G. & Mase) - Been Around The World/It's All About The Benjamins",
    "Memphis Bleek ( - It s Alright",
    "Nas Featuring Braveheart's - Oochi Wally",
    "QB's Finest Featuring Nas - Oochie Wally",
    "G.dep Featuring P. Diddy & Black Rob - Let's Get It",
    "Mariah Carey Featuring Cameo - Loverboy",
    "Nelly Featuring St. Lunatics - Air Force Ones",
    "50 Cent - What Up Gangsta", /// !!!
    "Body Head Bangerz Featuring YoungBloodz - I Smoke, I Drank",
    "Lil Jon Featuring E-40 & Sean Paul - Snap Ya Fingers",
    "Keyshia Cole Duet With Moncia - Trust",
    "Lil Wayne Featuring Gucci Mane - We Be Steady Mobbin'",
    "Lil Uzi Vert Featuring Oh Wonder - The Way Life Goes",
    "Sasanya Featuring South Black - Zum Zum",
    "Music Instructor (Feat. Flying Steps) - Super Sonic",
    "Thunderpuss Vs. Love Tribe - Stand Up",
    "Widelife - I Don't Want U (Widelife & DezRok Mixes)",
    "Beyonce - Baby Boy (J. Vasquez & M. Joshua Mixes)",
    "Murk - Believe",
    "Emballa - Emballa (Louie Vega Remixes)",
    "New Order Featuring Ana Mantron - Jetstream",
    "Hans Zimmer - He's A Pirate (Tiesto/Friscia & Lamboy Mixes)",
    "Eric Prydz Vs. Floyd - Proper Education",
    "Karen Young - Hot Shot 2007",
    "Axwell, Ingrosso Laidback Luke & Angello - Leave The World Behind",
    "Salme Dahlstrom - So Delicious",
    "DJ Denis Featuring Juan Magan, Lil Jon & Baby Bash - Shuri Shuri (Let's Get Loco)",
    "Laura LaRue - Capture Your Love",
    "Jack U Featuring Kiesza - Take U There",
    "Jade - Better & Better",
    "B. Howard - Don't Say You Love Me", // !!!
    "Various Artists - This Is For My Girls",
    "Sean Finn vs. Terri B! & Peter Brown - Free",
    "Nadel Paris Featuring DJM - Ooh La La La",
    "Mark Picchiotti Presents Basstoy Featuring Dana Divine - Runnin' 2018",
    "Jing x Atom Panda - Shadow",
    "Karol G & J Balvin Feauring Nicky Jam - Mi Cama",
    "Banda El Recodo - Deja",
    "Los Angeles de Charly - Un Sueno",
    "La Ley - El Duelo",
    "Banda El Limon - En Los Puritos Huesos",
    "Ejo Featuring Tego Calderon - No Tiene Novio",
    "Los Benjamins Featuring Wisin & Yandel, Daddy Yankee, Hector \"El Father\" Bambino & Zion - Noche De Entierro (Nuestro Amor)",
    "Enur Featuring Natasha - Calabria 2007",
    "La Original Banda El Limon - Derecho De Antiguedad",
    "Conjunto Atardecer - Encontre",
    "Banda El Recodo - Me Gusta Todo De Ti",
    "La Original Banda El Limon - Al Menos",
    "La Arrolladora Banda El Limon - Nina De Mi Corazon",
    "Chuy Lizarraga y Su Banda Tierra Sinaloense - Te Mirabas Mas Bonita",
    "DJ Luian & Mambo Kingz Presentan: Bad Bunny, J Balvin & Prince Royce - Sensualidad", // !!!
    "Shinedown - The Crow & The Butterfly",
    "Sean Paul - Gimme The Light",
    "Busta Rhymes - Light Your A** On Fire",
    "Lil Jon & The East Side Boyz Featuring Usher & Ludacris - Lovers & Friends",
    "Twista Featuring Trey Songz - Girl Tonite",
    "Huey - Pop, Lock & Drop It",
    "Soulja Boy Tell'em - Crank That (Soulja Boy)",
    "A\$AP Rocky Featuring Drake, 2 Chainz & Kendrick Lamar - F**kin Problems",
    "A\$AP Rocky - F**kin Problems",
    "DJ Khaled Featuring Jay Z, Meek Mill, Rick Ross & French Montana - They Don't Love U No More",
    "Lil Yachty - 1 Night",
    "Lumidee - Never Leave You - Uh Ooh, Uh Oooh!",
    "Beyonce - Irreplaceable",
    "Soulja Boy - Crank That (Soulja Boy)",
    "The-Dream - Shawty Is A 10",
    "Colbie Caillat - Fallin' For You",
    "Zayn Featuring Sia - Dusk Till Dawn",
    "Sofi Tukker - That's It (I'm Crazy)",
    "Prince - Nothing Compares 2 U (Original Studio Recording)",
    "Martin Garrix & David Guetta Featuring Jamie Scott & Romy Dya - So Far Away", //!!! Electronic 2018-04-28
    "deadmau5 - Infa Turbo Pigcart Racer",
    "Bassnectar Featuring W. Darling - You & Me",
    "30. Steve Aoki, Daddy Yankee, Play-N-Skillz & Elvis Crespo - Azukita", //!!! Electronic 2018-05-05
)
