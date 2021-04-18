package com.m14n.billib.data.chart

import com.m14n.billib.data.dao.Dao
import com.m14n.billib.data.dao.FindByName
import com.m14n.billib.data.dao.collections.FindByNameMapStrategy
import com.m14n.billib.data.dao.collections.collectionsDao

fun chartDao(charts: Collection<Chart>) = ChartDaoDelegate(
    collectionsDao(charts),
    FindByNameMapStrategy(charts)
)

interface ChartDao :
    Dao<Chart>,
    FindByName<Chart>

class ChartDaoDelegate(
    dao: Dao<Chart>,
    findByName: FindByName<Chart>
) : ChartDao,
    Dao<Chart> by dao,
    FindByName<Chart> by findByName
