package com.raed.drawingview;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

class Utilities {

    static Bitmap createBlackWhiteBackground(int w, int h, int squareSize){
        Bitmap bitmap = Bitmap.createBitmap( w, h, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.DITHER_FLAG);
        canvas.drawColor(Color.argb(255,248,248,248));
        paint.setColor(Color.argb(255,216,216,216));
        for(int i = 0; i < bitmap.getWidth() ; i += 2 * squareSize) {
            for(int j = 0; j < bitmap.getHeight() ; j += 2 * squareSize) {
                canvas.drawRect(i,j,i + squareSize, j + squareSize, paint);
                float l = i + squareSize;
                float t = j + squareSize;
                canvas.drawRect(l, t,l + squareSize, t + squareSize, paint);
            }
        }
        return bitmap;
    }


    static void cubicBezier(
            float x0, float y0,
            float x1, float y1,
            float x2, float y2,
            float x3, float y3,
            float t,
            float point[]){

        point[0] = (float) (Math.pow(1 - t, 3) * x0 +
                Math.pow(1 - t, 2) * 3 * t * x1 +
                (1 - t) * 3 * t * t * x2 +
                t * t * t * x3);

        point[1] = (float) (Math.pow(1 - t, 3) * y0 +
                Math.pow(1 - t, 2) * 3 * t * y1 +
                (1 - t) * 3 * t * t * y2 +
                t * t * t * y3);
    }

    static float dist(float x1, float y1, float x2, float y2) {
        x2-=x1;
        y2-=y1;
        return (float) Math.sqrt(x2*x2 + y2*y2);
    }

    static Bitmap resizeBitmap(Bitmap bm, int newWidth, int newHeight) {
        if (bm == null)
            return null;
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
    }
}
