package com.bakkenbaeck.sol.ui.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.bakkenbaeck.sol.R;

public class SunView extends View {
    private static final int PATH_HEIGHT = 3;
    private int circleRadius;
    private boolean entireSunOverHorizon;

    private Paint paint;
    private Path path;
    private String startLabel;
    private String endLabel;
    private String floatingLabel;

    private int textMargin;
    private int horizont_margin;

    private int bottom;
    private int left;
    private int right;
    private int width;

    private int availableSunHeight;
    private int availableSunWidth;

    private double[] coords;

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
        this.paint.setColor(ContextCompat.getColor(getContext(), R.color.daylight_text2));
        this.paint.setStrokeWidth(4);
        this.paint.setTextSize(this.getContext().getResources().getDimensionPixelSize(R.dimen.text_size_tert));
        this.textMargin = this.getContext().getResources().getDimensionPixelSize(R.dimen.text_margin);
        this.horizont_margin = this.getContext().getResources().getDimensionPixelSize(R.dimen.horizont_bottom_margin);
        this.circleRadius = this.getContext().getResources().getDimensionPixelSize(R.dimen.sun_size) / 2;

        this.coords = new double[]{0.0, 0.0};
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

    // 5 == height of path

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        this.width = w;
        this.bottom = h;
        this.left = 0;
        this.right = w;

        this.availableSunHeight = h - horizont_margin - textMargin;
        this.availableSunWidth = w - circleRadius * 2;

        this.path = new Path();
        this.path.moveTo(left + circleRadius, bottom);
        this.path.cubicTo(
                left + circleRadius,
                bottom - (PATH_HEIGHT),
                right - circleRadius,
                bottom - (PATH_HEIGHT),
                right - circleRadius,
                bottom);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        drawLabels(canvas);
        drawHorizontal(canvas);
        clipCanvas(canvas);
        drawSun(canvas);
    }

    private void drawLabels(final Canvas canvas) {
        if (this.startLabel != null) {
            canvas.drawText(
                    this.startLabel,
                    this.left,
                    this.bottom - 1,
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
                    this.bottom - 1,
                    this.paint);
        }
    }

    private void drawHorizontal(final Canvas canvas) {
        canvas.drawLine(
                this.left,
                this.bottom - horizont_margin,
                this.right,
                this.bottom - horizont_margin,
                this.paint);
    }

    private void drawSun(Canvas canvas){
        drawCircle(canvas, coords);

        if (entireSunOverHorizon) {
            drawFloatingLabel(canvas, coords);
        }
    }

    //Clipping for future drawing
    private void clipCanvas(final Canvas canvas) {
        canvas.clipRect(
                0,
                0,
                this.width,
                this.bottom - horizont_margin,
                Region.Op.REPLACE);
    }

    public SunView setEntireSunOverHorizon(final boolean b) {
        this.entireSunOverHorizon = b;
        return this;
    }

    public void setProgress(final double xPercent, final double yPercent) {
        this.coords = new double[]{xPercent, yPercent};
        invalidate();
    }

    private void drawCircle(final Canvas canvas, final double[] coords) {
        canvas.drawCircle(
                (int)(coords[0] * availableSunWidth) + circleRadius,
                (int)(this.availableSunHeight - (this.availableSunHeight * coords[1])) + circleRadius + textMargin,
                circleRadius,
                this.paint);
    }

    private void drawFloatingLabel(final Canvas canvas, final double[] coords) {
        if (this.floatingLabel != null) {
            Rect bounds = new Rect();
            this.paint.getTextBounds(
                    this.floatingLabel,
                    0,
                    this.floatingLabel.length(),
                    bounds);
            canvas.drawText(
                    this.floatingLabel,
                    (int)((coords[0] * availableSunWidth) - (bounds.width() / 2)) + circleRadius,
                    (int)((this.availableSunHeight - (this.availableSunHeight * coords[1]))
                            - (bounds.height() * 2)) + circleRadius + textMargin,
                    this.paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        double height = getMeasuredWidth() * 0.6;
        int newHeight = (int) height;
        setMeasuredDimension(getMeasuredWidth(), newHeight);
    }
}
