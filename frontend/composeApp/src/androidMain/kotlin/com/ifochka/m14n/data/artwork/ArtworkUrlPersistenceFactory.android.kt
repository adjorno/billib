package com.ifochka.m14n.data.artwork

import com.ifochka.m14n.data.db.ChartDatabaseRepository

actual fun createArtworkUrlPersistence(database: ChartDatabaseRepository): ArtworkUrlPersistence {
    println("[ARTWORK-PERSISTENCE-FACTORY] Creating SqlDelightArtworkUrlPersistence for Android")
    return SqlDelightArtworkUrlPersistence(database)
}
