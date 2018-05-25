package com.raed.drawingview.brushes;


import android.content.res.Resources;
import android.graphics.BitmapFactory;

import com.raed.drawingview.R;
import com.raed.drawingview.brushes.androidpathbrushes.Eraser;
import com.raed.drawingview.brushes.androidpathbrushes.Pen;
import com.raed.drawingview.brushes.stampbrushes.BitmapBrush;
import com.raed.drawingview.brushes.stampbrushes.Calligraphy;
import com.raed.drawingview.brushes.stampbrushes.RandRotBitmapBrush;


public class Brushes {

    public static final int PEN = 0;
    public static final int PENCIL = 1;
    public static final int CALLIGRAPHY = 2;
    public static final int AIR_BRUSH = 3;
    public static final int ERASER = 4;

    private BrushSettings mBrushSettings;
    private Brush[] mBrushes;

    public Brushes(Resources resources){
        mBrushes = new Brush[5];
        
        mBrushes[PEN] = new Pen(
                resources.getDimensionPixelSize(R.dimen.pen_min_stroke_size),
                resources.getDimensionPixelSize(R.dimen.pen_max_stroke_size));

        RandRotBitmapBrush pencil = new RandRotBitmapBrush(
                BitmapFactory.decodeResource(resources, R.drawable.brush_pencil),
                resources.getDimensionPixelSize(R.dimen.pencil_min_stroke_size),
                resources.getDimensionPixelSize(R.dimen.pencil_max_stroke_size),
                6);
        mBrushes[PENCIL] = pencil;

        mBrushes[ERASER] = new Eraser(
                resources.getDimensionPixelSize(R.dimen.eraser_min_stroke_size),
                resources.getDimensionPixelSize(R.dimen.eraser_max_stroke_size));

        BitmapBrush airBrush = new BitmapBrush(
                BitmapFactory.decodeResource(resources,R.drawable.brush_0),
                resources.getDimensionPixelSize(R.dimen.brush0_min_stroke_size),
                resources.getDimensionPixelSize(R.dimen.brush0_max_stroke_size),
                6);
        mBrushes[AIR_BRUSH] = airBrush;

        Calligraphy calligraphy = new Calligraphy(
                resources.getDimensionPixelSize(R.dimen.calligraphy_min_stroke_size),
                resources.getDimensionPixelSize(R.dimen.calligraphy_max_stroke_size),
                20);
        mBrushes[CALLIGRAPHY] = calligraphy;

        for (Brush brush : mBrushes) {
            brush.setSizeInPercentage(0.5f);
            brush.setColor(0xff000000);
        }

        mBrushSettings = new BrushSettings(this);
    }

    public Brush getBrush(int brushID){
        if (brushID >= mBrushes.length || brushID < 0)
            throw new IllegalArgumentException("There is no brush with id = " + brushID + " in " + getClass());
        return mBrushes[brushID];
    }

    Brush[] getBrushes() {
        return mBrushes;
    }

    public BrushSettings getBrushSettings() {
        return mBrushSettings;
    }
}
