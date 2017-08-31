package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.Chart;
import com.adjorno.billib.rest.db.ChartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChartController {
    @Autowired
    private ChartRepository mChartRepository;

    @RequestMapping(value = "/chart/all", method = RequestMethod.GET)
    public Iterable<Chart> getAllCharts() {
        return mChartRepository.findAll();
    }

}
