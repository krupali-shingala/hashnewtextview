package com.hashone.textview.textviewnew

import android.content.Context
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.atan2

class CustomRotationGestureDetector(
    context: Context, private val listener: OnRotationGestureListener
) {
    private var focusX = 0f
    private var focusY = 0f
    private var initialAngle = 0f
    private var currAngle = 0f
    private var prevAngle = 0f
    private var isInProgress = false
    private var isGestureAccepted = false

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> cancelRotation()
            MotionEvent.ACTION_POINTER_DOWN -> if (event.pointerCount == 2) {
                // Second finger is placed
                currAngle = computeRotation(event)
                prevAngle = currAngle
                initialAngle = prevAngle
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2 && (!isInProgress || isGestureAccepted)) {
                    // Moving 2 or more fingers on the screen
                    currAngle = computeRotation(event)
                    focusX = 0.5f * (event.getX(1) + event.getX(0))
                    focusY = 0.5f * (event.getY(1) + event.getY(0))
                    val isAlreadyStarted = isInProgress
                    tryStartRotation()
                    val isAccepted = !isAlreadyStarted || processRotation()
                    if (isAccepted) {
                        prevAngle = currAngle
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> if (event.pointerCount == 2) {
                // Only one finger is left
                cancelRotation()
            }

            else -> {}
        }
        return true
    }

    private fun tryStartRotation() {
        if (isInProgress || abs(initialAngle - currAngle) < ROTATION_SLOP) {
            return
        }
        isInProgress = true
        isGestureAccepted = listener.onRotationBegin(this)
    }

    private fun cancelRotation() {
        if (!isInProgress) {
            return
        }
        isInProgress = false
        if (isGestureAccepted) {
            listener.onRotationEnd(this)
            isGestureAccepted = false
        }
    }

    private fun processRotation(): Boolean {
        return isInProgress && isGestureAccepted && listener.onRotate(this)
    }

    private fun computeRotation(event: MotionEvent): Float {
        return Math.toDegrees(
            atan2(
                (event.getY(0) - event.getY(1)).toDouble(),
                (event.getX(0) - event.getX(1)).toDouble()
            )
        ).toFloat()
    }

    fun isInProgress(): Boolean {
        return isInProgress
    }

    fun getFocusX(): Float {
        return focusX
    }

    fun getFocusY(): Float {
        return focusY
    }

    fun getRotationDelta(): Float {
        return currAngle - prevAngle
    }

    interface OnRotationGestureListener {
        fun onRotate(detector: CustomRotationGestureDetector?): Boolean

        fun onRotationBegin(detector: CustomRotationGestureDetector?): Boolean

        fun onRotationEnd(detector: CustomRotationGestureDetector?)
    }

    companion object {
        private const val ROTATION_SLOP = 5f
    }
}