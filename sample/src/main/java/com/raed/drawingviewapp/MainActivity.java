package com.raed.drawingviewapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import com.raed.drawingview.BrushView;
import com.raed.drawingview.DrawingView;
import com.raed.drawingview.brushes.BrushSettings;
import com.raed.drawingview.brushes.Brushes;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMPORT_IMAGE = 10;

    private DrawingView mDrawingView;
    private SeekBar mSizeSeekBar;
    private Button mUndoButton;
    private Button mRedoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawingView = findViewById(R.id.drawing_view);
        mDrawingView.setUndoAndRedoEnable(true);

        BrushView brushView = findViewById(R.id.brush_view);
        brushView.setDrawingView(mDrawingView);

        mSizeSeekBar = findViewById(R.id.size_seek_bar);
        mSizeSeekBar.setMax(100);
        mSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                BrushSettings brushSettings = mDrawingView.getBrushSettings();
                brushSettings.setSelectedBrushSize(i/100f);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawingView.clear()) {
                    mUndoButton.setEnabled(true);
                    mRedoButton.setEnabled(false);
                }
            }
        });

        findViewById(R.id.reset_zoom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawingView.resetZoom();
            }
        });

        final Button zoomModeButton = findViewById(R.id.zoom_mode_button);
        zoomModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawingView.isInZoomMode()) {
                    mDrawingView.exitZoomMode();
                    zoomModeButton.setText(R.string.enterzoommode);
                    return;
                }
                mDrawingView.enterZoomMode();
                zoomModeButton.setText(R.string.exitzoommode);
            }
        });

        findViewById(R.id.set_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_IMPORT_IMAGE);
            }
        });

        setupUndoAndRedo();
        setupBrushes();
        setupColorViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT_IMAGE){
            if (AppCompatActivity.RESULT_OK != resultCode)
                return;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                mDrawingView.setBackgroundImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setBrushSelected(int brushID){
        BrushSettings settings = mDrawingView.getBrushSettings();
        settings.setSelectedBrush(brushID);
        int sizeInPercentage = (int) (settings.getSelectedBrushSize() * 100);
        mSizeSeekBar.setProgress(sizeInPercentage);
    }

    private void setupUndoAndRedo() {
        mUndoButton = findViewById(R.id.undo);
        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawingView.undo();
                mUndoButton.setEnabled(!mDrawingView.isUndoStackEmpty());
                mRedoButton.setEnabled(!mDrawingView.isRedoStackEmpty());
            }
        });

        mRedoButton = findViewById(R.id.redo);
        mRedoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawingView.redo();
                mUndoButton.setEnabled(!mDrawingView.isUndoStackEmpty());
                mRedoButton.setEnabled(!mDrawingView.isRedoStackEmpty());
            }
        });

        mDrawingView.setOnDrawListener(new DrawingView.OnDrawListener() {
            @Override
            public void onDraw() {
                mUndoButton.setEnabled(true);
                mRedoButton.setEnabled(false);
            }
        });
    }

    private void setupColorViews() {
        View.OnClickListener colorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ((ColorDrawable)view.getBackground()).getColor();
                BrushSettings brushSettings = mDrawingView.getBrushSettings();
                brushSettings.setColor(color);
            }
        };
        ViewGroup colorsContainer = findViewById(R.id.brush_colors_container);
        for (View colorView : colorsContainer.getTouchables())
            colorView.setOnClickListener(colorClickListener);

        View.OnClickListener bgClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ((ColorDrawable)view.getBackground()).getColor();
                mDrawingView.setDrawingBackground(color);
            }
        };
        ViewGroup bgColorsContainer = findViewById(R.id.bg_colors_container);
        for (View colorView : bgColorsContainer.getTouchables())
            colorView.setOnClickListener(bgClickListener);
    }

    private void setupBrushes() {
        RadioButton pen = findViewById(R.id.pen);
        pen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) setBrushSelected(Brushes.PEN);
            }
        });

        RadioButton pencil = findViewById(R.id.pencil);
        pencil.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) setBrushSelected(Brushes.PENCIL);
            }
        });

        RadioButton eraser = findViewById(R.id.eraser);
        eraser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) setBrushSelected(Brushes.ERASER);
            }
        });

        RadioButton calligraphy = findViewById(R.id.calligraphy);
        calligraphy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    setBrushSelected(Brushes.CALLIGRAPHY);
            }
        });

        RadioButton airBrush = findViewById(R.id.air_brush);
        airBrush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) setBrushSelected(Brushes.AIR_BRUSH);
            }
        });
    }


}

