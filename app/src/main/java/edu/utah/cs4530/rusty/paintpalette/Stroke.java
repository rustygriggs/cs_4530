package edu.utah.cs4530.rusty.paintpalette;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rusty on 10/2/2016.
 *
 * Represents a list of points and color that is a stroke to be drawn.
 */
public class Stroke implements Serializable{

    List<Point> _points = new ArrayList<>();
    int _color = 0xFF000000; //Black (opaque)

    int getPointCount() {
        return _points.size();
    }

    Point getPoint (int pointIndex) {
        return _points.get(pointIndex);
    }

    void addPoint(Point p) {
        _points.add(p);
    }

    public int getColor() {
        return _color;
    }

    public void setColor(int color) {
        _color = color;
    }
}
