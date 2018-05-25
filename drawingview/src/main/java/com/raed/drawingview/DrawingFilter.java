package com.raed.drawingview;

/**
 * This filter uses quad interpolation to make drawing smother.
 */

class DrawingFilter{

    private float mCurrentBrushStep;
    private float mPoint0[];
    private float mPoint1[];

    void filter(float x, float y, DrawingEvent points) {
        if (mPoint0 == null) {
            mPoint0 = new float[]{x, y};
            mPoint1 = new float[]{x, y};
            points.add(x, y);
            return;
        }
        float x2 = (mPoint1[0] + x) / 2.0f;
        float y2 = (mPoint1[1] + y) / 2.0f;

        float distanceX = x2 - mPoint0[0];
        float distanceY = y2 - mPoint0[1];
        int pointCount = (int) Math.ceil(Math.sqrt((distanceX * distanceX) + (distanceY * distanceY)) / mCurrentBrushStep);
        for (int n = 1; n < pointCount; n++) {
            float t = ((float) n) / ((float) pointCount);
            float tSqr = t * t;
            float tPrime = 1 - t;
            float tPrimeSqr = tPrime * tPrime;
            points.add(
                    tSqr * x2 + 2 * t * tPrime * mPoint1[0] + tPrimeSqr * mPoint0[0],
                    tSqr * y2 + 2 * t * tPrime * mPoint1[1] + tPrimeSqr * mPoint0[1]
            );
        }

        points.add(x2, y2);

        mPoint0[0] = x2;
        mPoint0[1] = y2;
        mPoint1[0] = x;
        mPoint1[1] = y;
    }


    void reset() {
        mPoint0 = mPoint1 = null;
    }

    void setCurrentBrushStep(float currentBrushStep) {
        mCurrentBrushStep = currentBrushStep;
    }
}
