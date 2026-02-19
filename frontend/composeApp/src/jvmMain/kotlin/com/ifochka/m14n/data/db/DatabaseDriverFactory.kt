package com.ifochka.m14n.data.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ifochka.m14n.db.M14nDatabase
import java.io.File

actual fun createDatabaseDriver(): SqlDriver {
    val databaseDir = File(System.getProperty("user.home"), ".m14n")
    if (!databaseDir.exists()) {
        databaseDir.mkdirs()
    }
    val databasePath = File(databaseDir, "m14n.db")

    // DEV MODE: Delete database on startup to handle schema changes
    // TODO: Remove for production and implement proper migrations
//    if (databasePath.exists()) {
//        println("[DB] 🗑️  Deleting old database (dev mode)")
//        databasePath.delete()
//    }

    // Create fresh database
    val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
    M14nDatabase.Schema.synchronous().create(driver)

    return driver
}
