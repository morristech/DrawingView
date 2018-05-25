package com.raed.drawingview;

import android.graphics.Bitmap;
import android.graphics.Rect;


class DrawingAction {

    Bitmap mBitmap;
    Rect mRect;

    DrawingAction(Bitmap bitmap, Rect rect){
        mBitmap = bitmap;
        mRect = new Rect(rect);
    }

    int getSize() {
        return mBitmap.getAllocationByteCount();
    }

}
