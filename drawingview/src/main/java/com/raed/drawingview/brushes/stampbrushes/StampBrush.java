package com.raed.drawingview.brushes.stampbrushes;

import android.graphics.Canvas;

import com.raed.drawingview.brushes.Brush;


public abstract class StampBrush extends Brush {

    private final int mFrequency;

    float mStep;

    StampBrush(int minSizePx, int maxSizePx, int frequency){
        super(minSizePx, maxSizePx);
        mFrequency = frequency;
        updateStep();
    }

    public abstract void drawFromTo(Canvas canvas, float lastDrawnPoint[], float x1, float y1);
    public abstract void drawPoint(Canvas canvas, float x, float y);

    @Override
    public void setSizeInPercentage(float sizePercentage) {
        super.setSizeInPercentage(sizePercentage);
        updateStep();
    }

    @Override
    public float getStep() {
        return mStep;
    }

    private void updateStep() {
        mStep = mSizeInPixel / mFrequency;
        if (mStep < 1)
            mStep = 1;
    }

}