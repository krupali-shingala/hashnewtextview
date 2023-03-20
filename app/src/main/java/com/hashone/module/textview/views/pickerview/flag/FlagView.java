package com.hashone.module.textview.views.pickerview.flag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.hashone.module.textview.views.pickerview.ColorEnvelope;
import com.hashone.module.textview.views.pickerview.FadeUtils;

/**
 * FlaView implements showing a flag above a selector.
 */
@SuppressWarnings("unused")
public abstract class FlagView extends RelativeLayout {

    private FlagMode flagMode = FlagMode.ALWAYS;
    private boolean flipAble = true;

    public FlagView(Context context, int layout) {
        super(context);
        initializeLayout(layout);
    }

    public abstract void onRefresh(ColorEnvelope colorEnvelope);

    public void receiveOnTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (getFlagMode() == FlagMode.LAST) gone();
                else if (getFlagMode() == FlagMode.FADE) FadeUtils.fadeIn(this);
                break;
            case MotionEvent.ACTION_MOVE:
                if (getFlagMode() == FlagMode.LAST) gone();
                break;
            case MotionEvent.ACTION_UP:
                if (getFlagMode() == FlagMode.LAST) visible();
                else if (getFlagMode() == FlagMode.FADE) FadeUtils.fadeOut(this);
            default:
                visible();
        }
    }

    private void initializeLayout(int layout) {
        View inflated = LayoutInflater.from(getContext()).inflate(layout, this);
        inflated.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        inflated.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        inflated.layout(0, 0, inflated.getMeasuredWidth(), inflated.getMeasuredHeight());
    }

    /**
     * makes {@link FlagView} visible.
     */
    public void visible() {
        setVisibility(View.VISIBLE);
    }

    /**
     * makes {@link FlagView} invisible.
     */
    public void gone() {
        setVisibility(View.GONE);
    }

    /**
     * gets the flag's mode of visibility action.
     *
     * @return {@link FlagMode}
     */
    public FlagMode getFlagMode() {
        return flagMode;
    }

    /**
     * sets the flag's mode of visibility action.
     *
     * @param flagMode {@link FlagMode}
     */
    public void setFlagMode(FlagMode flagMode) {
        this.flagMode = flagMode;
    }

    /**
     * gets is flag flip-able.
     *
     * @return true or false.
     */
    public boolean isFlipAble() {
        return flipAble;
    }

    /**
     * sets the flag being flipped down-sided automatically.
     *
     * @param flipAble true or false.
     */
    public void setFlipAble(boolean flipAble) {
        this.flipAble = flipAble;
    }
}
