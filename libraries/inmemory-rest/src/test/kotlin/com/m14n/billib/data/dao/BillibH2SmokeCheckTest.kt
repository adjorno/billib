package com.m14n.billib.data.dao

import com.m14n.billib.data.drop
import com.m14n.billib.data.reader.h2InMemoryDataSource
import com.m14n.billib.reader.SqlDataSourceReader
import org.junit.After
import org.junit.Test

class BillibH2SmokeCheckTest {

    private val dataSource = h2InMemoryDataSource()

    @Test
    fun `init database`() {
        SqlDataSourceReader()
            .read(dataSource)
    }

    @After
    fun `drop database`() {
        dataSource.drop()
    }

}