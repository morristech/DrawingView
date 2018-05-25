package com.raed.drawingview.brushes.stampbrushes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.view.MotionEvent;

import com.raed.drawingview.brushes.Brush;
import com.raed.drawingview.DrawingEvent;
import com.raed.drawingview.brushes.BrushRenderer;

public class StampBrushRenderer implements BrushRenderer {

    private Canvas mCanvas;
    private StampBrush mStampBrush;
    private float mLastPoint[];
    private Bitmap mBitmap;

    @Override
    public void onTouch(DrawingEvent drawingEvent) {
        if (drawingEvent.getAction() == MotionEvent.ACTION_DOWN) {
            mLastPoint = null;
            mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }

        int size = drawingEvent.size();
        for (int i = 0; i + 1 < size - 2; i+=2)
            drawTo(drawingEvent.mPoints[i], drawingEvent.mPoints[i + 1]);

        if (size != 0) {
            float lastX = drawingEvent.mPoints[size - 2];
            float lastY = drawingEvent.mPoints[size - 1];
            if (drawingEvent.getAction() == MotionEvent.ACTION_UP) {
                drawAndStop(lastX, lastY);
            } else {
                drawTo(lastX, lastY);
            }
        }
    }

    @Override
    public void setBrush(Brush brush) {
        mStampBrush = (StampBrush) brush;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mCanvas = new Canvas(mBitmap);
    }

    //drawFromTo from mLastPoint to the passed parameters x & y
    private void drawTo(float x, float y) {
        if (mLastPoint == null) {
            // This means the drawing have just started, there is
            // no need to drawFromTo anything now.
            mLastPoint = new float[]{x, y};
            return;
        }
        mStampBrush.drawFromTo(mCanvas, mLastPoint, x, y);
    }


    private void drawAndStop(float x, float y) {
        float tempX = mLastPoint[0];
        float tempY = mLastPoint[1];
        mStampBrush.drawFromTo(mCanvas, mLastPoint, x, y);
        if (tempX == mLastPoint[0] && tempY == mLastPoint[1])
            mStampBrush.drawPoint(mCanvas, x,y);
    }
}
