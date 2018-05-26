# DrawingView For Android
## Description
DrawingView allows the user to draw with different brushes and provides some features.
## Features
* **Provide multible brushes**
* **Drawing on top of images**
* **Undo/redo operations**
* **Zooming in/out & translation**
* **Preview the selected brush in BrushView**
* **Custom background color**
* **Export a drawing as a Bitmap**
## Demo
[https://www.youtube.com/watch?v=ApqwoVbGQv0](https://www.youtube.com/watch?v=ApqwoVbGQv0)

![Screenshot](https://github.com/Raed-Mughaus/DrawingView/blob/master/screenshots/screenshot.png)

## Download
Gradle:
```groovy
implementation 'com.raed.drawingview:drawingview:1.1-beta'
```
Maven:
```xml
<dependency>
  <groupId>com.raed.drawingview</groupId>
  <artifactId>drawingview</artifactId>
  <version>1.1-beta</version>
  <type>pom</type>
</dependency>
```
## Usage Guide
Add the following to your layout file:
```xml
<com.raed.drawingview.DrawingView
        android:id="@+id/drawing_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:brush="calligraphy"
        app:brush_size="1"
        app:brush_color="#2187bb"
        app:drawing_background_color="#dddddd"
        />
```
brush_size value should be between 0 and 1, otherwise an exception will be thrown.
### BrushSettings:
You can use the BrushSettings to:
- Change the brush size
- Change the brush color
- Change the selected brush
```java
//Obtain BrushSettings from your DrawingView
BrushSettings brushSettings = mDrawingView.getBrushSettings();

//Change the selected brush
brushSettings.setSelectedBrush(Brushes.PENCIL);

//Set the size for the pencil, pass a value between 0 and 1
brushSettings.setSelectedBrushSize(0.3f);

//Change the color for all brushes
brushSettings.setColor(0xFF000000);
```
### Enable undo and redo functionality:
Undo and redo are disabled by default. You can enable them by calling:
```java
mDrawingView.setUndoAndRedoEnable(true);
```
And here is an example of a complete implementation:
```java
mDrawingView.setUndoAndRedoEnable(true);

mUndoButton.setOnClickListener(view -> {
    mDrawingView.undo();
    mUndoButton.setEnabled(!mDrawingView.isUndoStackEmpty());
    mRedoButton.setEnabled(!mDrawingView.isRedoStackEmpty());
});
        
mRedoButton.setOnClickListener(view -> {
    mDrawingView.redo();
    mUndoButton.setEnabled(!mDrawingView.isUndoStackEmpty());
    mRedoButton.setEnabled(!mDrawingView.isRedoStackEmpty());
});

//Whenever the user draw somthing this method will be invoked.
mDrawingView.setOnDrawListener(view -> {
    mUndoButton.setEnabled(true);
    mRedoButton.setEnabled(false);
});
```
And remember to update your undo button when you call clear() or setBackgroundImage():
```java
mDrawingView.setBackgroundImage(bitmap);
mUndoButton.setEnable(false);
mRedoButton.setEnable(false);
```

```java
mDrawingView.clear();
mUndoButton.setEnabled(!mDrawingView.isUndoStackEmpty());
mRedoButton.setEnabled(!mDrawingView.isRedoStackEmpty());
```

### Drawing on images:
You can draw on top of an image by calling the following method:
```java
Bitmap photo = ...
mDrawingView.setBackgroundImage(photo);
```
But please remember that calling this method **clears any previous drawings if any**, and if the image is larger than the view it will be scaled down.
### How to get your drawing?
In most casses you want to use this method to get your drawing:
```java
Bitmap bitmap = mDrawingView.exportDrawing();
```
But if you are only intersted in the drawing without the background color and image you can use:
```java
Bitmap bitmap = mDrawingView.exportDrawingWithoutBackground();
```
### Zoom in/out
When the DrawingView is in the Zoom Mode it does not draw anything, touch events are used to zoom and move the drawing.
The following code show you how to enter and exit the Zoom Mode
```java
zoomModeButton.setOnClickListener(v -> {
    if (mDrawingView.isInZoomMode()) {
        mDrawingView.exitZoomMode();
        zoomModeButton.setText(R.string.enter_zoom_mode);
    }else{
        mDrawingView.enterZoomMode();
        zoomModeButton.setText(R.string.exit_zoom_mode);
    }
});
```
### BrushView
You can use the BrushView to show a preview of the selected brush. 
Add the following to your layout file:
```xml
<com.raed.drawingview.BrushView
        android:id="@+id/brush_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:padding="4dp"/>
```
And in your java code:
```java
mBrushView.setDrawingView(mDrawingView);
```

## License

    Copyright 2018 Raed Mughaus

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
