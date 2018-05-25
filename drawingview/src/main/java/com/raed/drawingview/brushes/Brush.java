package com.raed.drawingview.brushes;

import android.graphics.Paint;


public abstract class Brush {

    protected Paint mPaint;

    private float mSizeInPercentage;
    private int mMinSizeInPixel;
    private int mMaxSizeInPixel;
    protected int mSizeInPixel;

    protected Brush(int minSizeInPixel, int maxSizeInPixel) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mMinSizeInPixel = minSizeInPixel;
        if (mMinSizeInPixel < 1)
            mMaxSizeInPixel = 1;
        mMaxSizeInPixel = maxSizeInPixel;
        if (mMaxSizeInPixel < 1)
            mMaxSizeInPixel = 1;
    }

    public abstract void setColor(int color);

    // pass a percentage
    public void setSizeInPercentage(float sizeInPercentage) {
        mSizeInPercentage = sizeInPercentage;
        mSizeInPixel = (int) (mMinSizeInPixel + mSizeInPercentage * (mMaxSizeInPixel - mMinSizeInPixel));
    }

    protected int getSizeInPixel() {
        return mSizeInPixel;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public float getSizeInPercentage() {
        return mSizeInPercentage;
    }

    public int getSizeForSafeCrop(){
        return getSizeInPixel();
    }

    public void setMinAndMaxSizeInPixel(int minSizeInPixel, int maxSizeInPixel){
        if (maxSizeInPixel < minSizeInPixel)
            throw new IllegalArgumentException("maxSizeInPixel must be >= minSizeInPixel");
        if (mMinSizeInPixel < 1 || mMaxSizeInPixel < 1)
            throw new IllegalArgumentException("maxSizeInPixel and minSizeInPixel must be >= 1");
        mMinSizeInPixel = minSizeInPixel;
        mMaxSizeInPixel = maxSizeInPixel;
    }

    public int getMinSizeInPixel() {
        return mMinSizeInPixel;
    }

    public int getMaxSizeInPixel() {
        return mMaxSizeInPixel;
    }

    public float getStep(){
        float step = mSizeInPixel/5f;
        return step > 1f ? step : 1f;
    }

}
