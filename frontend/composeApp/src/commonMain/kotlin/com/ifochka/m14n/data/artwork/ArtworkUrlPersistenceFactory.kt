package com.ifochka.m14n.data.artwork

import com.ifochka.m14n.data.db.ChartDatabaseRepository

expect fun createArtworkUrlPersistence(database: ChartDatabaseRepository): ArtworkUrlPersistence
