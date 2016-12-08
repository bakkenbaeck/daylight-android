package com.bakkenbaeck.sol.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.Region;
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
    private int iCurStep = 0;
    private int viewMargin;
    private int textMargin;
    private int bottom;
    private int left;
    private int right;


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

    private void init(){
        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(Color.RED);
        this.paint.setStrokeWidth(4);
        this.paint.setTextSize(40);
        this.viewMargin = dpToPx(this.getContext().getResources().getDimension(R.dimen.activity_horizontal_margin));
        this.textMargin = dpToPx(22);
    }

    public SunView setStartLabel(final String startLabel) {
        this.startLabel = startLabel;
        return this;
    }

    public SunView setEndLabel(final String endLabel) {
        this.endLabel = endLabel;
        return this;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
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
            this.paint.getTextBounds("23:59", 0, "23:59".length(), bounds);
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
                this.left,
                0,
                this.right,
                this.bottom,
                Region.Op.REPLACE);
    }

    private void animateCircle(final Canvas canvas) {
        // This splits the curve up into 1000 bits and animates through them.
        float fSegmentLen = pm.getLength() / 1000;
        float afP[] = {0f, 0f};

        if (iCurStep <= 1000) {
            pm.getPosTan(fSegmentLen * iCurStep, afP, null);
            canvas.drawCircle(
                    afP[0],
                    afP[1],
                    circleRadius,
                    this.paint);
            iCurStep++;
        } else {
            iCurStep = 0;
        }

        invalidate();
    }

    private int dpToPx(final float dp){
        return Math.round(dp*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }
}
