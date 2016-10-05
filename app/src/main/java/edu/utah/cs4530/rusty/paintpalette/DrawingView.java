package edu.utah.cs4530.rusty.paintpalette;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rusty Griggs on 9/16/2016.
 *
 * The view where you can draw with your finger and it is drawn on the screen.
 */
public class DrawingView extends View {
    public DrawingView(Context context) {
        super(context);
    }

    /**
     * Adds a path to the current drawing.
     * @param linePath is the path to be drawn.
     * @param strokeColor is the color in which the path will be drawn.
     */
    public void drawNewPath(Path linePath, int strokeColor) {
        _pathList.add(linePath);
        _colorList.add(strokeColor);
        invalidate();
    }

    /**
     * This method will draw a new drawing.
     * @param pathList is the list of paths to be drawn.
     * @param colorList is the list of colors associated with each path to be drawn.
     */
    public void drawNewDrawing(List<Path> pathList, List<Integer> colorList) {
        _pathList = pathList;
        _colorList = colorList;
        invalidate();
    }

    /**
     * This method will create a blank canvas with which to draw.
     */
    public void clearCanvas() {
        _pathList.clear();
        _colorList.clear();
        invalidate();
    }

    /**
     * Listener for when the path ends.
     */
    public interface OnPathEndedListener {
        void onPathEnded(List<PointF> points, int paintColor, Path path);
    }

    //concurrent arrays to store the paths and colors together
    List<Path> _pathList = new ArrayList<>();
    List<Integer> _colorList = new ArrayList<>();
    Path _linePath = new Path();
    int _paintColor;
    List<PointF> _points = new ArrayList<>();

    /**
     * getter for the OnPathEndedListener
     * @return
     */
    public OnPathEndedListener getOnPathEndedListener() {
        return _onPathEndedListener;
    }

    /**
     * Setter for the OnPathEndedListener
     * @param _onPathEndedListener
     */
    public void setOnPathEndedListener(OnPathEndedListener _onPathEndedListener) {
        this._onPathEndedListener = _onPathEndedListener;
    }

    OnPathEndedListener _onPathEndedListener = null;

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < _colorList.size(); i++) {
            Paint paintColor = new Paint(Paint.ANTI_ALIAS_FLAG);
            paintColor.setColor(_colorList.get(i));
            paintColor.setStrokeWidth(5);
            paintColor.setStyle(Paint.Style.STROKE);
            canvas.drawPath(_pathList.get(i), paintColor);
        }
        super.onDraw(canvas);
    }

    /**
     * Called whenever the user touches the screen. Most of the logic for drawing paths is found here.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchPointX = event.getX();
        float touchPointY = event.getY();
        PointF point = new PointF(touchPointX, touchPointY);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _linePath = new Path();
            _linePath.moveTo(touchPointX, touchPointY);
            _points.add(point);
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            _linePath.lineTo(touchPointX, touchPointY);
            _pathList.add(_linePath);
            _colorList.add(_paintColor);
            _points.add(point);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (_onPathEndedListener != null) {
                _onPathEndedListener.onPathEnded(_points, _paintColor, _linePath);
            }
            _points.clear();
        }

        invalidate();
        return true;
    }

    /**
     * Will set the active color with which to draw the paths.
     * @param paintColor
     */
    public void setActiveColor(int paintColor) {
        _paintColor = paintColor;
    }

    public int getActiveColor() {
        return _paintColor;
    }
}
