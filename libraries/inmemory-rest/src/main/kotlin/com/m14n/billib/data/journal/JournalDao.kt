package com.m14n.billib.data.journal

import com.m14n.billib.data.dao.Dao
import com.m14n.billib.data.dao.FindByName
import com.m14n.billib.data.dao.collections.FindByNameMapStrategy
import com.m14n.billib.data.dao.collections.collectionsDao

fun journalDao(journals: Collection<Journal>) = JournalDaoDelegate(
    collectionsDao(journals),
    FindByNameMapStrategy(journals)
)

interface JournalDao :
    Dao<Journal>,
    FindByName<Journal>

class JournalDaoDelegate(
    dao: Dao<Journal>,
    findByName: FindByName<Journal>
) : JournalDao,
    Dao<Journal> by dao,
    FindByName<Journal> by findByName
