package com.ifochka.billib.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.ifochka.billib.db.BillibDatabase
import java.io.File

actual fun createDatabaseDriver(): SqlDriver {
    val databaseDir = File(System.getProperty("user.home"), ".billib")
    if (!databaseDir.exists()) {
        databaseDir.mkdirs()
    }
    val databasePath = File(databaseDir, "billib.db")
    val driver = JdbcSqliteDriver("jdbc:sqlite:${databasePath.absolutePath}")
    BillibDatabase.Schema.create(driver)
    return driver
}
