package com.hashone.module.textview.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hashone.commonutils.utils.Utils;

public class CircleWithWhiteRingDrawable extends Drawable {

    private final Paint circlePaint;
    private final int fillColor;
    private final int strokeColor;
    private final float radius;

    public CircleWithWhiteRingDrawable(int fillColor, int strokeColor, float radius) {
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.radius = radius;
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int x = getBounds().centerX();
        int y = getBounds().centerY();
        //draw fill color circle
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(fillColor);
        canvas.drawCircle(x, y, radius, circlePaint);
        // draw stroke circle
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(strokeColor);
        circlePaint.setStrokeWidth(Utils.INSTANCE.dpToPx(3));
        canvas.drawCircle(x, y, radius - Utils.INSTANCE.dpToPx(2), circlePaint);
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStrokeWidth(Utils.INSTANCE.dpToPx(1));
        canvas.drawCircle(x, y, radius, circlePaint);
        circlePaint.setColor(Color.BLACK);
        circlePaint.setStrokeWidth(Utils.INSTANCE.dpToPx(2));
        canvas.drawCircle(x, y, Utils.INSTANCE.dpToPx(4), circlePaint);

    }

    @Override
    public void setAlpha(int alpha) {
        circlePaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        circlePaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}