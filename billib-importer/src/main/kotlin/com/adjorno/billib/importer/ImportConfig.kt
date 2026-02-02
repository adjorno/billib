package com.adjorno.billib.importer

data class ImportConfig(
    val dataPath: String,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val metadataFile: String = "metadata_billboard.json",
    val batchSize: Int = 5000,
    val enableProgressTracking: Boolean = true
)
