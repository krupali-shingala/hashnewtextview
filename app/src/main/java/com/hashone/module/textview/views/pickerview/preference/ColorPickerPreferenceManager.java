package com.hashone.module.textview.views.pickerview.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import com.hashone.module.textview.views.pickerview.ColorPickerView;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ColorPickerPreferenceManager {

    protected static final String COLOR = "_COLOR";
    protected static final String SelectorX = "_SELECTOR_X";
    protected static final String SelectorY = "_SELECTOR_Y";
    protected static final String AlphaSlider = "_SLIDER_ALPHA";
    protected static final String BrightnessSlider = "_SLIDER_BRIGHTNESS";
    private static ColorPickerPreferenceManager colorPickerPreferenceManager;
    private final SharedPreferences sharedPreferences;

    private ColorPickerPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    /**
     * gets an instance of the {@link ColorPickerPreferenceManager}.
     *
     * @param context context.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public static ColorPickerPreferenceManager getInstance(Context context) {
        if (colorPickerPreferenceManager == null) {
            colorPickerPreferenceManager = new ColorPickerPreferenceManager(context);
        }
        return colorPickerPreferenceManager;
    }

    /**
     * saves a color on preference.
     *
     * @param name  preference name.
     * @param color preference color.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager setColor(String name, int color) {
        sharedPreferences.edit().putInt(getColorName(name), color).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * gets the saved color from preference.
     *
     * @param name         preference name.
     * @param defaultColor default preference color.
     * @return the saved color.
     */
    public int getColor(String name, int defaultColor) {
        return sharedPreferences.getInt(getColorName(name), defaultColor);
    }

    /**
     * clears the saved color from preference.
     *
     * @param name preference name.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedColor(String name) {
        sharedPreferences.edit().remove(getColorName(name)).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * saves a selector position on preference.
     *
     * @param name     preference name.
     * @param position position of the selector.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager setSelectorPosition(String name, Point position) {
        sharedPreferences.edit().putInt(getSelectorXName(name), position.x).apply();
        sharedPreferences.edit().putInt(getSelectorYName(name), position.y).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * gets the saved selector position on preference.
     *
     * @param name         preference name.
     * @param defaultPoint default position of the selector.
     * @return the saved selector position.
     */
    public Point getSelectorPosition(String name, Point defaultPoint) {
        return new Point(sharedPreferences.getInt(getSelectorXName(name), defaultPoint.x), sharedPreferences.getInt(getSelectorYName(name), defaultPoint.y));
    }

    /**
     * clears the saved selector position from preference.
     *
     * @param name preference name.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedSelectorPosition(String name) {
        sharedPreferences.edit().remove(getSelectorXName(name)).apply();
        sharedPreferences.edit().remove(getSelectorYName(name)).apply();
        return colorPickerPreferenceManager;
    }

    public ColorPickerPreferenceManager setAlphaSliderPosition(String name, int position) {
        sharedPreferences.edit().putInt(getAlphaSliderName(name), position).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * gets the alpha slider position.
     *
     * @param name            preference name.
     * @param defaultPosition default position of alpha slider position.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public int getAlphaSliderPosition(String name, int defaultPosition) {
        return sharedPreferences.getInt(getAlphaSliderName(name), defaultPosition);
    }

    /**
     * clears the saved alpha slider position from preference.
     *
     * @param name preference name.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedAlphaSliderPosition(String name) {
        sharedPreferences.edit().remove(getAlphaSliderName(name)).apply();
        return colorPickerPreferenceManager;
    }

    public ColorPickerPreferenceManager setBrightnessSliderPosition(String name, int position) {
        sharedPreferences.edit().putInt(getBrightnessSliderName(name), position).apply();
        return colorPickerPreferenceManager;
    }

    /**
     * gets the brightness slider position.
     *
     * @param name            preference name.
     * @param defaultPosition default position of brightness slider position.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public int getBrightnessSliderPosition(String name, int defaultPosition) {
        return sharedPreferences.getInt(getBrightnessSliderName(name), defaultPosition);
    }

    /**
     * clears the saved brightness slider position from preference.
     *
     * @param name preference name.
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedBrightnessSlider(String name) {
        sharedPreferences.edit().remove(getBrightnessSliderName(name)).apply();
        return colorPickerPreferenceManager;
    }

    public void saveColorPickerData(ColorPickerView colorPickerView) {
        if (colorPickerView != null && colorPickerView.getPreferenceName() != null) {
            String name = colorPickerView.getPreferenceName();
            setColor(name, colorPickerView.getColor());
            setSelectorPosition(name, colorPickerView.getSelectedPoint());

            if (colorPickerView.getAlphaSlideBar() != null) {
                setAlphaSliderPosition(name, colorPickerView.getAlphaSlideBar().getSelectedX());
            }
            if (colorPickerView.getBrightnessSlider() != null) {
                setBrightnessSliderPosition(name, colorPickerView.getBrightnessSlider().getSelectedX());
            }
        }
    }

    /**
     * restores all data from the preference.
     *
     * @param colorPickerView {@link ColorPickerView}.
     */
    public void restoreColorPickerData(ColorPickerView colorPickerView) {
        if (colorPickerView != null && colorPickerView.getPreferenceName() != null) {
            String name = colorPickerView.getPreferenceName();
            colorPickerView.setPureColor(getColor(name, -1));
            Point defaultPoint = new Point(colorPickerView.getMeasuredWidth() / 2, colorPickerView.getMeasuredHeight() / 2);
            colorPickerView.moveSelectorPoint(getSelectorPosition(name, defaultPoint).x, getSelectorPosition(name, defaultPoint).y, getColor(name, -1));
        }
    }

    /**
     * clears all saved preference data.
     *
     * @return {@link ColorPickerPreferenceManager}.
     */
    public ColorPickerPreferenceManager clearSavedAllData() {
        sharedPreferences.edit().clear().apply();
        return colorPickerPreferenceManager;
    }

    protected String getColorName(String name) {
        return name + COLOR;
    }

    protected String getSelectorXName(String name) {
        return name + SelectorX;
    }

    protected String getSelectorYName(String name) {
        return name + SelectorY;
    }

    protected String getAlphaSliderName(String name) {
        return name + AlphaSlider;
    }

    protected String getBrightnessSliderName(String name) {
        return name + BrightnessSlider;
    }
}
