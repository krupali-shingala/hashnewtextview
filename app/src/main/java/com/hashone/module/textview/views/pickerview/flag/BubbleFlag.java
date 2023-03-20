package com.hashone.module.textview.views.pickerview.flag;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;

import com.hashone.module.textview.R;
import com.hashone.module.textview.views.CircleWithWhiteRingDrawable;
import com.hashone.module.textview.views.pickerview.ColorEnvelope;

/**
 * BubbleFlag is a supported {@link FlagView} by the library.
 */
public class BubbleFlag extends FlagView {

    private final AppCompatImageView bubble;

    public BubbleFlag(Context context) {
        super(context, R.layout.flag_bubble_colorpickerview_skydoves);
        this.bubble = findViewById(R.id.bubble);
    }

    /**
     * invoked when selector is moved.
     *
     * @param colorEnvelope provide hsv color, hexCode, argb
     */
    @Override
    public void onRefresh(ColorEnvelope colorEnvelope) {
        ImageViewCompat.setImageTintList(bubble, ColorStateList.valueOf(colorEnvelope.getColor()));
        bubble.setBackground(new CircleWithWhiteRingDrawable(Color.TRANSPARENT, colorEnvelope.getColor(), 48f));
    }
}
