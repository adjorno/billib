package com.ifochka.m14n.data.db

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.ifochka.m14n.db.BillibDatabase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual fun createDatabaseDriver(): SqlDriver {
    val context = DatabaseDriverContext.context
    val driver = AndroidSqliteDriver(BillibDatabase.Schema.synchronous(), context, "billib.db")
    return driver
}

object DatabaseDriverContext : KoinComponent {
    val context: Context by inject()
}
