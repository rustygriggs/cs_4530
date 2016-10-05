package edu.utah.cs4530.rusty.paintpalette;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ImageButton;
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
    //SplotchView _colorButton = null;
    private int _currentDrawingIndex = 0;
    private boolean _animatorMode = false;
    private List<Path> _animatorPathList = null;
    private List<Integer> _animatorColorList = null;
    private final ValueAnimator _drawingAnimator = new ValueAnimator();
    private  Button _backButton;
    private  Button _forwardButton;
    private  Button _animationButton;
    private ImageButton _colorButton;
    private  ImageButton _deleteDrawingButton;


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


        _backButton = new Button(this);
        _backButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        _backButton.setText("←");

        _forwardButton = new Button(this);
        _forwardButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        _forwardButton.setText("→");
        if (_currentDrawingIndex == 0) {
            _backButton.setEnabled(false);
        }
        else if (_currentDrawingIndex < Gallery.getInstance().getDrawingCount() + 1){
            _forwardButton.setEnabled(true);
            _deleteDrawingButton.setEnabled(true);
        }
        else {
            _backButton.setEnabled(true);
        }

        _animationButton = new Button(this);
        _animationButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        _animationButton.setText("►");
        //SplotchView _colorButton = new SplotchView(this);
        //_colorButton.setOnSplotchSelectedListener(this);

        _colorButton = new ImageButton(this);
        _colorButton.setBackgroundColor(Color.BLACK);
        _colorButton.setOnClickListener(this);
        //_colorButton.setSplotchColor(Color.BLACK);

        _deleteDrawingButton = new ImageButton(this);
        _deleteDrawingButton.setImageResource(R.drawable.small_trash_icon);

        LinearLayout sideButtonLayout = new LinearLayout(this);
        sideButtonLayout.setOrientation(LinearLayout.VERTICAL);
        sideButtonLayout.setBackgroundColor(Color.BLUE);
        sideButtonLayout.addView(_backButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(_forwardButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(_animationButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(_colorButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        sideButtonLayout.addView(_deleteDrawingButton,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        rootLayout.addView(sideButtonLayout, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        _drawingView = new DrawingView(this);
        _drawingView.setBackgroundColor(Color.WHITE);
        _drawingView.setActiveColor(Color.BLACK);

        convertToPathAndDraw(Gallery.getInstance().getDrawing(_currentDrawingIndex));

        rootLayout.addView(_drawingView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 5));

        //_drawingView.manualInvalidate(); //TODO: somehow get the screen to show up on the first opening of the app

        _drawingView.setOnPathEndedListener(this);

        _forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: don't add new drawing here...I think
                //Gallery.getInstance().addNewDrawing();
                _backButton.setEnabled(true);
                _currentDrawingIndex++;
                if (_currentDrawingIndex == Gallery.getInstance().getDrawingCount()) {// if the next drawing isn't drawn yet...
                    _drawingView.clearCanvas();
                    _forwardButton.setEnabled(false);
                    _deleteDrawingButton.setEnabled(false);
                }
                else {
                    convertToPathAndDraw(Gallery.getInstance().getDrawing(_currentDrawingIndex));
                }
            }
        });

        _backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _forwardButton.setEnabled(true);
                _deleteDrawingButton.setEnabled(true);
                Drawing prevDrawing = Gallery.getInstance().getDrawing(--_currentDrawingIndex);
                convertToPathAndDraw(prevDrawing);
                if (_currentDrawingIndex == 0) {
                    _backButton.setEnabled(false);
                }
            }
        });

        _deleteDrawingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Gallery.getInstance().getDrawingCount() > 0) {
                    Gallery.getInstance().removeDrawing(_currentDrawingIndex++);
                    saveToFile();
                    _currentDrawingIndex--;
                    if (_currentDrawingIndex == Gallery.getInstance().getDrawingCount()) {
                        _drawingView.clearCanvas();
                        _forwardButton.setEnabled(false);
                        _deleteDrawingButton.setEnabled(false);
                    }
                    else {
                        convertToPathAndDraw(Gallery.getInstance().getDrawing(_currentDrawingIndex));
                    }
                }
            }
        });

        _animationButton.setOnClickListener(new View.OnClickListener() {
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
                int pixelX = (int) (point.x * Resources.getSystem().getDisplayMetrics().density);
                int pixelY = (int) (point.y * Resources.getSystem().getDisplayMetrics().density);
                if (pointIndex == 0) {
                    linePath.moveTo(pixelX, pixelY);
                }
                else {
                    linePath.lineTo(pixelX, pixelY);
                }
            }
            pathList.add(linePath);
            colorList.add(strokeColor);
        }
        if (!_drawingAnimator.isRunning()) {
            _drawingView.drawNewDrawing(pathList, colorList);
        }
        _animatorPathList = new ArrayList<>(pathList);
        _animatorColorList = new ArrayList<>(colorList);
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
                _colorButton.setBackgroundColor(splotchColor);
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
        //todo: create a new drawing here, and not on the forward button...
        if (_currentDrawingIndex == Gallery.getInstance().getDrawingCount()) {
            Gallery.getInstance().addNewDrawing();
            _forwardButton.setEnabled(true);
            _deleteDrawingButton.setEnabled(true);
        }
        Log.i("Path Ended", "There are " + points.size() + " points in this line. Color is " + paintColor);
        Stroke stroke = new Stroke();
        int density = getResources().getConfiguration().densityDpi;
        int screenHeight = getResources().getConfiguration().screenHeightDp * density;
        int screenWidth = getResources().getConfiguration().screenWidthDp * density;
        int dpHeight = (int) (screenHeight / Resources.getSystem().getDisplayMetrics().density);
        int dpWidth = (int) (screenWidth / Resources.getSystem().getDisplayMetrics().density);
        Log.i("screen dimensions", "Height: " + dpHeight + ". Width: " + dpWidth + ". Density: " + density);
        for (PointF pathPoint : points) {
            edu.utah.cs4530.rusty.paintpalette.Point strokePoint = new Point();
            // TODO: convert coordinate  - should be in inches and stuff, not pixels


            strokePoint.x = pathPoint.x / Resources.getSystem().getDisplayMetrics().density;
            strokePoint.y = pathPoint.y / Resources.getSystem().getDisplayMetrics().density;
            stroke.addPoint(strokePoint);
            Log.i("coordinates", "Coordinates are " + strokePoint.x + ", " + strokePoint.y);
        }
        stroke.setColor(paintColor);
        Gallery.getInstance().addStrokeToDrawing(_currentDrawingIndex, stroke);
        if (_animatorPathList != null) {
            _animatorPathList.add(linePath);
        }
        if (_animatorColorList != null) {
            _animatorColorList.add(paintColor);
        }
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
        disableButtonsAndViews();
        enableButtonsAndViews();
    }

    private void enableButtonsAndViews() {
        _forwardButton.setEnabled(true);
        _backButton.setEnabled(true);
        _colorButton.setEnabled(true);
        _deleteDrawingButton.setEnabled(true);
        _drawingView.setEnabled(true);
        _animationButton.setText("►");
    }

    private void disableButtonsAndViews() {
        _forwardButton.setEnabled(false);
        _backButton.setEnabled(false);
        _colorButton.setEnabled(false);
        _deleteDrawingButton.setEnabled(false);
        _drawingView.setEnabled(false);
        _animationButton.setText("◼");
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        int animationValue = (int) valueAnimator.getAnimatedValue();
        Log.i("Animation", "Current Animation value: " + animationValue);

        _drawingView.drawNewPath(_animatorPathList.get(animationValue), _animatorColorList.get(animationValue));
    }
}
