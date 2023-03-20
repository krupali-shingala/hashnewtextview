package com.hashone.module.textview.views.pickerview.listeners;

import com.hashone.module.textview.views.pickerview.ColorEnvelope;

public interface ColorEnvelopeListener extends ColorPickerViewListener {
    void onColorSelected(ColorEnvelope envelope, boolean fromUser);
}
