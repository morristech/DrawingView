package com.raed.drawingview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.raed.drawingview.brushes.Brush;
import com.raed.drawingview.brushes.BrushRenderer;
import com.raed.drawingview.brushes.Brushes;
import com.raed.drawingview.brushes.androidpathbrushes.Eraser;
import com.raed.drawingview.brushes.androidpathbrushes.PathBrushRenderer;
import com.raed.drawingview.brushes.stampbrushes.StampBrush;
import com.raed.drawingview.brushes.stampbrushes.StampBrushRenderer;

public class DrawingPerformer {

    private static final String TAG = "DrawingPerformer";
    private BrushRenderer mCurrentBrushRenderer;

    interface DrawingPerformerListener{
        void onDrawingPerformed(Path path, Paint paint, Rect rect);
        void onDrawingPerformed(Bitmap bitmap, Rect rect);
    }

    private DrawingPerformerListener mDrawingPerformerListener;

    private Bitmap mBitmap;//this is used for the eraser
    private Canvas mCanvas;

    private final DrawingFilter mDrawingFilter = new DrawingFilter();

    private StampBrushRenderer mStampBrushRenderer;
    private PathBrushRenderer mPathBrushRenderer;

    private DrawingBoundsRect mDrawingBoundsRect;
    private Brush mSelectedBrush;
    private boolean mDrawing = false;

    private Brushes mBrushes;

    public DrawingPerformer(Brushes brushes){

        mStampBrushRenderer = new StampBrushRenderer();
        mPathBrushRenderer = new PathBrushRenderer();

        mBrushes = brushes;

        mDrawingBoundsRect = new DrawingBoundsRect();
        
    }

    void draw(Canvas canvas,  Bitmap bitmap) {
        if (mSelectedBrush.getClass().equals(Eraser.class)){
            mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            mCanvas.drawBitmap(bitmap, 0, 0, null);
            mCurrentBrushRenderer.draw(mCanvas);
            canvas.drawBitmap(mBitmap, 0, 0, null);
            return;
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
        mCurrentBrushRenderer.draw(canvas);
    }

    private DrawingEvent mTemDrawingEvent = new DrawingEvent();
    void onTouch(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_CANCEL)
            action = MotionEvent.ACTION_UP;

        float x = event.getX();
        float y = event.getY();
        if (action == MotionEvent.ACTION_DOWN) {
            updateSelectedBrush();
            mDrawing = true;
            mDrawingFilter.reset();
        }

        if (!mDrawing)//this means the drawing have been canceled but touch events are st
            return;

        mTemDrawingEvent.clear();
        mDrawingFilter.filter(x, y, mTemDrawingEvent);
        
        mTemDrawingEvent.setAction(action);
        
        if (action == MotionEvent.ACTION_DOWN){
            mDrawingBoundsRect.reset(x, y);
        }else {
            mDrawingBoundsRect.update(mTemDrawingEvent);
        }
        
        mCurrentBrushRenderer.onTouch(mTemDrawingEvent);

        if (action == MotionEvent.ACTION_UP) {
            mDrawing = false;
            notifyListener();
        }
    }

    void setPaintPerformListener(DrawingPerformerListener listener) {
        mDrawingPerformerListener = listener;
    }

    boolean isDrawing() {
        return mDrawing;
    }

    void setWidthAndHeight(int width, int height){
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        mStampBrushRenderer.setBitmap(mBitmap);
    }


    private Rect getDrawingBoundsRect() {
        int size = mSelectedBrush.getSizeForSafeCrop();
        Log.d(TAG, "getDrawingBoundsRect: " + size);
        int left = (int) (mDrawingBoundsRect.mMinX - size / 2);
        left = ( left > 0) ?  left : 0;
        int top = (int) (mDrawingBoundsRect.mMinY - size / 2);
        top = ( top > 0) ?  top : 0;
        int width = (int) (mDrawingBoundsRect.mMaxX - mDrawingBoundsRect.mMinX + size);
        width = width > (mBitmap.getWidth() - left) ? mBitmap.getWidth() - left : width;
        int height = (int) (mDrawingBoundsRect.mMaxY - mDrawingBoundsRect.mMinY + size);
        height = height > (mBitmap.getHeight() - top) ? mBitmap.getHeight() - top : height;
        return new Rect(left, top, left + width, top + height);
    }

    private void updateSelectedBrush() {
        int selectedBrushID = mBrushes.getBrushSettings().getSelectedBrush();
        mSelectedBrush = mBrushes.getBrush(selectedBrushID);
        if (mSelectedBrush instanceof StampBrush)
            mCurrentBrushRenderer = mStampBrushRenderer;
        else  //this means this is a path brush
            mCurrentBrushRenderer = mPathBrushRenderer;
        mCurrentBrushRenderer.setBrush(mSelectedBrush);
        mDrawingFilter.setCurrentBrushStep(mSelectedBrush.getStep());
    }

    private void notifyListener() {
        Rect rect = getDrawingBoundsRect();
        if (rect.right - rect.left <= 0 || rect.bottom - rect.top <= 0)
            return;
        if (mSelectedBrush instanceof StampBrush){
            //uncomment for testing
            //Canvas canvas = new Canvas(mBitmap);
            //canvas.drawColor(Color.argb(150,150,150,150));
            Bitmap b = Bitmap.createBitmap(mBitmap,
                    rect.left,
                    rect.top,
                    rect.right - rect.left,
                    rect.bottom - rect.top);
            mDrawingPerformerListener.onDrawingPerformed(b, rect);
        } else {
            mDrawingPerformerListener.onDrawingPerformed(
                    mPathBrushRenderer.mPath,
                    mPathBrushRenderer.mCurrentPathToolPaint,
                    rect);
        }
    }

    private static class DrawingBoundsRect {

        private float mMinX;
        private float mMinY;
        private float mMaxX;
        private float mMaxY;

        void update(DrawingEvent drawingEvent) {
            int size = drawingEvent.size();
            for (int i = 0; i + 1 < size; i+=2) {
                
                if (drawingEvent.mPoints[i] < mMinX)
                    mMinX = drawingEvent.mPoints[i];
                else if (drawingEvent.mPoints[i] > mMaxX)
                    mMaxX = drawingEvent.mPoints[i];

                if (drawingEvent.mPoints[i + 1] < mMinY)
                    mMinY = drawingEvent.mPoints[i + 1];
                else if (drawingEvent.mPoints[i + 1] > mMaxY)
                    mMaxY = drawingEvent.mPoints[i + 1];
            }
        }

        void reset(float x, float y){
            mMinX = mMaxX = x;
            mMinY = mMaxY = y;
        }
    }

}
