package com.ifochka.m14n.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.ifochka.m14n.db.BillibDatabase
import org.w3c.dom.Worker

private var initializedDriver: SqlDriver? = null

internal suspend fun initializeDatabaseDriver() {
    val driver = WebWorkerDriver(jsWorker())
    BillibDatabase.Schema.create(driver).await()
    initializedDriver = driver
}

actual fun createDatabaseDriver(): SqlDriver =
    checkNotNull(initializedDriver) { "Driver not initialized. Call initializeDatabaseDriver() first." }

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsWorker(): Worker =
    js("""new Worker(new URL("@cashapp/sqldelight-sqljs-worker/sqljs.worker.js", import.meta.url))""")
