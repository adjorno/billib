package com.adjorno.billib.importer.util

import com.adjorno.billib.importer.ImportConfig
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DatabaseConnection {

    fun create(config: ImportConfig): Connection {
        try {
            Class.forName("org.postgresql.Driver")
            println("Connecting to: ${config.dbUrl}")
            println("User: ${config.dbUser}")
            val connection = DriverManager.getConnection(
                config.dbUrl,
                config.dbUser,
                config.dbPassword
            )
            connection.autoCommit = false // Use manual transactions for performance
            return connection
        } catch (e: SQLException) {
            System.err.println("SQL Exception connecting to database:")
            System.err.println("  URL: ${config.dbUrl}")
            System.err.println("  User: ${config.dbUser}")
            System.err.println("  Error: ${e.message}")
            System.err.println("  SQLState: ${e.sqlState}")
            e.printStackTrace()
            throw RuntimeException("Failed to connect to database: ${e.message}", e)
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("PostgreSQL JDBC driver not found", e)
        }
    }

    fun testConnection(config: ImportConfig): Boolean {
        return try {
            create(config).use { conn ->
                val stmt = conn.createStatement()
                val rs = stmt.executeQuery("SELECT 1")
                rs.next() && rs.getInt(1) == 1
            }
        } catch (e: Exception) {
            false
        }
    }
}
