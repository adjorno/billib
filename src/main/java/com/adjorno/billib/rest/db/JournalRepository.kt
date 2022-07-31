package com.adjorno.billib.rest.db;

import org.springframework.data.repository.CrudRepository;

public interface JournalRepository extends CrudRepository<Journal, Long> {

    Journal findByName(String name);
}
