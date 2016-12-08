package com.bakkenbaeck.sol.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import com.bakkenbaeck.sol.R;

public class SunView extends View {

    private Paint paint;
    private Path path;
    private PathMeasure pm;
    private int iCurStep = 0;
    private int margin;
    private int bottom;
    private int left;
    private int right;

    private final int circleRadius = 30;

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
        this.margin = dpToPx(this.getContext().getResources().getDimension(R.dimen.activity_horizontal_margin));
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        this.bottom = h / 2;
        this.left = margin;
        this.right = w - margin;

        this.path = new Path();
        this.path.moveTo(left + circleRadius, bottom);
        this.path.cubicTo(
                left + circleRadius,
                bottom - (margin * 5),
                right - circleRadius,
                bottom - (margin * 5),
                right - circleRadius,
                bottom);

        this.pm = new PathMeasure(this.path, false);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        clipCanvas(canvas);
        animateCircle(canvas);
        drawHorizontal(canvas);
    }

    private void drawHorizontal(Canvas canvas) {
        canvas.drawLine(
                this.left,
                this.bottom,
                this.right,
                this.bottom,
                this.paint);
    }

    private void clipCanvas(Canvas canvas) {
        canvas.clipRect(
                this.left,
                0,
                this.right,
                this.bottom,
                Region.Op.REPLACE);
    }

    private void animateCircle(Canvas canvas) {
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
