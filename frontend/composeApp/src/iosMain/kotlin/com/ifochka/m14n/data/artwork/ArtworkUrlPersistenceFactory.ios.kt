package com.ifochka.m14n.data.artwork

import com.ifochka.m14n.data.db.ChartDatabaseRepository

actual fun createArtworkUrlPersistence(database: ChartDatabaseRepository): ArtworkUrlPersistence =
    SqlDelightArtworkUrlPersistence(database)
