package edu.utah.cs4530.rusty.paintpalette;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * Created by Rusty on 9/16/2016.
 */
public class KnobView extends android.view.View{

    float _theta = 0.0f;
    RectF _knobRect = new RectF();
    Paint _knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    int _knobColor = -1;

    public KnobView(Context context) {
        super(context);
    }

    public KnobView(Context context, int knobColor) {
        super(context);
        _knobColor = knobColor;
    }

    public int getColor() {
        return _knobPaint.getColor();
    }

    public void setTheta(float theta) {
        _theta = theta;
        invalidate();
    }

    public float getTheta() {
        return _theta;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        PointF touchPoint = new PointF();
        touchPoint.x = event.getX();
        touchPoint.y = event.getY();

        //set theta property
        float theta = (float)Math.atan2(touchPoint.y - _knobRect.centerY(),
                touchPoint.x - _knobRect.centerX());

        //convert radians to degrees and then to rgb 255 values
        int degrees = (int) Math.toDegrees(theta);
        int colorAdjustment = (int) (degrees * .708);

        if (colorAdjustment < 0) {
            colorAdjustment *= -1;
            colorAdjustment = colorAdjustment + ((128 - colorAdjustment) * 2);
        }

        //update each individual knob color
        switch(_knobColor) {
            case 1: updateKnobColor(Color.rgb(colorAdjustment, 0, 0)); break;
            case 2: updateKnobColor(Color.rgb(0, colorAdjustment, 0)); break;
            case 3: updateKnobColor(Color.rgb(0, 0, colorAdjustment)); break;
            default: break;
        }
        setTheta(theta);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        _knobRect.left = getPaddingLeft();
        _knobRect.top = getPaddingTop();
        _knobRect.right = getWidth() - getPaddingRight();
        _knobRect.bottom = _knobRect.right - getPaddingBottom();

        //Center knob
        float offset = (getHeight() - _knobRect.height()) * 0.5f;
        _knobRect.top += offset;
        _knobRect.bottom += offset;

        float knobRadius = _knobRect.height() * 0.4f;

        PointF nibCenter = new PointF();
        nibCenter.x = _knobRect.centerX() + (float)Math.cos((double)_theta) * knobRadius;
        nibCenter.y = _knobRect.centerY() + (float)Math.sin((double)_theta) * knobRadius;

        float nibRadius = knobRadius / 8.0f;

        RectF nibRect = new RectF();
        nibRect.left = nibCenter.x - nibRadius;
        nibRect.top = nibCenter.y - nibRadius;
        nibRect.right = nibCenter.x + nibRadius;
        nibRect.bottom = nibCenter.y + nibRadius;

        Paint nibPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nibPaint.setColor(Color.WHITE);

        canvas.drawOval(_knobRect, _knobPaint);
        canvas.drawOval(nibRect, nibPaint);
    }

    private void updateKnobColor(int color) {
        _knobPaint.setColor(color);
    }
}
