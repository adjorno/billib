package com.adjorno.billib.importer

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    println("""
        ╔═══════════════════════════════════════════════════════════╗
        ║         Billboard Charts PostgreSQL Importer             ║
        ╚═══════════════════════════════════════════════════════════╝
    """.trimIndent())

    try {
        val config = parseArgs(args)
        val importer = BillboardImporter(config)
        val startTime = System.currentTimeMillis()

        importer.import()

        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60

        println("\n✅ Import completed successfully in ${minutes}m ${seconds}s")
        exitProcess(0)

    } catch (e: Exception) {
        println("\n❌ Import failed: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}

private fun parseArgs(args: Array<String>): ImportConfig {
    val argMap = mutableMapOf<String, String>()

    var i = 0
    while (i < args.size) {
        val arg = args[i]
        if (arg.startsWith("--")) {
            // Handle --key=value format
            if (arg.contains("=")) {
                val parts = arg.substring(2).split("=", limit = 2)
                argMap[parts[0]] = parts.getOrElse(1) { "true" }
            } else {
                // Handle --key value format
                val key = arg.substring(2)
                val value = if (i + 1 < args.size && !args[i + 1].startsWith("--")) {
                    args[++i]
                } else {
                    "true"
                }
                argMap[key] = value
            }
        }
        i++
    }

    // Default values
    val dataPath = argMap["data-path"] ?: "/Users/adjorno/Developer/Sources/billibdata/data"
    val dbUrl = argMap["db-url"] ?: "jdbc:postgresql://localhost:5432/billibdb"
    val dbUser = argMap["db-user"] ?: "postgres"
    val dbPassword = argMap["db-password"] ?: "postgres"
    val batchSize = argMap["batch-size"]?.toIntOrNull() ?: 5000

    if (argMap.containsKey("help") || argMap.containsKey("h")) {
        printHelp()
        exitProcess(0)
    }

    return ImportConfig(
        dataPath = dataPath,
        dbUrl = dbUrl,
        dbUser = dbUser,
        dbPassword = dbPassword,
        batchSize = batchSize
    )
}

private fun printHelp() {
    println("""
        Usage: java -jar billib-importer.jar [options]

        Options:
          --data-path <path>       Path to JSON data directory
                                   Default: /Users/adjorno/Developer/Sources/billibdata/data

          --db-url <url>           PostgreSQL JDBC URL
                                   Default: jdbc:postgresql://localhost:5432/billibdb

          --db-user <user>         Database username
                                   Default: postgres

          --db-password <pass>     Database password
                                   Default: postgres

          --batch-size <size>      Batch size for inserts
                                   Default: 5000

          --help, -h               Show this help message

        Examples:
          # Import with defaults
          java -jar billib-importer.jar

          # Import with custom database
          java -jar billib-importer.jar \\
            --db-url jdbc:postgresql://db.example.com:5432/billib \\
            --db-user myuser \\
            --db-password mypass

          # Import with custom data path
          java -jar billib-importer.jar \\
            --data-path /path/to/json/data

    """.trimIndent())
}
