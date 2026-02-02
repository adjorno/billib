package com.adjorno.billib.rest;

import com.adjorno.billib.rest.db.Track;
import com.adjorno.billib.rest.db.TrendType;
import com.m14n.ex.Ex;
import com.m14n.billib.data.BB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class Schedule {
    private static final DateFormat PACIFIC_DATE_FORMAT = new SimpleDateFormat(BB.CHART_DATE_FORMAT_STRING);

    static {
        PACIFIC_DATE_FORMAT.setTimeZone(Ex.TIME_ZONE_PACIFIC_AUCKLAND);
    }

    @Autowired(required = false)
    private UpdateController mUpdateController;

    @Autowired
    private TrendsController mTrendsController;

    @Autowired
    private TrackController mTrackController;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = Ex.TIME_ZONE_CRON_PACIFIC_AUCKLAND)
    void updateDb() {
//        System.out.println("START UPDATING DB");
//        UpdateResult theUpdateResult = mUpdateController.updateDB();
//        List<Pair<String, Integer>> theChartUpdates = theUpdateResult.getChartUpdates();
//        if (Ex.isNotEmpty(theChartUpdates)) {
//            for (Pair<String, Integer> theChartUpdate : theChartUpdates) {
//                System.out.println(theChartUpdate.getFirst() + " " + UpdateResult.toString(theChartUpdate.getSecond()));
//            }
//        } else {
//            System.out.println("THERE ARE NO ANY UPDATES");
//        }
//        Date theUpdateWeek = theUpdateResult.getUpdateWeek();
//        if (theUpdateWeek != null) {
//            mTrendsController.generateTrends(BB.CHART_DATE_FORMAT.format(theUpdateWeek), TrendType.TYPE_ALL);
//            System.out.println("TRENDS CREATED");
//        }
//        System.out.println("FINISH UPDATING DB");
    }

    @Scheduled(cron = "0 20 0 * * *", zone = Ex.TIME_ZONE_CRON_PACIFIC_AUCKLAND)
    void updateDayTrack() throws ParseException {
//        System.out.println("START UPDATING DAY TRACK");
//        String theDate = PACIFIC_DATE_FORMAT.format(new Date());
//        Track theTrack = mTrackController.updateDayTrack(theDate);
//        System.out.println(theDate + " " + theTrack.getId() + " " + theTrack);
//        System.out.println("FINISH UPDATING DAY TRACK");
    }

    @Scheduled(fixedRate = 1000 * 60 * 15)
    void pingApp() {
//        System.out.println("PING APP");
//        new RestTemplate().getForObject("https://billibrest.herokuapp.com", String.class);
    }
}
