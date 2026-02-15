package com.adjorno.billib.rest;

import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UpdateResult {
    public static final int RESULT_OK = 0;
    public static final int RESULT_DIFFERENT_SIZE = 1;
    public static final int RESULT_DUPLICATE = 2;
    public static final int RESULT_FAILED = 3;

    private final List<Pair<String, Integer>> mChartUpdates = new ArrayList<>();

    private Date mUpdateWeek;

    public void setUpdateWeek(Date updateWeek) {
        mUpdateWeek = updateWeek;
    }

    public void addChartUpdate(String chart, int result) {
        mChartUpdates.add(Pair.of(chart, result));
    }

    public List<Pair<String, Integer>> getChartUpdates() {
        return mChartUpdates;
    }

    public Date getUpdateWeek() {
        return mUpdateWeek;
    }

    public static String toString(int result) {
        switch (result) {
            case RESULT_OK: return "OK";
            case RESULT_DIFFERENT_SIZE: return "DIFFERENT_SIZE";
            case RESULT_DUPLICATE: return "DUPLICATE";
            case RESULT_FAILED: return "FAILED";
            default:
                return "UNEXPECTED RESULT";
        }
    }
}
