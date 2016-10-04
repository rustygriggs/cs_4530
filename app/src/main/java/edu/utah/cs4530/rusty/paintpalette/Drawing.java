package edu.utah.cs4530.rusty.paintpalette;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rusty on 9/29/2016.
 */
public class Drawing implements Serializable{

    List<Stroke> _strokes = new ArrayList<>();

    int getStrokeCount() {
        return _strokes.size();
    }

    Stroke getStroke(int strokeIndex) {
        return _strokes.get(strokeIndex);
    }

    void addStroke(Stroke stroke) {
        _strokes.add(stroke);
    }
}
