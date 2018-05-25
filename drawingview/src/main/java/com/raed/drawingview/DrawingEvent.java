package com.raed.drawingview;


public class DrawingEvent {

    private int mAction;//similar to MotionEvent actions
    private int mSize;
    public float[] mPoints = new float[20000];

    void add(float x, float y){
        mPoints[mSize] = x;
        mPoints[mSize + 1] = y;
        mSize += 2;
    }

    void clear() {
        mSize = 0;/* clearing this array */
    }

    void setAction(int action) {
        mAction = action;
    }

    public int size() {
        return mSize;
    }

    public int getAction() {
        return mAction;
    }
}
