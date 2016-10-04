package edu.utah.cs4530.rusty.paintpalette;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Rusty on 9/12/2016.
 */
public class SplotchView extends View {

    private int _splotchColor = 0;
    private boolean _highlighted = false;
    OnSplotchSelectedListener _splotchSelectedListener = null;

    public SplotchView(Context context) {
        super(context);
    }

    public interface OnSplotchSelectedListener {
        void onSplotchSelected(int splotchColor, SplotchView splotchView);
    }

    public void setOnSplotchSelectedListener(OnSplotchSelectedListener listener) {
        _splotchSelectedListener = listener;
    }

    public void setSelected() {
        if (_highlighted) {
            setIsHighlighted(false);
        }
        else setIsHighlighted(true);
    }

    public boolean isHighlighted() {
        return _highlighted;
    }

    public void setIsHighlighted(boolean highlighted) {
        _highlighted = highlighted;
        invalidate();
    }

    public int getSplotchColor() {
        return _splotchColor;
    }

    public void setSplotchColor(int splotchColor) {
        _splotchColor = splotchColor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF splotchRect = new RectF();
        splotchRect.left = getPaddingLeft();
        splotchRect.right = getWidth() - getPaddingRight();
        splotchRect.top = getPaddingTop();
        splotchRect.bottom = getHeight() - getPaddingBottom();

        Paint splotchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        splotchPaint.setColor(_splotchColor);

        canvas.drawOval(splotchRect, splotchPaint);

        if (_highlighted) {
            Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (_splotchColor == Color.YELLOW) {
                highlightPaint.setColor(Color.BLUE);
            }
            else {
                highlightPaint.setColor(Color.YELLOW);
            }
            highlightPaint.setStyle(Paint.Style.STROKE);
            highlightPaint.setStrokeWidth(splotchRect.height() * 0.1f);
            canvas.drawOval(splotchRect, highlightPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        _splotchSelectedListener.onSplotchSelected(_splotchColor, this);
        return true;
    }
}
