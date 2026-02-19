package com.ifochka.m14n.data.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement

expect fun createDatabaseDriver(): SqlDriver

internal class NoOpSqlDriver : SqlDriver {
    override fun close() = Unit

    override fun currentTransaction(): Transacter.Transaction? = null

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> = QueryResult.Value(0L)

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> = mapper(NoOpCursor())

    override fun newTransaction(): QueryResult<Transacter.Transaction> =
        QueryResult.Value(
            object : Transacter.Transaction() {
                override val enclosingTransaction: Transacter.Transaction? = null

                override fun endTransaction(successful: Boolean): QueryResult<Unit> = QueryResult.Unit
            },
        )

    override fun addListener(
        vararg queryKeys: String,
        listener: Query.Listener,
    ) = Unit

    override fun removeListener(
        vararg queryKeys: String,
        listener: Query.Listener,
    ) = Unit

    override fun notifyListeners(vararg queryKeys: String) = Unit
}

internal class NoOpCursor : SqlCursor {
    override fun getBytes(index: Int): ByteArray? = null

    override fun getDouble(index: Int): Double? = null

    override fun getLong(index: Int): Long? = null

    override fun getString(index: Int): String? = null

    override fun getBoolean(index: Int): Boolean? = null

    override fun next(): QueryResult<Boolean> = QueryResult.Value(false)
}
