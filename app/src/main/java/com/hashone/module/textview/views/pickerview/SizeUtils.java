package com.hashone.module.textview.views.pickerview;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;

import androidx.annotation.RestrictTo;

/**
 * SizeUtils a util class for resizing scales.
 */
@RestrictTo(LIBRARY_GROUP)
class SizeUtils {
    /**
     * changes dp size to px size.
     */
    protected static int dp2Px(Context context, int dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
