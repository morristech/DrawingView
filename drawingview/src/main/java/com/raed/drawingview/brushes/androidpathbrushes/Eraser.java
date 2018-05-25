package com.raed.drawingview.brushes.androidpathbrushes;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;


public class Eraser extends PathBrush {

    private static final String TAG = "Eraser";

    public Eraser(int minSizePx, int maxSizePx) {
        super(minSizePx, maxSizePx);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaint.setColor(-1);

    }

    @Override
    public void setColor(int color) {
        Log.w(TAG,"Eraser does not has a color");
        //Erasers do not have a color
    }

}
