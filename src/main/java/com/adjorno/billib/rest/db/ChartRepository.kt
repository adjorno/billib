package com.adjorno.billib.rest.db;

import org.springframework.data.repository.CrudRepository;

public interface ChartRepository extends CrudRepository<Chart, Long> {

    Chart findByName(String chartName);

}
