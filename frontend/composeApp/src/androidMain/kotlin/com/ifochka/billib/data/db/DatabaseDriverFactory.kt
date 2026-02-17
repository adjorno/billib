package com.ifochka.billib.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ifochka.billib.db.BillibDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun createDatabaseDriver(): SqlDriver {
    val context = DatabaseDriverContext.context
    return AndroidSqliteDriver(BillibDatabase.Schema, context, "billib.db")
}

object DatabaseDriverContext : KoinComponent {
    val context: Context by inject()
}
