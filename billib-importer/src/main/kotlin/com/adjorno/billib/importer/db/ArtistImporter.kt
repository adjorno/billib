package com.adjorno.billib.importer.db

import com.adjorno.billib.importer.util.ProgressTracker
import java.sql.Connection

class ArtistImporter(
    private val connection: Connection,
    private val progress: ProgressTracker,
    private val batchSize: Int = 5000
) {

    fun importArtists(artistNames: Set<String>): Map<String, Long> {
        progress.log("Importing ${artistNames.size} unique artists...")

        val artistMap = mutableMapOf<String, Long>()

        // Batch insert artists
        val insertSql = "INSERT INTO ARTIST (NAME) VALUES (?) ON CONFLICT (NAME) DO NOTHING"
        connection.prepareStatement(insertSql).use { stmt ->
            var count = 0
            artistNames.forEach { artistName ->
                stmt.setString(1, artistName.trim())
                stmt.addBatch()
                count++

                if (count % batchSize == 0) {
                    stmt.executeBatch()
                    connection.commit()
                    progress.track("Artists", count, artistNames.size)
                }
            }

            // Execute remaining batch
            if (count % batchSize != 0) {
                stmt.executeBatch()
                connection.commit()
            }
            progress.track("Artists", artistNames.size, artistNames.size)
        }

        // Read back artist IDs
        progress.log("Reading artist IDs...")
        val selectSql = "SELECT _id, NAME FROM ARTIST"
        connection.createStatement().use { stmt ->
            val rs = stmt.executeQuery(selectSql)
            while (rs.next()) {
                artistMap[rs.getString("NAME")] = rs.getLong("_id")
            }
        }

        progress.log("Imported ${artistMap.size} artists")
        return artistMap
    }
}
