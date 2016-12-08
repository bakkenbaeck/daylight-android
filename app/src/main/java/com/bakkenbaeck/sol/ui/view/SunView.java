package com.bakkenbaeck.sol.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.bakkenbaeck.sol.R;

public class SunView extends View {

    private final int circleRadius = 20;

    private Paint paint;
    private Path path;
    private PathMeasure pm;
    private String startLabel;
    private String endLabel;
    private String floatingLabel;
    private int iCurStep = 0;
    private int viewMargin;
    private int textMargin;
    private int bottom;
    private int left;
    private int right;
    private int width;


    public SunView(final Context context) {
        super(context);
        init();
    }

    public SunView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SunView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(Color.RED);
        this.paint.setStrokeWidth(4);
        this.paint.setTextSize(35);
        this.viewMargin = dpToPx(this.getContext().getResources().getDimension(R.dimen.activity_horizontal_margin));
        this.textMargin = dpToPx(22);
    }

    public SunView setTypeface(final Typeface typeface) {
        this.paint.setTypeface(typeface);
        return this;
    }

    public SunView setColor(final int color) {
        this.paint.setColor(color);
        return this;
    }

    public SunView setStartLabel(final String startLabel) {
        this.startLabel = startLabel;
        return this;
    }

    public SunView setEndLabel(final String endLabel) {
        this.endLabel = endLabel;
        return this;
    }

    public SunView setFloatingLabel(final String floatingLabel) {
        this.floatingLabel = floatingLabel;
        return this;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        this.width = w;
        this.bottom = h / 2;
        this.left = viewMargin;
        this.right = w - viewMargin;

        this.path = new Path();
        this.path.moveTo(left + circleRadius, bottom);
        this.path.cubicTo(
                left + circleRadius,
                bottom - (viewMargin * 5),
                right - circleRadius,
                bottom - (viewMargin * 5),
                right - circleRadius,
                bottom);

        this.pm = new PathMeasure(this.path, false);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        drawLabels(canvas);
        clipCanvas(canvas);
        animateCircle(canvas);
        drawHorizontal(canvas);
    }

    private void drawLabels(final Canvas canvas) {
        if (this.startLabel != null) {
            canvas.drawText(
                    this.startLabel,
                    this.left,
                    this.bottom + this.textMargin,
                    this.paint);
        }

        if (this.endLabel != null) {
            Rect bounds = new Rect();
            this.paint.getTextBounds(
                    this.endLabel,
                    0,
                    this.endLabel.length(),
                    bounds);
            canvas.drawText(
                    this.endLabel,
                    this.right - bounds.width(),
                    this.bottom + this.textMargin,
                    this.paint);
        }
    }

    private void drawHorizontal(final Canvas canvas) {
        canvas.drawLine(
                this.left,
                this.bottom,
                this.right,
                this.bottom,
                this.paint);
    }

    private void clipCanvas(final Canvas canvas) {
        canvas.clipRect(
                0,
                0,
                this.width,
                this.bottom,
                Region.Op.REPLACE);
    }

    private void animateCircle(final Canvas canvas) {
        // This splits the curve up into 1000 bits and animates through them.
        float fSegmentLen = pm.getLength() / 1000;
        float coords[] = {0f, 0f};

        if (iCurStep <= 1000) {
            pm.getPosTan(fSegmentLen * iCurStep, coords, null);

            drawCircle(canvas, coords);
            drawFloatingLabel(canvas, coords);

            iCurStep++;
        } else {
            iCurStep = 0;
        }

        invalidate();
    }

    private void drawCircle(final Canvas canvas, final float[] coords) {
        canvas.drawCircle(
                coords[0],
                coords[1],
                circleRadius,
                this.paint);
    }

    private void drawFloatingLabel(final Canvas canvas, final float[] afP) {
        if (this.floatingLabel != null) {
            Rect bounds = new Rect();
            this.paint.getTextBounds(
                    this.floatingLabel,
                    0,
                    this.floatingLabel.length(),
                    bounds);
            canvas.drawText(
                    this.floatingLabel,
                    afP[0] - (bounds.width() / 2),
                    afP[1] - (bounds.height() * 2),
                    this.paint);
        }
    }

    private int dpToPx(final float dp){
        return Math.round(dp*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }
}
