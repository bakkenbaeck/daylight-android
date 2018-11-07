package com.bakkenbaeck.sol.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import com.bakkenbaeck.sol.R;

public class SunView extends View {
    private Paint paint;

    private String startLabel = "";
    private String endLabel = "";
    private String floatingLabel = "";

    private int bottom;
    private int left;
    private int right;
    private int width;
    private int textMargin;
    private int horizon_bottom_margin;
    private int circleRadius;
    private int availableSunHeight;
    private int availableSunWidth;
    private int strokeWidth;
    private int rightMarginLabel;
    private int leftMarginLabel;

    private double[] coords;
    private boolean sunIsVisible;

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
        this.coords = new double[]{0.0, 0.0};

        initSizes();
    }

    public void initSizes() {
        this.paint.setColor(ContextCompat.getColor(getContext(), R.color.sun_default_color));
        this.paint.setTextSize(this.getContext().getResources().getDimensionPixelSize(R.dimen.sun_text_size));
        this.textMargin = this.getContext().getResources().getDimensionPixelSize(R.dimen.sun_text_margin);
        this.horizon_bottom_margin = this.getContext().getResources().getDimensionPixelSize(R.dimen.sun_horizon_bottom_margin);
        this.circleRadius = this.getContext().getResources().getDimensionPixelSize(R.dimen.sun_radius);
        this.strokeWidth = this.getContext().getResources().getDimensionPixelSize(R.dimen.sun_stroke_width);
        this.rightMarginLabel = getContext().getResources().getDimensionPixelSize(R.dimen.sun_right_margin_label);
        this.leftMarginLabel = getContext().getResources().getDimensionPixelSize(R.dimen.sun_left_margin_label);
        this.paint.setStrokeWidth(strokeWidth);
    }

    public void setStrokeWidth(final int strokeWidth) {
        this.paint.setStrokeWidth(strokeWidth);
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

    public SunView setTextSize(final int textSize) {
        this.paint.setTextSize(textSize);
        return this;
    }

    public SunView setFloatingTextBottomMargin(final int textMargin) {
        this.textMargin = textMargin;
        return this;
    }

    public SunView setSunRadius(final int radius) {
        this.circleRadius = radius;
        return this;
    }

    public SunView setHorizonMargin(final int margin) {
        this.horizon_bottom_margin = margin;
        return this;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        this.width = w;
        this.bottom = h;
        this.left = 0;
        this.right = w;

        this.availableSunHeight = h - horizon_bottom_margin - textMargin;
        this.availableSunWidth = w - (circleRadius * 4);

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
                    this.left + leftMarginLabel,
                    this.bottom - 1,
                    this.paint);
        }

        if (this.endLabel != null) {
            final Rect bounds = new Rect();
            this.paint.getTextBounds(
                    this.endLabel,
                    0,
                    this.endLabel.length(),
                    bounds);
            canvas.drawText(
                    this.endLabel,
                    this.right - bounds.width() - rightMarginLabel,
                    this.bottom - 1,
                    this.paint);
        }
    }

    private void drawHorizontal(final Canvas canvas) {
        canvas.drawLine(
                this.left,
                this.bottom - horizon_bottom_margin,
                this.right,
                this.bottom - horizon_bottom_margin,
                this.paint);
    }

    private void drawSun(Canvas canvas) {
        drawCircle(canvas, coords);
        if (sunIsVisible) {
            drawFloatingLabel(canvas, coords);
        }
    }

    private void clipCanvas(final Canvas canvas) {
        canvas.clipRect(
                0,
                0,
                this.width,
                this.bottom - horizon_bottom_margin);
    }

    public SunView setPercentProgress(final double progress) {
        final double position = (Math.PI + (progress * Math.PI));
        double x = (50 + Math.cos(position) * 50) / 100;
        double y = (Math.abs(Math.sin(position) * 100)) / 100;

        if (progress > 1 || progress < 0) {
            x = 0;
            y = 0;
            sunIsVisible = false;
        } else {
            sunIsVisible = true;
        }

        this.coords = new double[]{x, y};
        invalidate();

        return this;
    }

    private void drawCircle(final Canvas canvas, final double[] coords) {
        canvas.drawCircle(
                (int)(coords[0] * availableSunWidth) + (circleRadius * 2),
                (int)(this.availableSunHeight - (this.availableSunHeight * coords[1]))
                        + circleRadius + textMargin,
                circleRadius,
                this.paint);
    }

    private void drawFloatingLabel(final Canvas canvas, final double[] coords) {
        if (this.floatingLabel != null) {
            final Rect bounds = new Rect();
            this.paint.getTextBounds(
                    this.floatingLabel,
                    0,
                    this.floatingLabel.length(),
                    bounds);
            canvas.drawText(
                    this.floatingLabel,
                    (int)((coords[0] * availableSunWidth) - (bounds.width() / 2)) + (circleRadius * 2),
                    (int)((this.availableSunHeight - (this.availableSunHeight * coords[1]))
                            - (bounds.height() * 2)) + circleRadius + textMargin,
                    this.paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final double height = getMeasuredWidth() * 0.6;
        final int newHeight = (int) height;
        setMeasuredDimension(getMeasuredWidth(), newHeight);
    }
}