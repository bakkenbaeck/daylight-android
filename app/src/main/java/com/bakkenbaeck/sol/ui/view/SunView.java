package com.bakkenbaeck.sol.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class SunView extends View {

    private Paint paint;
    private Path path;
    private PathMeasure pm;

    private int width;
    private int height;
    private int bottom;
    private int left;
    private int right;

    private int iCurStep = 0;

    private final int margin = dpToPx(32);

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
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setColor(Color.RED);
        this.paint.setStrokeWidth(7);


    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        this.width = w;
        this.height = h;

        this.bottom = this.height / 2;
        this.left = margin;
        this.right = this.width - margin;

        this.path = new Path();
        this.path.moveTo(this.left, this.bottom);
        this.path.cubicTo(
                margin * -1,
                this.bottom - (margin * 5),
                this.width + (margin / 2),
                this.bottom - (margin * 5),
                this.right,
                this.bottom);

        this.pm = new PathMeasure(this.path, false);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        // This draws the curve -- and can be removed when you're happy with the curve shape
        canvas.drawPath(path, paint);

        // This just shows how to render a circle over the Path
        animateCircle(canvas);
    }

    private void animateCircle(Canvas canvas) {
        // This splits the curve up into 1000 bits and animates through them.
        float fSegmentLen = pm.getLength() / 1000;
        float afP[] = {0f, 0f};

        if (iCurStep <= 1000) {
            pm.getPosTan(fSegmentLen * iCurStep, afP, null);
            canvas.drawCircle(afP[0],afP[1],30,this.paint);
            iCurStep++;
        } else {
            iCurStep = 0;
        }

        invalidate();
    }

    private int dpToPx(final int dp){
        return Math.round(dp*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }
}
