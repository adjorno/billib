package com.m14n.billib.data.reader

import com.m14n.billib.data.execute
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource

fun h2InMemoryDataSource(): DataSource {
    val source = JdbcDataSource().apply {
        user = "sa"
        password = "sa"
        setURL("jdbc:h2:mem:billib;DB_CLOSE_DELAY=-1")
    }
    source.execute(
        """
            CREATE TABLE ARTIST(
                _id int(11),
                NAME text,
                PRIMARY KEY(_id)
            );

            CREATE TABLE DUPLICATE_ARTIST(
                DUPLICATE_NAME text,
                ARTIST_ID int(11)
            );

            CREATE TABLE TRACK(
                _id int(11),
                ARTIST_ID int(11),
                TITLE text,
                PRIMARY KEY(_id)
            );

            CREATE TABLE DUPLICATE_TRACK (
                DUPLICATE_TITLE text,
                TRACK_ID int(11)
            );
            
            CREATE TABLE JOURNAL(
                _id int(11),
                NAME text,
                PRIMARY KEY(_id)
            );

            CREATE TABLE CHART(
                _id int(11),
                NAME text,
                JOURNAL_ID int(11),
                LIST_SIZE int(11),
                START_DATE text,
                END_DATE text,
                PRIMARY KEY(_id)
            );

            CREATE TABLE WEEK (
                WEEK_ID int(11),
                DATE text,
                PRIMARY KEY(WEEK_ID)
            );

            CREATE TABLE CHART_LIST (
                _id int(11),
                CHART_ID int(11),
                WEEK_ID int(11),
                NUMBER int(11),
                PREVIOUS_CHART_LIST_ID int(11),
                PRIMARY KEY(_id)
            );

            CREATE TABLE CHART_TRACK (
                _id int(11),
                TRACK_ID int(11),
                CHART_LIST_ID int(11),
                RANK int(11),
                LAST_WEEK_RANK int(11),
                PRIMARY KEY(_id)
            );
        """
    )
    return source
}
