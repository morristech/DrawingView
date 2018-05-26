package com.raed.drawingview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.raed.drawingview.brushes.BrushSettings;
import com.raed.drawingview.brushes.Brushes;

public class BrushView extends View {

    private BrushPreviewPerformer mBrushPreviewPerformer;
    private Bitmap mBackground;

    private Brushes mBrushes;

    public BrushView(Context context) {
        this(context, null, 0);
    }

    public BrushView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrushView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0)
            return;
        if (mBrushes == null)
            throw new RuntimeException("You need to call BrushPreview.setDrawingView(drawingView)");
        int squareDimension = (int) (10 * getResources().getDisplayMetrics().density);//10dp
        mBackground = Utilities.createBlackWhiteBackground(
                w - getPaddingStart() - getPaddingEnd(),
                h - getPaddingTop() - getPaddingBottom(),
                squareDimension
        );
        mBrushPreviewPerformer = new BrushPreviewPerformer(getContext(), mBrushes,mBackground.getWidth(), mBackground.getHeight());
        mBrushPreviewPerformer.setPreviewCallbacks(new BrushPreviewPerformer.PreviewCallbacks() {
            @Override
            public void onPreviewReadyToBeDrawn() {
                BrushView.super.invalidate();
            }
        });
        mBrushPreviewPerformer.preparePreview();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getPaddingStart(), getPaddingTop());
        canvas.drawBitmap(mBackground, 0,0,null);
        mBrushPreviewPerformer.drawPreview(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minWidth = (int) (200 * getResources().getDisplayMetrics().density);
        int minHeight = (int) (70 * getResources().getDisplayMetrics().density);
        int contentWidth = minWidth + getPaddingStart() + getPaddingEnd();
        int contentHeight = minHeight + getPaddingTop() + getPaddingBottom();

        int measuredWidth = resolveSize(contentWidth, widthMeasureSpec);
        int measuredHeight = resolveSize(contentHeight, heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    public void invalidate(){
        if (mBrushPreviewPerformer != null)
            mBrushPreviewPerformer.preparePreview();
    }

    public void setDrawingView(DrawingView drawingView) {
        mBrushes = drawingView.getBrushes();
        mBrushes.getBrushSettings().addBrushSettingListener(new BrushSettings.BrushSettingListener() {
            @Override
            public void onSettingsChanged() {
                invalidate();
            }
        });
    }

}
