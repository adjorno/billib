package com.m14n.billib.data.week

import com.m14n.billib.data.dao.Dao
import com.m14n.billib.data.dao.FindByDate
import com.m14n.billib.data.dao.collections.FindByDateMapStrategy
import com.m14n.billib.data.dao.collections.collectionsDao

fun weekDao(weeks: Collection<Week>) = WeekDaoDelegate(
    collectionsDao(weeks),
    FindByDateMapStrategy(weeks)
)

interface WeekDao :
    Dao<Week>,
    FindByDate<Week>

class WeekDaoDelegate(
    dao: Dao<Week>,
    findByDate: FindByDate<Week>
) : WeekDao,
    Dao<Week> by dao,
    FindByDate<Week> by findByDate
