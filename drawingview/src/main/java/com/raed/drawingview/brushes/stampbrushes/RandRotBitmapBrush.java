package com.raed.drawingview.brushes.stampbrushes;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class RandRotBitmapBrush extends BitmapBrush {

    private static float sRandomAnglesArray[];
    private static int sRandomAnglesIndex = 0;
    private static final int RANDOM_ANGLES_NUMBER = 100;

    static {
        sRandomAnglesArray = new float[RANDOM_ANGLES_NUMBER];
        for (int i = 0; i < RANDOM_ANGLES_NUMBER; i++)
            sRandomAnglesArray[i] = (float) (Math.random() * 360);
    }

    public RandRotBitmapBrush(Bitmap bitmap, int minSizePx, int maxSizePx, int frequency) {
        super(bitmap, minSizePx, maxSizePx, frequency);
    }


    @Override
    public void drawFromTo(Canvas canvas, float lastDrawnPoint[], float x1, float y1) {

        float xTerm = x1 - lastDrawnPoint[0];
        float yTerm = y1 - lastDrawnPoint[1];
        float dist = (float) Math.sqrt(xTerm * xTerm + yTerm * yTerm);
        if (dist < mStep)
            return;

        float stepInPercentage = mStep / dist;
        float i = 0;
        float dx = x1 - lastDrawnPoint[0];
        float dy = y1 - lastDrawnPoint[1];
        for (; i <= 1; i += stepInPercentage) {
            float xCenter = lastDrawnPoint[0] + i * dx;
            float yCenter = lastDrawnPoint[1] + i * dy;

            canvas.rotate(sRandomAnglesArray[sRandomAnglesIndex], xCenter, yCenter );
            canvas.drawBitmap(mResizedBrush,  xCenter - mHalfResizedBrushWidth, yCenter - mHalfResizedBrushHeight, null);
            canvas.rotate(-sRandomAnglesArray[sRandomAnglesIndex], xCenter, yCenter );

            sRandomAnglesIndex++;
            sRandomAnglesIndex %= RANDOM_ANGLES_NUMBER;

        }
        lastDrawnPoint[0] = lastDrawnPoint[0] + i * dx;
        lastDrawnPoint[1] = lastDrawnPoint[1] + i * dy;
    }

    public void drawPoint(Canvas canvas, float left, float top) {
        float rotation = (float) (Math.random() * 360);
        canvas.rotate(rotation, left, top );
        canvas.drawBitmap(mResizedBrush,  left - mHalfResizedBrushWidth, top - mHalfResizedBrushHeight, null);
        canvas.rotate(-rotation, left, top );
    }

    @Override
    public int getSizeForSafeCrop() {
        return (int) (getSizeInPixel() * 2);
    }
}
