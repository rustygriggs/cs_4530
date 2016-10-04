package edu.utah.cs4530.rusty.paintpalette;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SplotchView.OnSplotchSelectedListener,
                                                               Button.OnClickListener,
                                                               DrawingView.OnPathEndedListener,
                                                               ValueAnimator.AnimatorUpdateListener {

    static final int PICK_PAINT_REQUEST = 0;
    public static final String SPLOTCH_COLOR = "splotchColor";
    public static final String PAINT_PALETTE_FILE_NAME = "paint_palette_file.dat";

    DrawingView _drawingView = null;
    SplotchView _colorButton = null;
    private int _currentDrawingIndex = 0;
    private boolean _animatorMode = false;
    private List<Path> _animatorPathList = null;
    private List<Integer> _animatorColorList = null;
    private final ValueAnimator _drawingAnimator = new ValueAnimator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Gallery.setInstance(loadFromFile(PAINT_PALETTE_FILE_NAME));

        if (Gallery.getInstance().getDrawingCount() == 0) {
            Gallery.getInstance().addNewDrawing();
        }

        LinearLayout rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.HORIZONTAL);
        setContentView(rootLayout);

        final TextView drawingNumberView = new TextView(this);
        drawingNumberView.setText("1");


        final Button backButton = new Button(this);
        backButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        backButton.setText("←");

        final Button forwardButton = new Button(this);
        forwardButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        forwardButton.setText("→");
        if (_currentDrawingIndex == 0) {
            backButton.setEnabled(false);
        }
        else if (_currentDrawingIndex < Gallery.getInstance().getDrawingCount() + 1){
            forwardButton.setEnabled(true);
        }
        else {
            backButton.setEnabled(true);
        }
        final Button animationButton = new Button(this);
        animationButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        animationButton.setText("►");
        //SplotchView colorButton = new SplotchView(this);
        //colorButton.setOnSplotchSelectedListener(this);

        final Button colorButton = new Button(this);
        colorButton.setText("Color");
        colorButton.setOnClickListener(this);
        //colorButton.setSplotchColor(Color.BLACK);

        final Button deleteDrawingButton = new Button(this);
        deleteDrawingButton.setText("Delete Drawing");

        LinearLayout sideButtonLayout = new LinearLayout(this);
        sideButtonLayout.setOrientation(LinearLayout.VERTICAL);
        sideButtonLayout.setBackgroundColor(Color.BLUE);
        sideButtonLayout.addView(drawingNumberView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(backButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(forwardButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(animationButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(colorButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(deleteDrawingButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        rootLayout.addView(sideButtonLayout, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        _drawingView = new DrawingView(this);
        _drawingView.setBackgroundColor(Color.WHITE);
        _drawingView.setActiveColor(Color.BLACK);

        rootLayout.addView(_drawingView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 5));

        //_drawingView.manualInvalidate(); //TODO: somehow get the screen to show up on the first opening of the app

        _drawingView.setOnPathEndedListener(this);

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Gallery.getInstance().addNewDrawing();
                backButton.setEnabled(true);
                convertToPathAndDraw(Gallery.getInstance().getDrawing(++_currentDrawingIndex));
                drawingNumberView.setText("" + (_currentDrawingIndex + 1));
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawing prevDrawing = Gallery.getInstance().getDrawing(--_currentDrawingIndex);
                convertToPathAndDraw(prevDrawing);
                if (_currentDrawingIndex == 0) {
                    backButton.setEnabled(false);
                }
                drawingNumberView.setText("" + (_currentDrawingIndex + 1));
            }
        });

        deleteDrawingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Gallery.getInstance().getDrawingCount() > 0) {
                    Gallery.getInstance().removeDrawing(_currentDrawingIndex++);
                }
                saveToFile();
                //_drawingView.clearCanvas();
                convertToPathAndDraw(Gallery.getInstance().getDrawing(_currentDrawingIndex));
                drawingNumberView.setText("" + (_currentDrawingIndex + 1));
            }
        });

        animationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animate();
            }
        });

    }

    private Gallery loadFromFile(String paintPaletteFileName) {
        Gallery gallery = null;
        try {
            FileInputStream fis = openFileInput(paintPaletteFileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            gallery = (Gallery) ois.readObject();
            ois.close();
        }
        catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return gallery;
    }

    private void convertToPathAndDraw(Drawing currentDrawing) {
        List<Path> pathList = new ArrayList<>();
        List<Integer> colorList = new ArrayList<>();
        Path linePath;
        for (int strokeIndex = 0; strokeIndex < currentDrawing.getStrokeCount(); strokeIndex++) {
            Stroke stroke = currentDrawing.getStroke(strokeIndex);
            linePath = new Path();
            int strokeColor = stroke.getColor();
            for (int pointIndex = 0; pointIndex < stroke.getPointCount(); pointIndex++) {
                Point point = stroke.getPoint(pointIndex);
                if (pointIndex == 0) {
                    linePath.moveTo(point.x, point.y);
                }
                else {
                    linePath.lineTo(point.x, point.y);
                }
            }
            pathList.add(linePath);
            colorList.add(strokeColor);
        }
        if (!_drawingAnimator.isRunning()) {
            drawConvertedDrawing(pathList, colorList);
        }
        _animatorPathList = new ArrayList<>(pathList);
        _animatorColorList = new ArrayList<>(colorList);
    }

    private void drawConvertedDrawing(List<Path> pathList, List<Integer> colorList) {
        _drawingView.drawNewDrawing(pathList, colorList);
    }

    @Override
    public void onClick(View view) {
        Intent showPaintPicker = new Intent();
        showPaintPicker.setClass(this, PaintPickerActivity.class);
        startActivityForResult(showPaintPicker, PICK_PAINT_REQUEST); //not sure what the second param is supposed to be
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PAINT_REQUEST) {
            if (resultCode == RESULT_OK) {
                int splotchColor = data.getIntExtra(SPLOTCH_COLOR, 0);
                _drawingView.setActiveColor(splotchColor);
                //_colorButton.setBackgroundColor(splotchColor);
            }
        }

    }

    @Override
    public void onSplotchSelected(int splotchColor, SplotchView splotchView) {
        Intent showPaintPicker = new Intent();
        showPaintPicker.setClass(this, PaintPickerActivity.class);
        startActivityForResult(showPaintPicker, PICK_PAINT_REQUEST); //not sure what the second param is supposed to be
    }

    @Override
    public void onPathEnded(List<PointF> points, int paintColor, Path linePath) {
        Log.i("Path Ended", "There are " + points.size() + " points in this line. Color is " + paintColor);
        Stroke stroke = new Stroke();
        for (PointF pathPoint : points) {
            edu.utah.cs4530.rusty.paintpalette.Point strokePoint = new Point();
            // TODO: convert coordinate  - should be in inches and stuff, not pixels
            strokePoint.x = pathPoint.x;
            strokePoint.y = pathPoint.y;
            stroke.addPoint(strokePoint);
        }
        stroke.setColor(paintColor);
        Gallery.getInstance().addStrokeToDrawing(_currentDrawingIndex, stroke);
        _animatorPathList.add(linePath);
        _animatorColorList.add(paintColor);
        saveToFile();
    }

    void saveToFile() {
        try {
            FileOutputStream fos = openFileOutput(PAINT_PALETTE_FILE_NAME, MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(Gallery.getInstance());
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void animate() {
        _drawingAnimator.setDuration(5000); //set duration for 5 seconds
        _drawingAnimator.setIntValues(0, Gallery.getInstance().getDrawing(_currentDrawingIndex).getStrokeCount() - 1);
        _drawingAnimator.addUpdateListener(this);
        _drawingView.clearCanvas();
        _drawingAnimator.start();

    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        int animationValue = (int) valueAnimator.getAnimatedValue();
        Log.i("Animation", "Current Animation value: " + animationValue);

        _drawingView.drawNewPath(_animatorPathList.get(animationValue), _animatorColorList.get(animationValue));
        //TODO: fix this. it's causing the animated picture to stay the picture always. see line 190 for reference
        if (valueAnimator.isRunning()) {
            _animatorMode = true;
        }
        else {
            _animatorMode = false;
        }
    }
}
