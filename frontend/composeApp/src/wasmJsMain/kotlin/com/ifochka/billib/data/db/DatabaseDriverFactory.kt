package com.ifochka.billib.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.ifochka.billib.db.BillibDatabase
import org.w3c.dom.Worker
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => new Worker(new URL('@cashapp/sqldelight-sqljs-worker/sqljs.worker.js', import.meta.url))")
private external fun createWorker(): Worker

actual fun createDatabaseDriver(): SqlDriver {
    return WebWorkerDriver(createWorker()).also {
        BillibDatabase.Schema.create(it)
    }
}
