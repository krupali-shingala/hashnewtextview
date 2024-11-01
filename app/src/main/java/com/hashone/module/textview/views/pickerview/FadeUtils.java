/*
 * Designed and developed by 2017 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hashone.module.textview.views.pickerview;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.RestrictTo;

import com.hashone.module.textview.R;


@RestrictTo(LIBRARY_GROUP)
public class FadeUtils {

    public static void fadeIn(View view) {
        Animation fadeIn =
                AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in_colorpickerview_skydoves);
        fadeIn.setFillAfter(true);
        view.startAnimation(fadeIn);
    }

    public static void fadeOut(View view) {
        Animation fadeOut =
                AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out_colorpickerview_skydoves);
        fadeOut.setFillAfter(true);
        view.startAnimation(fadeOut);
    }
}
