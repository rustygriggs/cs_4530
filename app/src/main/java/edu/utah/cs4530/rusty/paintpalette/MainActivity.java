package edu.utah.cs4530.rusty.paintpalette;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is the main controller for the program that interacts with the Data model (Gallery)
 * and the View(s).
 */
public class MainActivity extends AppCompatActivity implements Button.OnClickListener,
                                                               DrawingView.OnPathEndedListener,
                                                               ValueAnimator.AnimatorUpdateListener {

    static final int PICK_PAINT_REQUEST = 0;
    public static final String SPLOTCH_COLOR = "splotchColor";
    public static final String PAINT_PALETTE_FILE_NAME = "paint_palette_file.dat";

    DrawingView _drawingView = null;
    private int _currentDrawingIndex = 0;
    private List<Path> _animatorPathList = null;
    private List<Integer> _animatorColorList = null;
    private final ValueAnimator _drawingAnimator = new ValueAnimator();
    private  Button _backButton;
    private  Button _forwardButton;
    private  Button _animationButton;
    private  ImageButton _colorButton;
    private  ImageButton _deleteDrawingButton;

    /**
     * This method is called when the activity is first created.
     * @param savedInstanceState
     */
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

        _colorButton = new ImageButton(this);
        _colorButton.setBackgroundColor(Color.BLACK);
        _colorButton.setOnClickListener(this);

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

        _drawingView.setOnPathEndedListener(this);

        _forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    /**
     * This method will save the Gallery to disk using a Serializable fileStream
     */
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

    /**
     * This method extracts a Gallery from a file saved to disk.
     * @param paintPaletteFileName is the file name where the Gallery is saved.
     * @return the Gallery that was previously saved to disk.
     */
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

    /**
     * This converts the drawings saved in the Gallery as a series of strokes to Paths which can
     * be understood by the drawingView.
     * @param currentDrawing is the currentDrawing to be converted and drawn on the screen.
     */
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

    /**
     * This method is invoked when the _colorButton is clicked.
     * @param view
     */
    @Override
    public void onClick(View view) {
        Intent showPaintPicker = new Intent();
        showPaintPicker.setClass(this, PaintPickerActivity.class);
        startActivityForResult(showPaintPicker, PICK_PAINT_REQUEST);
    }

    /**
     * This method is invoked when the PaintPickerActivity is killed and the results, the color
     * that was chosen by the user, are extracted.
     * @param requestCode should be the same as when the request was made.
     * @param resultCode should be OK if all went well.
     * @param data contains the color as an intExtra.
     */
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

    /**
     * This listener method is called whenever a path being drawn is finshed. It will convert the Path
     * to a Stroke and add it to the drawing.
     * @param points is the list of points to be converted.
     * @param paintColor is the color associated with the path/stroke.
     * @param linePath is the path to be converted, also added to the animator path list.
     */
    @Override
    public void onPathEnded(List<PointF> points, int paintColor, Path linePath) {
        if (_currentDrawingIndex == Gallery.getInstance().getDrawingCount()) {
            Gallery.getInstance().addNewDrawing();
            _forwardButton.setEnabled(true);
            _deleteDrawingButton.setEnabled(true);
        }

        Stroke stroke = new Stroke();

        for (PointF pathPoint : points) {
            edu.utah.cs4530.rusty.paintpalette.Point strokePoint = new Point();
            strokePoint.x = pathPoint.x / Resources.getSystem().getDisplayMetrics().density;
            strokePoint.y = pathPoint.y / Resources.getSystem().getDisplayMetrics().density;
            stroke.addPoint(strokePoint);
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

    /**
     * This method will animate the current drawing. It shows each stroke being drawn over a series
     * of 5 seconds.
     */
    private void animate() {
        _drawingAnimator.setDuration(5000); //set duration for 5 seconds
        _drawingAnimator.setIntValues(0, Gallery.getInstance().getDrawing(_currentDrawingIndex).getStrokeCount() - 1);
        _drawingAnimator.addUpdateListener(this);
        _drawingView.clearCanvas();
        _drawingAnimator.start();
    }

    /**
     * This is the listener for the animate() method. Whenever the valueAnimator is invoked, this
     * method is called.
     * @param valueAnimator is how you get the animation value.
     */
    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        int animationValue = (int) valueAnimator.getAnimatedValue();

        _drawingView.drawNewPath(_animatorPathList.get(animationValue), _animatorColorList.get(animationValue));
    }
}
