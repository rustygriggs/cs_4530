package edu.utah.cs4530.rusty.paintpalette;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rusty on 9/29/2016.
 */
public class Gallery implements Serializable{
    //Singleton class
    private static Gallery _instance;

    public static Gallery getInstance() {
        //TODO: thread safe?
        if (_instance == null) {
            _instance = new Gallery();
        }
        return _instance;
    }

    public static void setInstance(Gallery gallery) {
        _instance = gallery;
    }

    List<Drawing> _drawingList = new ArrayList<>();

    int getDrawingCount() {
        return _drawingList.size();
    }

    Drawing getDrawing(int drawingIndex) {
        return _drawingList.get(drawingIndex);
    }

    void addNewDrawing() {
        _drawingList.add(new Drawing());
    }

    void addStrokeToDrawing(int drawingIndex, Stroke stroke) {
        _drawingList.get(drawingIndex).addStroke(stroke);
    }

    void removeDrawing(int drawingIndex) {
        _drawingList.remove(drawingIndex);
    }

}
