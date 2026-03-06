package com.ifochka.m14n.data.db

import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ifochka.m14n.db.M14nDatabase

actual fun createDatabaseDriver(): SqlDriver =
    NativeSqliteDriver(
        schema = M14nDatabase.Schema.synchronous(),
        name = "m14n.db",
    )
