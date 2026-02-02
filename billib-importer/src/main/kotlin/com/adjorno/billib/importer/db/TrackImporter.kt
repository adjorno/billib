package com.adjorno.billib.importer.db

import com.adjorno.billib.importer.util.ProgressTracker
import java.sql.Connection

data class TrackKey(val title: String, val artistName: String)

class TrackImporter(
    private val connection: Connection,
    private val progress: ProgressTracker,
    private val batchSize: Int = 5000
) {

    fun importTracks(
        tracks: Set<TrackKey>,
        artistMap: Map<String, Long>
    ): Map<TrackKey, Long> {
        progress.log("Importing ${tracks.size} unique tracks...")

        val trackMap = mutableMapOf<TrackKey, Long>()

        // Batch insert tracks with denormalized artist_name
        val insertSql = """
            INSERT INTO TRACK (TITLE, ARTIST_ID, ARTIST_NAME)
            VALUES (?, ?, ?)
            ON CONFLICT (TITLE, ARTIST_ID) DO NOTHING
        """.trimIndent()

        connection.prepareStatement(insertSql).use { stmt ->
            var count = 0
            tracks.forEach { trackKey ->
                val artistId = artistMap[trackKey.artistName]
                if (artistId != null) {
                    stmt.setString(1, trackKey.title.trim())
                    stmt.setLong(2, artistId)
                    stmt.setString(3, trackKey.artistName.trim())
                    stmt.addBatch()
                    count++

                    if (count % batchSize == 0) {
                        stmt.executeBatch()
                        connection.commit()
                        progress.track("Tracks", count, tracks.size)
                    }
                } else {
                    progress.log("WARNING: Artist not found for track: ${trackKey.title} - ${trackKey.artistName}")
                }
            }

            // Execute remaining batch
            if (count % batchSize != 0) {
                stmt.executeBatch()
                connection.commit()
            }
            progress.track("Tracks", tracks.size, tracks.size)
        }

        // Read back track IDs
        progress.log("Reading track IDs...")
        val selectSql = "SELECT _id, TITLE, ARTIST_NAME FROM TRACK"
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery(selectSql)
            while (rs.next()) {
                val key = TrackKey(rs.getString("TITLE"), rs.getString("ARTIST_NAME"))
                trackMap[key] = rs.getLong("_id")
            }
        }

        progress.log("Imported ${trackMap.size} tracks")
        return trackMap
    }
}
