package com.raed.drawingview.brushes;


import java.util.ArrayList;
import java.util.List;

public class BrushSettings {

    private Brushes mBrushes;

    private int mSelectedBrush = Brushes.PENCIL;
    private int mColor = 0xff000000;//default to black

    private List<BrushSettingListener> mListeners = new ArrayList<>();

    public interface BrushSettingListener{
        void onSettingsChanged();
    }

    BrushSettings(Brushes brushes) {
        mBrushes = brushes;
    }

    public int getSelectedBrush() {
        return mSelectedBrush;
    }

    public void setSelectedBrush(int selectedBrush) {
        mSelectedBrush = selectedBrush;
        mBrushes.getBrush(mSelectedBrush).setColor(mColor);
        notifyListeners();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
        Brush selectedBrush = mBrushes.getBrush(mSelectedBrush);
        selectedBrush.setColor(mColor);
        notifyListeners();
    }

    /**
     * Set the size of the currently selected brush.
     * @param size The value of the new size, it should be a value between 0 and 1. 0 maps to
     *             {@link Brush#getMinSizeInPixel() and 1 maps to {@link Brush#getMaxSizeInPixel()}.
     */
    public void setSelectedBrushSize(float size){
        setBrushSize(mSelectedBrush, size);
    }

    /**
     * Return the currently selected brush size. Note that the size is a value between 0 and 1.
     * @return the size of the currently selected brush.
     */
    public float getSelectedBrushSize(){
        return getBrushSize(mSelectedBrush);
    }

    /**
     * Set the size of the currently selected brush.
     * @param size this should be between 0 and 1.
     * @param brushID the id of the brush, you can get it from {@link Brushes}
     */
    public void setBrushSize(int brushID, float size){
        if (size > 1 || size < 0)
            throw new IllegalArgumentException("Size must be between 0 and 1");
        mBrushes.getBrush(brushID).setSizeInPercentage(size);
        notifyListeners();
    }

    public float getBrushSize(int brushID){
        return mBrushes.getBrush(brushID).getSizeInPercentage();
    }

    Brushes getBrushes() {
        return mBrushes;
    }

    public void addBrushSettingListener(BrushSettingListener listener) {
        mListeners.add(listener);
    }

    private void notifyListeners(){
        for (BrushSettingListener listener : mListeners)
            listener.onSettingsChanged();
    }
}
