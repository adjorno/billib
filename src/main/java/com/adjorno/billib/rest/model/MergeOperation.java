package com.adjorno.billib.rest.model;

public class MergeOperation<T> {
    private T mRemoved;
    private T mMergedTo;

    public MergeOperation() {
    }

    public MergeOperation(T removed, T mergedTo) {
        mRemoved = removed;
        mMergedTo = mergedTo;
    }

    public T getRemoved() {
        return mRemoved;
    }

    public void setRemoved(T removed) {
        mRemoved = removed;
    }

    public T getMergedTo() {
        return mMergedTo;
    }

    public void setMergedTo(T mergedTo) {
        mMergedTo = mergedTo;
    }
}
