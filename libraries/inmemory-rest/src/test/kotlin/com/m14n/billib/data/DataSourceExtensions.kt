package com.m14n.billib.data

import javax.sql.DataSource

fun DataSource.execute(sql: String) {
    val statement = connection.createStatement()
    statement.execute(sql)
    statement.close()
}

fun DataSource.drop() = execute("DROP ALL OBJECTS;")
