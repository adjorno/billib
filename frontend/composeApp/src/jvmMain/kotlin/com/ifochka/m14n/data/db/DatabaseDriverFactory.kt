package com.ifochka.m14n.data.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ifochka.m14n.db.BillibDatabase
import java.io.File

actual fun createDatabaseDriver(): SqlDriver {
    val databaseDir = File(System.getProperty("user.home"), ".billib")
    if (!databaseDir.exists()) {
        databaseDir.mkdirs()
    }
    val databasePath = File(databaseDir, "billib.db")

    // DEV MODE: Delete database on startup to handle schema changes
    // TODO: Remove for production and implement proper migrations
//    if (databasePath.exists()) {
//        println("[DB] 🗑️  Deleting old database (dev mode)")
//        databasePath.delete()
//    }

    // Create fresh database
    val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
    BillibDatabase.Schema.synchronous().create(driver)

    return driver
}
