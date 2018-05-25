package com.raed.drawingview.brushes.stampbrushes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

public class BitmapBrush extends StampBrush {

    private Bitmap mOriginalBrush;
    Bitmap mResizedBrush;
    float mHalfResizedBrushHeight;
    float mHalfResizedBrushWidth;

    private Canvas mResizedBrushCanvas;

    public BitmapBrush(Bitmap bitmap, int minSizePx, int maxSizePx, int frequency) {
        super(minSizePx, maxSizePx, frequency);
        mResizedBrushCanvas = new Canvas();
        mOriginalBrush = bitmap;
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
            canvas.drawBitmap(mResizedBrush, xCenter - mHalfResizedBrushWidth, yCenter - mHalfResizedBrushHeight, null);
        }
        lastDrawnPoint[0] = lastDrawnPoint[0] + i * dx;
        lastDrawnPoint[1] = lastDrawnPoint[1] + i * dy;
    }

    @Override
    public void drawPoint(Canvas canvas, float left, float top) {
        canvas.drawBitmap(mResizedBrush, left - mHalfResizedBrushWidth, top - mHalfResizedBrushHeight, null);
    }

    @Override
    public void setSizeInPercentage(float sizePercentage) {
        super.setSizeInPercentage(sizePercentage);

        mResizedBrush = Bitmap.createBitmap(mSizeInPixel, (int) (mSizeInPixel /(float)mOriginalBrush.getWidth() * mOriginalBrush.getHeight()), Bitmap.Config.ARGB_8888);

        mResizedBrushCanvas.setBitmap(mResizedBrush);

        updateResizeBrush();

        mHalfResizedBrushWidth = mResizedBrush.getWidth()/2;
        mHalfResizedBrushHeight = mResizedBrush.getHeight()/2;
    }

    @Override
    public void setColor(int color) {
        mPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        mPaint.setAlpha(Color.alpha(color));
        updateResizeBrush();
    }

    private void updateResizeBrush(){
        mResizedBrushCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        float xScale = mResizedBrush.getWidth()/(float)mOriginalBrush.getWidth();
        float yScale = mResizedBrush.getHeight()/(float)mOriginalBrush.getHeight();
        mResizedBrushCanvas.scale(xScale, yScale);
        mResizedBrushCanvas.drawBitmap(mOriginalBrush, 0, 0, mPaint);
        mResizedBrushCanvas.scale(1/xScale, 1/yScale);
    }
}
