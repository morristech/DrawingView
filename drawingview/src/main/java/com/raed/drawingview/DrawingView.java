package com.raed.drawingview;



import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.raed.drawingview.brushes.BrushSettings;
import com.raed.drawingview.brushes.Brushes;


public class DrawingView extends View{

    private static final float MAX_SCALE = 5f;
    private static final float MIN_SCALE = 0.1f;

    private Canvas mCanvas;
    private Bitmap mDrawingBitmap;
    private Bitmap mBGBitmap;
    private int mBGColor;//BackGroundColor

    //if true, do not drawFromTo any thin. Just zoom and translate thr drawing in onTouchEvent()
    private boolean mZoomMode = false;

    private float mDrawingTranslationX = 0f;
    private float mDrawingTranslationY = 0f;
    private float mScaleFactor = 1f;

    private float mLastX[] = new float[2];
    private float mLastY[] = new float[2];

    private ActionStack mActionStack;//This is used for undo/redo, if null this means the undo and redo are disabled

    private DrawingPerformer mDrawingPerformer;

    private OnDrawListener mOnDrawListener;

    private Brushes mBrushes;

    private boolean mCleared = true;

    private Paint mSrcPaint = new Paint(){{
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        }};

    private ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector(
            getContext(),
            new ScaleGestureDetector.SimpleOnScaleGestureListener(){
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float xCenter = (mLastX[0] + mLastX[1])/2;
                    float yCenter = (mLastY[0] + mLastY[1])/2;
                    float xd = (xCenter - mDrawingTranslationX);
                    float yd = (yCenter - mDrawingTranslationY);
                    mScaleFactor *= detector.getScaleFactor();
                    if (mScaleFactor == MAX_SCALE || mScaleFactor == MIN_SCALE)
                        return true;
                    mDrawingTranslationX = xCenter - xd * detector.getScaleFactor();
                    mDrawingTranslationY = yCenter - yd * detector.getScaleFactor();

                    checkBounds();
                    invalidate();
                    return true;
                }
            }
    );

    public interface OnDrawListener{
        void onDraw();
    }

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBrushes = new Brushes(context.getResources());
        if (attrs != null)
            initializeAttributes(attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mDrawingBitmap == null && w != 0 && h != 0) {
            initializeDrawingBitmap(
                    w - getPaddingStart() - getPaddingEnd(),
                    h - getPaddingTop() - getPaddingBottom());
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //prevent drawing in the padding
        canvas.clipRect(
                getPaddingStart(),
                getPaddingTop(),
                canvas.getWidth() - getPaddingRight(),
                canvas.getHeight() - getPaddingBottom()
        );

        //drawFromTo the background and the bitmap in the middle with scale and translation
        canvas.translate(getPaddingStart() + mDrawingTranslationX, getPaddingTop() + mDrawingTranslationY);
        canvas.scale(mScaleFactor, mScaleFactor);
        canvas.clipRect(//prevent drawing paths outside the bounds
                0,
                0,
                mDrawingBitmap.getWidth(),
                mDrawingBitmap.getHeight()
        );
        canvas.drawColor(mBGColor);
        if (mBGBitmap != null)
            canvas.drawBitmap(mBGBitmap, 0, 0, null);
        if (mDrawingPerformer.isDrawing())//true if the user is touching the screen
            mDrawingPerformer.draw(canvas, mDrawingBitmap);
        else
            canvas.drawBitmap(mDrawingBitmap,0, 0, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minDimension = (int) (250 * getResources().getDisplayMetrics().density);//150dp
        int contentWidth = minDimension + getPaddingStart() + getPaddingEnd();
        int contentHeight = minDimension + getPaddingTop() + getPaddingBottom();

        int measuredWidth = resolveSize(contentWidth, widthMeasureSpec);
        int measuredHeight = resolveSize(contentHeight, heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mZoomMode)
            return handleZoomAndTransEvent(event);
        if (event.getPointerCount() > 1)
            return false;
        float scaledX = (event.getX() - mDrawingTranslationX) / mScaleFactor;
        float scaledY = (event.getY() - mDrawingTranslationY) / mScaleFactor;
        event.setLocation(scaledX, scaledY);
        mDrawingPerformer.onTouch(event);
        invalidate();
        return true;
    }

    private int mPointerId;
    private boolean translateAction = true;
    public boolean handleZoomAndTransEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP && event.getPointerCount() == 1)
            return false;
        if (event.getPointerCount() > 1){
            translateAction = false;
            mScaleGestureDetector.onTouchEvent(event);
        }else if (translateAction)
            switch (event.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    mPointerId = event.getPointerId(0);
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_CANCEL:
                    int pointerIndex = event.findPointerIndex(mPointerId);
                    if (pointerIndex != -1) {
                        mDrawingTranslationX += event.getX(pointerIndex) - mLastX[0];
                        mDrawingTranslationY += event.getY(pointerIndex) - mLastY[0];
                    }
                    break;
            }
        if (event.getActionMasked() == MotionEvent.ACTION_UP)
            translateAction = true; // reset

        mLastX[0] = event.getX(0);
        mLastY[0] = event.getY(0);
        if (event.getPointerCount() > 1) {
            mLastX[1] = event.getX(1);
            mLastY[1] = event.getY(1);
        }

        checkBounds();
        invalidate();
        return true;
    }

    public Bitmap exportDrawing(){
        Bitmap bitmap = Bitmap.createBitmap(
                mDrawingBitmap.getWidth(),
                mDrawingBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(mBGColor);
        canvas.drawBitmap(mBGBitmap, 0, 0, null);
        canvas.drawBitmap(mDrawingBitmap, 0, 0, null);
        return bitmap;
    }

    public Bitmap exportDrawingWithoutBackground(){
        return mDrawingBitmap;
    }

    public void setDrawingBackground(int color){
        mBGColor = color;
        invalidate();
    }

    public void setUndoAndRedoEnable(boolean enabled){
        if (enabled)
            mActionStack = new ActionStack();
        else
            mActionStack = null;
    }

    /**
     * Set an image as a background so you can draw on top of it. NOTE that calling this method is
     * going to clear anything drawn previously and you will not be able to restore anything with undo().
     * @param bitmap to be used as a background image.
     */
    public void setBackgroundImage(Bitmap bitmap) {
        privateSetBGBitmap(bitmap);
        initializeDrawingBitmap(mBGBitmap.getWidth(), mBGBitmap.getHeight());
        if (mActionStack != null) //if undo and redo is enabled, remove the old actions by creating a new instance.
            mActionStack = new ActionStack();
        invalidate();
    }

    public int getDrawingBackground() {
        return mBGColor;
    }

    public void resetZoom(){
        //if the bitmap is smaller than the view zoom in to make the bitmap fit the view
        float targetSF = calcAppropriateScaleFactor(mDrawingBitmap.getWidth(), mDrawingBitmap.getHeight());

        //align the bitmap in the center
        float targetX = (getWidth() - mDrawingBitmap.getWidth() * targetSF) / 2;
        float targetY = (getHeight() - mDrawingBitmap.getHeight() * targetSF) / 2;

        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(this, "scaleFactor", mScaleFactor, targetSF);
        ObjectAnimator xTranslationAnimator
                = ObjectAnimator.ofFloat(this, "drawingTranslationX", mDrawingTranslationX, targetX);
        ObjectAnimator yTranslationAnimator
                = ObjectAnimator.ofFloat(this, "drawingTranslationY", mDrawingTranslationY, targetY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleAnimator, xTranslationAnimator, yTranslationAnimator);
        animatorSet.start();
    }

    /**
     * This method clears the drawing bitmap. If this method is called consecutively only the first
     * call will take effect.
     * @return true if the canvas cleared successfully.
     */
    public boolean clear() {
        if (mCleared)
            return false;
        Rect rect = new Rect(
                0,
                0,
                mDrawingBitmap.getWidth(),
                mDrawingBitmap.getHeight()
        );
        if (mActionStack != null){
            DrawingAction drawingAction = new DrawingAction(
                    Bitmap.createBitmap(mDrawingBitmap),
                    rect
            );
            mActionStack.addAction(drawingAction);
        }
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
        mCleared = true;
        return true;
    }

    public boolean isCleared() {
        return mCleared;
    }

    Brushes getBrushes() {
        return mBrushes;
    }

    public boolean undo(){
        if (mActionStack == null)
            throw new IllegalStateException("Undo functionality is disable you can enable it by calling setUndoAndRedoEnable(true)");
        if (mActionStack.isUndoStackEmpty() || mDrawingPerformer.isDrawing())
            return false;
        DrawingAction previousAction = mActionStack.previous();
        DrawingAction oppositeAction = getOppositeAction(previousAction);
        mActionStack.addActionToRedoStack(oppositeAction);
        performAction(previousAction);
        return true;
    }

    public boolean redo(){
        if (mActionStack == null)
            throw new IllegalStateException("Redo functionality is disable you can enable it by calling setUndoAndRedoEnable(true)");
        if (mActionStack.isRedoStackEmpty() || mDrawingPerformer.isDrawing())
            return false;
        DrawingAction nextAction = mActionStack.next();
        DrawingAction oppositeAction = getOppositeAction(nextAction);
        mActionStack.addActionToUndoStack(oppositeAction);
        performAction(nextAction);
        return true;
    }

    public boolean isUndoStackEmpty(){
        if (mActionStack == null)
            throw new IllegalStateException("Undo functionality is disable you can enable it by calling setUndoAndRedoEnable(true)");
        return mActionStack.isUndoStackEmpty();
    }

    public boolean isRedoStackEmpty(){
        if (mActionStack == null)
            throw new IllegalStateException("Undo functionality is disable you can enable it by calling setUndoAndRedoEnable(true)");
        return mActionStack.isRedoStackEmpty();
    }

    /**
     * Return an instance of BrushSetting, you can use it to change the selected brush. And change
     * the size of the selected brush and the color.
     * @return an instance of BrushSetting associated with this DrawingView.
     */
    public BrushSettings getBrushSettings() {
        return mBrushes.getBrushSettings();
    }

    /**
     * Enter the zoom mode to be able to zoom and move the drawing. Note that you cannot enter
     * the zoom mode if the the user is drawing.
     * @return true if enter successfully, false otherwise.
     */
    public boolean enterZoomMode() {
        if (mDrawingPerformer.isDrawing())
            return false;
        mZoomMode = true;
        return true;
    }

    /**
     * Exit the zoom mode to be able to draw.
     */
    public void exitZoomMode() {
        mZoomMode = false;
    }

    public boolean isInZoomMode() {
        return mZoomMode;
    }

    /**
     * Set a listener to be notified whenever a new stroke or a point is drawn.
     */
    public void setOnDrawListener(OnDrawListener onDrawListener) {
        mOnDrawListener = onDrawListener;
    }

    public float getDrawingTranslationX() {
        return mDrawingTranslationX;
    }

    public void setDrawingTranslationX(float drawingTranslationX) {
        mDrawingTranslationX = drawingTranslationX;
        invalidate();
    }

    public float getDrawingTranslationY() {
        return mDrawingTranslationY;
    }

    public void setDrawingTranslationY(float drawingTranslationY) {
        mDrawingTranslationY = drawingTranslationY;
        invalidate();
    }

    public float getScaleFactor() {
        return mScaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        mScaleFactor = scaleFactor;
        invalidate();
    }

    private void performAction(DrawingAction action) {
        mCleared = false;
        mCanvas.drawBitmap(
                action.mBitmap,
                action.mRect.left,
                action.mRect.top,
                mSrcPaint
        );
        invalidate();
    }

    private DrawingAction getOppositeAction(DrawingAction action){
        Rect rect = action.mRect;
        Bitmap bitmap = Bitmap.createBitmap(
                mDrawingBitmap,
                rect.left,
                rect.top,
                rect.right - rect.left,
                rect.bottom - rect.top
        );
        return new DrawingAction(bitmap, rect);
    }

    protected void checkBounds(){
        int width = mDrawingBitmap.getWidth();
        int height = mDrawingBitmap.getHeight();
        
        int contentWidth = (int) (width * mScaleFactor);
        int contentHeight = (int) (height * mScaleFactor);

        float widthBound = getWidth()/6;
        float heightBound = getHeight()/6;

        if (contentWidth < widthBound){
            if (mDrawingTranslationX < -contentWidth/2)
                mDrawingTranslationX = -contentWidth/2f;
            else if (mDrawingTranslationX > getWidth() - contentWidth/2)
                mDrawingTranslationX = getWidth() - contentWidth/2f;
        } else if (mDrawingTranslationX > getWidth() - widthBound)
            mDrawingTranslationX = getWidth() - widthBound;
        else if (mDrawingTranslationX + contentWidth < widthBound)
            mDrawingTranslationX = widthBound -  contentWidth;

        if (contentHeight < heightBound){
            if (mDrawingTranslationY < -contentHeight/2)
                mDrawingTranslationY = -contentHeight/2f;
            else if (mDrawingTranslationY > getHeight() - contentHeight/2)
                mDrawingTranslationY = getHeight() - contentHeight/2f;
        }else if (mDrawingTranslationY > getHeight() - heightBound)
            mDrawingTranslationY = getHeight() - heightBound;
        else if (mDrawingTranslationY + contentHeight < heightBound)
            mDrawingTranslationY = heightBound -  contentHeight;
    }

    private void initializeDrawingBitmap(int w, int h) {
        mDrawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mDrawingBitmap);
        if (mDrawingPerformer == null){
            mDrawingPerformer = new DrawingPerformer(mBrushes);
            mDrawingPerformer.setPaintPerformListener(new MyDrawingPerformerListener());
        }
        mDrawingPerformer.setWidthAndHeight(w, h);
    }

    private void initializeAttributes(AttributeSet attrs){
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DrawingView,
                0, 0);
        try {
            BrushSettings settings = mBrushes.getBrushSettings();
            int brushColor = typedArray.getColor(R.styleable.DrawingView_brush_color, 0xFF000000);
            settings.setColor(brushColor);
            int selectedBrush = typedArray.getInteger(R.styleable.DrawingView_brush, Brushes.PENCIL);
            settings.setSelectedBrush(selectedBrush);
            float size = typedArray.getFloat(R.styleable.DrawingView_brush_size, 0.5f);
            if (size < 0 || size > 1)
                throw new IllegalArgumentException("DrawingView brush_size attribute should have a value between 0 and 1 in your xml file");
            settings.setSelectedBrushSize(size);

            mBGColor = typedArray.getColor(R.styleable.DrawingView_drawing_background_color, -1);//default to white

        } finally {
            typedArray.recycle();
        }
    }

    private void privateSetBGBitmap(Bitmap bitmap){
        float canvasWidth = getWidth() - getPaddingStart() - getPaddingEnd();
        float canvasHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();
        float scaleFactor = 1;
        //if the bitmap is smaller than the view -> find a scale factor to scale it down
        if (bitmapWidth > canvasWidth && bitmapHeight > canvasHeight) {
            scaleFactor = canvasHeight/bitmapHeight;
            if ((int) (bitmap.getWidth() * scaleFactor) > canvasWidth)
                scaleFactor = canvasWidth/bitmapWidth;
        } else if (bitmapWidth > canvasWidth && bitmapHeight < canvasHeight)
            scaleFactor = canvasWidth/bitmapWidth;
        else if (bitmapWidth < canvasWidth && bitmapHeight > canvasHeight)
            scaleFactor = canvasHeight/bitmapHeight;

        if (scaleFactor != 1)//if the bitmap is larger than the view scale it down
            bitmap = Utilities.resizeBitmap(bitmap, ((int) (bitmap.getWidth() * scaleFactor)), (int) (bitmap.getHeight() * scaleFactor));

        mBGBitmap = bitmap;

        mScaleFactor = calcAppropriateScaleFactor(mBGBitmap.getWidth(), mBGBitmap.getHeight());

        //align the bitmap in the center
        mDrawingTranslationX = (canvasWidth - mBGBitmap.getWidth() * mScaleFactor)/2;
        mDrawingTranslationY = (canvasHeight - mBGBitmap.getHeight() * mScaleFactor)/2;
    }

    private float calcAppropriateScaleFactor(int bitmapWidth, int bitmapHeight){
        float canvasWidth = getWidth() - getPaddingStart() - getPaddingEnd();
        float canvasHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (bitmapWidth < canvasWidth && bitmapHeight < canvasHeight){
            float scaleFactor;
            //if the bitmap is smaller than the view -> zoom in
            scaleFactor = canvasHeight/bitmapHeight;//let us zoom in to make the bitmap height appears equal to the view height
            if (bitmapWidth * scaleFactor > canvasWidth){
                //OHHH NO,  the bitmap width is larger than the view width, let us make the scale factor depends
                // on the width and for sure the bitmap height will appear less than the view height.
                scaleFactor = canvasWidth/bitmapWidth;
            }
             return scaleFactor;
        }else { //otherwise just make sure the scale is 1
            return 1f;
        }
    }

    private class MyDrawingPerformerListener implements DrawingPerformer.DrawingPerformerListener{

        @Override
        public void onDrawingPerformed(Bitmap bitmap, Rect rect) {
            mCleared = false;
            if (mActionStack !=  null)
                storeAction(rect);
            mCanvas.drawBitmap(bitmap, rect.left, rect.top, null);
            invalidate();
            if (mOnDrawListener != null)
                mOnDrawListener.onDraw();
        }

        @Override
        public void onDrawingPerformed(Path path, Paint paint, Rect rect) {
            mCleared = false;
            if (mActionStack !=  null)
                storeAction(rect);
            mCanvas.drawPath(path, paint);
            invalidate();
            if (mOnDrawListener != null)
                mOnDrawListener.onDraw();
        }

        private void storeAction(Rect rect) {
            Bitmap bitmap = Bitmap.createBitmap(
                    mDrawingBitmap,
                    rect.left,
                    rect.top,
                    rect.right - rect.left,
                    rect.bottom - rect.top
            );
            DrawingAction action = new DrawingAction(bitmap, rect);
            mActionStack.addAction(action);
        }
    }
}
