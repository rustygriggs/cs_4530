package edu.utah.cs4530.rusty.paintpalette;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rusty on 9/15/2016.
 */
public class PaletteLayout extends ViewGroup implements SplotchView.OnSplotchSelectedListener {
    List<SplotchView> _splotchList;
    OnColorChangedListener _colorChangedListener = null;

    public PaletteLayout(Context context) {
        super(context);
        _splotchList = new ArrayList<>();
    }

    public void removeSplotch() {
        for (int i  = 0; i < _splotchList.size(); i++) {
            SplotchView splotchView = _splotchList.get(i);
            if (splotchView.isHighlighted()) {
                removeView(splotchView);
                _splotchList.remove(splotchView);
            }
        }
    }

    public interface OnColorChangedListener {
        void onColorChanged(int splotchColor);
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        _colorChangedListener = listener;
    }

    public void addSplotch(int splotchColor) {
        SplotchView splotchView = new SplotchView(getContext());
        splotchView.setSplotchColor(splotchColor);
        addView(splotchView);
        _splotchList.add(splotchView);
        splotchView.setOnSplotchSelectedListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        RectF paletteRect = new RectF();
        paletteRect.left = getPaddingLeft();
        paletteRect.right = getWidth() - getPaddingRight();
        paletteRect.top = getPaddingTop();
        paletteRect.bottom = getHeight() - getPaddingBottom();

        Paint palettePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        palettePaint.setColor(Color.argb(255, 139,69,19)); //saddle brown

        canvas.drawOval(paletteRect, palettePaint);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int  childIndex = 0; childIndex < getChildCount(); childIndex++) {
            float theta = (float)(2.0 * Math.PI) / (float)getChildCount() * (float)childIndex;

            float density = getResources().getDisplayMetrics().density;
            float childWidth = 0.3f * 160.0f * density; //3/10 of an inch (physical size)
            float childHeight = 0.25f * 160.0f * density; // 1/4 of an inch (physical size)

            PointF childCenter = new PointF();
            childCenter.x = (float) (getWidth() * 0.5f + (getWidth() * 0.5f - childWidth * 0.5)* (float)Math.cos(theta));
            childCenter.y = (float) (getHeight() * 0.5f + (getHeight() * 0.5f - childHeight * 0.5)* (float)Math.sin(theta));

            Rect childRect = new Rect();
            childRect.left = (int)(childCenter.x - childWidth * 0.5f);
            childRect.right = (int) (childCenter.x + childWidth * 0.5f);
            childRect.top = (int) (childCenter.y - childHeight * 0.5f);
            childRect.bottom = (int) (childCenter.y + childHeight* 0.5f);

            View childView = getChildAt(childIndex);
            childView.layout(childRect.left, childRect.top, childRect.right, childRect.bottom);
        }
    }

    @Override
    public void onSplotchSelected(int splotchColor, SplotchView splotchView) {
        for (SplotchView splotch : _splotchList) {
            if (splotch.isHighlighted()) {
                splotch.setSelected();
            }
        }
        splotchView.setSelected();
        _colorChangedListener.onColorChanged(splotchColor);
    }
}