package com.hashone.module.textview.views.pickerview;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import androidx.annotation.Dimension;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Dimension(unit = Dimension.DP)
@interface Dp {
}
