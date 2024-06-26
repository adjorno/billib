package com.adjorno.billib.sql

import java.io.File

fun main(args: Array<String>) {
    val inputSqlPath = args[0]
    val outputSqlPath = args[1]

    File(outputSqlPath).writeText(
            File(inputSqlPath).readText()
                .fixCreateTableCommand()
                .fixBackslashQuotes()
                .fixIntType()
                .removeTableUnLock()
                .removeTableLock())
}

/**
 * Converts `int(11)` type into `int`
 */
private fun String.fixIntType() =
    foreach("int(11)") { builder: StringBuilder, index: Int ->
        val bracketIndex = builder.indexOf("(", index)
        builder.delete(bracketIndex, bracketIndex + 4)
    }

/**
 * Removes post-creation range from `CREATE TABLE` SQL command. For example, the following command:
 * ```
 * CREATE TABLE `ARTIST` (
 * `_id` int(11) NOT NULL AUTO_INCREMENT,
 * `NAME` text,
 * PRIMARY KEY (`_id`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=26719 DEFAULT CHARSET=latin1;
 * ```
 * Will be transformed to:
 * ```
 * CREATE TABLE `ARTIST` (
 * `_id` int(11) NOT NULL AUTO_INCREMENT,
 * `NAME` text,
 * PRIMARY KEY (`_id`)
 * );
 * ```
 */
private fun String.fixCreateTableCommand(): String =
    foreach("ENGINE=InnoDB") { builder: StringBuilder, index: Int ->
        val semicolonIndex = builder.indexOf(";", index)
        builder.delete(index, semicolonIndex)
    }

/**
 * Replaces `\'` with `''` in provided SQL script.
 */
private fun String.fixBackslashQuotes(): String =
    foreach("\\'") { builder: StringBuilder, index: Int ->
        // hack to ignore sequence \\'
        if (builder[index - 1] != '\\') {
            builder[index] = '\''
        }
    }


/**
 * Removes `LOCK TABLES` command from SQL script.
 */
private fun String.removeTableLock(): String =
    foreach("LOCK TABLES") { builder: StringBuilder, index: Int ->
        val semicolonIndex = builder.indexOf(";", index)
        builder.delete(index, semicolonIndex)
    }

/**
 * Removes `UNLOCK TABLES` command from SQL script.
 */
private fun String.removeTableUnLock(): String =
    foreach("UNLOCK TABLES") { builder: StringBuilder, index: Int ->
        val semicolonIndex = builder.indexOf(";", index)
        builder.delete(index, semicolonIndex)
    }

private fun String.foreach(search: String, callback: (builder: StringBuilder, index: Int) -> Unit): String {
    val result = StringBuilder(this)
    var index = result.indexOf(search)
    while (index >= 0) {
        callback(result, index)
        index = result.indexOf(search, index + 1)
    }
    return result.toString()
}
