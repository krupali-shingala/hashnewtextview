package com.hashone.module.textview.views.pickerview.listeners;

import androidx.annotation.ColorInt;

public interface ColorListener extends ColorPickerViewListener {
    void onColorSelected(@ColorInt int color, boolean fromUser);
}
