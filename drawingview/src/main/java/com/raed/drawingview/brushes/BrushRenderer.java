package com.raed.drawingview.brushes;


import android.graphics.Canvas;

import com.raed.drawingview.DrawingEvent;

public interface BrushRenderer {
    void draw(Canvas canvas);
    void onTouch(DrawingEvent drawingEvent);
    void setBrush(Brush brush);
}
