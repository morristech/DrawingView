package com.raed.drawingview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;

import com.raed.drawingview.brushes.Brush;
import com.raed.drawingview.brushes.BrushSettings;
import com.raed.drawingview.brushes.Brushes;
import com.raed.drawingview.brushes.androidpathbrushes.Eraser;
import com.raed.drawingview.brushes.androidpathbrushes.PathBrush;
import com.raed.drawingview.brushes.stampbrushes.StampBrush;

import java.util.ArrayList;
import java.util.List;



public class BrushPreviewPerformer {


    private final Canvas mPreviewCanvas;
    private final Bitmap mPreviewBitmap;

    private Brushes mBrushes;

    private Paint mEraserPaint;

    private PreviewCallbacks mPreviewCallbacks;

    private Path mCurvePath;
    private float[] mPoints0;
    private float[] mPoints1;
    private float[] mPoints2;
    private float[] mPoints3;
    private float[] mLastPoint;


    private Brush mBrush;//the currently selected brush
    private StampBrush mStampBrush;//the currently selected stamp brush if any

    public interface PreviewCallbacks{
        void onPreviewReadyToBeDrawn();
    }

    BrushPreviewPerformer(Context context, Brushes brushes, int w, int h) {
        mBrushes = brushes;

        List<float[]> curvePoints = initializeCurve(context, w, h);
        initializeCurvePoints(curvePoints);
        mCurvePath = new Path();
        mCurvePath.moveTo(curvePoints.get(0)[0], curvePoints.get(0)[1]);
        for (int i = 1 ; i < curvePoints.size() ; i++) {
            float[] point = curvePoints.get(i);
            mCurvePath.lineTo(point[0], point[1]);
        }

        mPreviewBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mPreviewCanvas = new Canvas(mPreviewBitmap);

        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setColor(-1);

    }

    void drawPreview(Canvas canvas) {
        if (mBrush == null)
            return;
        if (mBrush instanceof StampBrush) {
            canvas.drawBitmap(mPreviewBitmap, 0, 0, null);
            return;
        }
        PathBrush pathBrush = (PathBrush) mBrush;
        if (pathBrush instanceof Eraser) {
            mEraserPaint.setStrokeWidth(pathBrush.getPaint().getStrokeWidth());
            canvas.drawPath(mCurvePath, mEraserPaint);//drawFromTo it as a white stroke
        }
        else
            canvas.drawPath(mCurvePath, pathBrush.getPaint());
    }

    /*
     * This method is called, and when the brush preview is ready to be drawn on another
     * canvas(not mPreviewCanvas) we call mPreviewCallbacks.onPreviewReadyToBeDrawn().
     */
    void preparePreview() {
        mStampBrush = null;
        BrushSettings brushSettings = mBrushes.getBrushSettings();
        mBrush = mBrushes.getBrush(brushSettings.getSelectedBrush());
        if (mBrush instanceof StampBrush) {
            mStampBrush = (StampBrush) mBrush;
            //We do not want to block the main thread, so divide the curve into segments
            //and cancel any previous unfinished drawing tasks.
            mHandler.removeCallbacksAndMessages(null);
            Message.obtain(mHandler, 0).sendToTarget();
            Message.obtain(mHandler, 1).sendToTarget();
            Message.obtain(mHandler, 2).sendToTarget();
            Message.obtain(mHandler, 3).sendToTarget();
        } else
            mPreviewCallbacks.onPreviewReadyToBeDrawn();//drawing other tool is not expensive, be lazy and drawFromTo it when the client ask for it
    }

    void setPreviewCallbacks(PreviewCallbacks previewCallbacks) {
        mPreviewCallbacks = previewCallbacks;
    }

    private void drawTo(float x, float y) {
        if (mLastPoint == null) {
            // This means the drawing have just started, there is
            // no need to drawFromTo anything now.
            mLastPoint = new float[]{x, y};
            return;
        }
        mStampBrush.drawFromTo(mPreviewCanvas, mLastPoint, x, y);
    }

    private void drawAndStop(float x, float y) {
        float tempX = mLastPoint[0];
        float tempY = mLastPoint[1];
        mStampBrush.drawFromTo(mPreviewCanvas, mLastPoint, x, y);
        if (tempX == mLastPoint[0] && tempY == mLastPoint[1])
            mStampBrush.drawPoint(mPreviewCanvas, x,y);
    }

    private void initializeCurvePoints(List<float[]> curvePoints){

        int pointListSize = curvePoints.size();
        mPoints0 = new float[2 * (pointListSize/4)];
        mPoints1 = new float[2 * (pointListSize/4)];
        mPoints2 = new float[2 * (pointListSize/4)];
        mPoints3 = new float[2 * (pointListSize - 3 * (pointListSize/4))];

        for (int i = 0 ; i < mPoints0.length ; i++)
            mPoints0[i] = curvePoints.get(i/2)[i%2];

        int startFrom = mPoints0.length;
        for (int i = 0 ; i < mPoints1.length ; i++)
            mPoints1[i] = curvePoints.get( (startFrom + i) / 2 )[i % 2];

        startFrom = mPoints0.length + mPoints1.length;
        for (int i = 0 ; i < mPoints2.length ; i++)
            mPoints2[i] = curvePoints.get((startFrom + i)/2)[i % 2];

        startFrom = mPoints0.length + mPoints1.length + mPoints2.length;
        for (int i = 0 ; i < mPoints3.length ; i++)
            mPoints3[i] = curvePoints.get((startFrom + i)/2)[i % 2];

    }

    private List<float[]> initializeCurve(Context context, int w, int h){
        if (w == 0 || h == 0)
            throw new IllegalArgumentException("width & height must be > 0");
        float curvePoints[][] = new float[4][2];
        curvePoints[0][0] = w/10f;
        curvePoints[1][0] = 0.35f * w;
        curvePoints[2][0] = 0.65f * w;
        curvePoints[3][0] = 0.9f * w;
        curvePoints[0][1] = h/2f;
        curvePoints[1][1] = 0;
        curvePoints[2][1] = h;
        curvePoints[3][1] = h/2f;

        List<float[]> pointList = new ArrayList<>();
        float approximatedArcLength =
                Utilities.dist(curvePoints[0][0],curvePoints[0][1], curvePoints[1][0],curvePoints[1][1]) +
                        Utilities.dist(curvePoints[1][0],curvePoints[1][1],curvePoints[2][0],curvePoints[2][1]) +
                        Utilities.dist(curvePoints[2][0],curvePoints[2][1],curvePoints[3][0],curvePoints[3][1]);
        float step = context.getResources().getDisplayMetrics().density;// 1dp
        int size = (int) (approximatedArcLength/step);
        int i = 0;
        for (; i < size; i++) {
            float[] point = new float[2];
            pointList.add(point);
            Utilities.cubicBezier(
                    curvePoints[0][0], curvePoints[0][1],
                    curvePoints[1][0], curvePoints[1][1],
                    curvePoints[2][0], curvePoints[2][1],
                    curvePoints[3][0], curvePoints[3][1],
                    i * step / approximatedArcLength, point);
        }

        return pointList;
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (mStampBrush == null)
                return true;

            if (message.what == 0){
                mLastPoint = null;
                mPreviewCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            }

            float[] points;
            if (message.what == 0)
                points = mPoints0;
            else if (message.what == 1)
                points = mPoints1;
            else if (message.what == 2)
                points = mPoints2;
            else if (message.what == 3)
                points = mPoints3;
            else
                throw new IllegalArgumentException("Undefiled message");

            for (int i = 0 ; i + 1 < points.length - 2; i+=2)
                drawTo(points[i], points[i + 1]);

            int pointsLength = points.length;
            if (message.what == 3) {
                drawAndStop(points[pointsLength - 2], points[pointsLength - 1]);
                mPreviewCallbacks.onPreviewReadyToBeDrawn();
            }
            else
                drawTo(points[pointsLength - 2], points[pointsLength - 1]);

            return true;
        }
    });

}
