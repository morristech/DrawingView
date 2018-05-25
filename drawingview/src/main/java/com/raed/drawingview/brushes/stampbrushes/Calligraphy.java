package com.raed.drawingview.brushes.stampbrushes;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

public class Calligraphy extends StampBrush {

    private float mHalfHeight;
    private float mHalfWidth;

    public Calligraphy(int minSizePx, int maxSizePx, int frequency) {
        super(minSizePx, maxSizePx, frequency);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    private RectF mTempRectF = new RectF();
    @Override
    public void drawFromTo(Canvas canvas, float lastDrawnPoint[], float x1, float y1) {
        float xTerm = x1 - lastDrawnPoint[0];
        float yTerm = y1 - lastDrawnPoint[1];
        float distance = (float) Math.sqrt(xTerm * xTerm + yTerm * yTerm);
        if (distance < mStep)
            return;
        float stepInPercentage = mStep / distance;
        float dx = x1 - lastDrawnPoint[0];
        float dy = y1 - lastDrawnPoint[1];
        float i = 0;

        for (; i <= 1; i += stepInPercentage) {
            float xCenter = lastDrawnPoint[0] + i * dx;
            float yCenter = lastDrawnPoint[1] + i * dy;
            //We can extract a method to drawFromTo a point but since this method is invoked very frequently
            // lets do a small optimization by minimizing function calls
            canvas.rotate(-45, xCenter, yCenter );
            mTempRectF.left = xCenter - mHalfWidth;
            mTempRectF.top = yCenter - mHalfHeight;
            mTempRectF.right = xCenter + mHalfWidth;
            mTempRectF.bottom = yCenter + mHalfHeight;
            canvas.drawOval(mTempRectF, mPaint);
            canvas.rotate(45, xCenter, yCenter );//restore old rotation
        }

        //update the last drawn point
        lastDrawnPoint[0] = lastDrawnPoint[0] + i * dx;
        lastDrawnPoint[1] = lastDrawnPoint[1] + i * dy;
    }

    @Override
    public void drawPoint(Canvas canvas, float left, float top) {
        canvas.rotate(-45, left, top );

        mTempRectF.left = left - mHalfWidth;
        mTempRectF.top = top - mHalfHeight;
        mTempRectF.right = left + mHalfWidth;
        mTempRectF.bottom = top + mHalfHeight;
        canvas.drawOval(mTempRectF, mPaint);

        canvas.rotate(45, left, top );
    }

    @Override
    public void setColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void setSizeInPercentage(float sizePercentage) {
        super.setSizeInPercentage(sizePercentage);
        mHalfHeight = mSizeInPixel /8;
        mHalfWidth = mSizeInPixel /2;
    }
}
