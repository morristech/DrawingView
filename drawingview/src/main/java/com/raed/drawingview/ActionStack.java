package com.raed.drawingview;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;


public class ActionStack {

    private static final String TAG = "ActionStack";
    private static final long mMaxSize = Runtime.getRuntime().maxMemory() / 4;
    private long mCurrentSize;

    private List<DrawingAction> mUndoStack = new ArrayList<>();
    private List<DrawingAction> mRedoStack = new ArrayList<>();


    void addAction(DrawingAction action) {
        Log.d(TAG, "Add getAction: " + action);
        if (mRedoStack.size() > 0){ //Clear the redo stack
            for (DrawingAction s:mRedoStack)
                mCurrentSize -= s.getSize();
            mRedoStack.clear();
        }
        addActionToStack(mUndoStack, action);
    }

    void addActionToRedoStack(DrawingAction action){
        Log.d(TAG, "Add getAction to redo stack: " + action);
        addActionToStack(mRedoStack, action);
    }

    void addActionToUndoStack(DrawingAction action){
        Log.d(TAG, "Add getAction to undo stack: " + action);
        addActionToStack(mUndoStack, action);
    }

    DrawingAction previous() {
        return freeLastItem(mUndoStack);
    }

    DrawingAction next() {
        return freeLastItem(mRedoStack);
    }

    boolean isRedoStackEmpty(){
        return mRedoStack.size() == 0;
    }

    boolean isUndoStackEmpty(){
        return mUndoStack.size() == 0;
    }

    private void freeItem(){
        //I do not know weather it is necessary to do this or not, but please do not change it.
        if (mUndoStack.size() >= mRedoStack.size())
            mCurrentSize -= mUndoStack.remove(0).getSize();
        else
            mCurrentSize -= mRedoStack.remove(0).getSize();
    }

    private void addActionToStack(List<DrawingAction> stack, DrawingAction action){
        Log.d(TAG,"MaxSize = " + mMaxSize);
        Log.d(TAG,"Before:CurSize = " + mCurrentSize);
        Log.d(TAG,"Dr+mCSi = " + (mCurrentSize + action.getSize()));
        if (action.getSize() > mMaxSize) {
            //I hope this won't happen :)
            mUndoStack.clear();
            mRedoStack.clear();
            mCurrentSize = 0;
            return;
        }
        while (mCurrentSize + action.getSize() > mMaxSize) {
            freeItem();
        }
        stack.add(action);
        mCurrentSize += action.getSize();
        Log.d(TAG,"After:CurSize = " + mCurrentSize);
    }

    private DrawingAction freeLastItem(List<DrawingAction> list){
        mCurrentSize -= list.get(list.size() - 1).getSize();
        return list.remove(list.size() - 1);
    }
}
