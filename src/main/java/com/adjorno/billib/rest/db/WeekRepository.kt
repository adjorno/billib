package com.adjorno.billib.rest.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WeekRepository extends CrudRepository<Week, Long> {
    @Query(value = "select w from Week w order by w.date desc")
    Page<Week> findLastWeek(Pageable pageable);

    Week findByDate(String chartDate);

    @Query(value = "select w from Week w where w.date >= ?1 order by w.date asc")
    List<Week> findClosest(String chartDate);
}
