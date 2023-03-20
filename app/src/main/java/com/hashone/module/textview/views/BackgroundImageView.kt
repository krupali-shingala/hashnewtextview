package com.hashone.module.textview.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import com.hashone.commonutils.enums.ElementType
import com.hashone.commonutils.utils.ColorUtils
import com.hashone.commonutils.utils.Constants
import com.hashone.commonutils.utils.Constants.isProjectHasChanges

class BackgroundImageView : AppCompatImageView {

    var elementType: ElementType = ElementType.BACKGROUND

    var isLock: Int = 0

    var mAlpha: Int = 100

    var mColorName: String = ""
    var bgColor = Color.BLACK
        set(bgColor) {
            isGradientEnabled = false
            field = bgColor

            if (isTintEnabled) {
                imageTintList = ColorStateList.valueOf(bgColor)
            } else {
                setImageDrawable(null)
                setImageDrawable(ColorDrawable(bgColor))
            }
            invalidate()
        }
    private var isRippleEnabled = false
    var isTintEnabled = false

    var isGradientEnabled = false
    var gradientResourceName: String = ""

    var isEditEnabled: Boolean = false
    private var mDetector: GestureDetector? = null
    var mCallbackListener: CallbackListener? = null
    var mEventCallbackListener: EventCallbackListener? = null

    constructor(context: Context) : super(context) {
        initViews()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initViews()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initViews()
    }

    private fun initViews() {
        //setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        scaleType = ScaleType.CENTER_CROP

        mDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                return super.onDoubleTap(e)
            }

            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (mCallbackListener != null) {
                    mCallbackListener!!.onViewClick(e)
                    return true
                }
                return false
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }
        })
    }

    fun setRippleEnabled(isRippleEnabled: Boolean) {
        this.isRippleEnabled = isRippleEnabled

        if (this.isRippleEnabled) {
            var rippledImage: RippleDrawable? = null
            rippledImage = RippleDrawable(
                ColorStateList.valueOf(Color.parseColor("#B3E2F4E6")),
                drawable,
                null
            )
            setImageDrawable(null)
            setImageDrawable(rippledImage)
        }
        invalidate()
    }

    fun setViewAlpha(mAlpha: Int = 100, byUser: Boolean = false) {
        this.mAlpha = mAlpha
        alpha = (this.mAlpha * 255F) / 100F
        invalidate()
        if (byUser)
            isProjectHasChanges = true
    }

    var maskBitmap: Bitmap? = null
    fun setMaskImage(_maskingBitmap: Bitmap): Bitmap? {
        isGradientEnabled = true
        var resultMaskBitmap: Bitmap? = null
        val getMaskBitmap: Bitmap
        try {
            val original = drawable.toBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)

            val intWidth = drawable.intrinsicWidth
            val intHeight = drawable.intrinsicHeight
            resultMaskBitmap = Bitmap.createBitmap(intWidth, intHeight, Bitmap.Config.ARGB_8888)
            getMaskBitmap = Bitmap.createScaledBitmap(_maskingBitmap, intWidth, intHeight, true)
            val mCanvas = Canvas(resultMaskBitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.isAntiAlias = true
            paint.isDither = true
            paint.isFilterBitmap = true
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            mCanvas.drawBitmap(getMaskBitmap, 0F, 0F, null)
            mCanvas.drawBitmap(original, 0F, 0F, paint)
            paint.xfermode = null
            paint.style = Paint.Style.STROKE
            setImageBitmap(null)
            setImageDrawable(BitmapDrawable(resources, resultMaskBitmap))
        } catch (o: OutOfMemoryError) {
            o.printStackTrace()
        }
        return resultMaskBitmap
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isLock == 1)
            return false
        if (event == null) return false
        if (mDetector != null) {
            mDetector!!.onTouchEvent(event)
        }
        if (Constants.currentSelectedViewType == ElementType.NONE
//            || Constants.currentSelectedViewType == ElementType.BACKGROUND
        ) {
            if (mDetector != null) {
                mDetector!!.onTouchEvent(event)
                return true
            }
        }
        if (mEventCallbackListener != null) {
            mEventCallbackListener!!.onEventDetected(event)
        }
        return true
    }

    interface CallbackListener {
        fun onViewClick(motionEvent: MotionEvent)
    }

    interface EventCallbackListener {
        fun onEventDetected(motionEvent: MotionEvent)
    }

    fun applyColor(colorName: String) {
        mColorName = colorName
        isGradientEnabled = false
        setImageDrawable(BitmapDrawable(resources, prepareBitmap()))
    }

    private fun prepareBitmap(): Bitmap? {
        val colorBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val surfaceCanvas = Canvas(colorBitmap)
        val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        surfacePaint.isAntiAlias = true
        surfacePaint.isDither = true
        surfacePaint.isFilterBitmap = true
        surfacePaint.color = ColorUtils.colorCode(mColorName)
        surfaceCanvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), surfacePaint)
        return colorBitmap
    }
}