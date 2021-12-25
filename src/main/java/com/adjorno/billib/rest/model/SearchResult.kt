package com.adjorno.billib.rest.model;

import com.adjorno.billib.rest.db.Artist;
import com.adjorno.billib.rest.db.Track;

import java.util.List;

public class SearchResult<T> {
    private int mTotal;

    private int mOffset;

    private List<T> mResults;

    public SearchResult() {
    }

    public int getTotal() {
        return mTotal;
    }

    public void setTotal(int total) {
        mTotal = total;
    }

    public int getOffset() {
        return mOffset;
    }

    public void setOffset(int offset) {
        mOffset = offset;
    }

    public List<T> getResults() {
        return mResults;
    }

    public void setResults(List<T> results) {
        mResults = results;
    }
}
