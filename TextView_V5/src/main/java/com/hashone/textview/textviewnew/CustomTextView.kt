package com.hashone.textview.textviewnew

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.view.*
import android.view.View.OnTouchListener
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.hashone.module.textview.R
import com.hashone.commonutils.enums.ElementType
import com.hashone.commonutils.utils.Constants.currentSelectedViewId
import com.hashone.commonutils.utils.Constants.currentSelectedViewType
import com.hashone.commonutils.utils.Constants.elementPositionList
import com.hashone.commonutils.utils.Constants.isProjectHasChanges
import com.hashone.commonutils.utils.Utils
import java.util.*
import kotlin.math.*

open class CustomTextView(
    cntx: Activity, parent: View,
    viewVertical: View?, viewHorizontal: View?
) : AppCompatTextView(cntx),
    CustomRotationGestureDetector.OnRotationGestureListener,
    android.view.ScaleGestureDetector.OnScaleGestureListener {

    private enum class Mode {
        NONE, DRAG, LEFT_WRAP, RIGHT_WRAP, ZOOM
    }

    private var mode = Mode.NONE

    private val MIN_ZOOM = 0.1f
    private val MAX_ZOOM = 4.0f
    private var minWidth = 0f
    private var maxWidth = 0f

    var flipX: Int = 0
    var flipY: Int = 0

    var gestureDetector: GestureDetector? = null
    var mSelectedTag = ""
    var isParentTouch = false
    var isEditEnabled = false
    var isScaleFirstTime = false
    var textCaseIndex = TextCaseType.DEFAULT
    var baseh = 0
    var basew = 0
    var basex: Int = 0
    var basey: Int = 0

    var mLineSpacing: Float = 0F

    var colorName = ""
    var typeFace: Typeface? = null
    var maskImage: String = ""

    var SELF_WIDTH_SIZE_DP = 200
    var SELF_HEIGHT_SIZE_DP = 200
    var scale = 1.0f
    var elementType = ElementType.TEXT
    var opacity = 1.0f
    var iStickerOperation: IStickerOperation? = null
    var mTag = ""
    var minimumScale = 0.1f
    var maximumScale = 8.0f
    var minWrap = 110
    var minWrapI = 110
    var maxWrap = 100
    var startAng = 0.0
    var startScale = 0.0
    var isClick = false
    var isXBaseLineDetected = false
    var isYBaseLineDetected = false
    private var enableBorder = false

    private var move_orgX = -1f
    private var move_orgY = -1f
    private var centerX = 0.0
    private var lastScaleFactor = 0f
    private var baseWidth = 0
    var isLock = 0
    var storedString = ""
    var updatedByUser = false
    private val extraMaxWidth1 = 0
    var elementAlpha = 100
    var ACTION_UP = true
    private var layGroupX = 0f
    private var layGroupY = 0f
    private var previewsWidth = 0f
    private var lineCountForLargeWidth = ""

    var fontName: String = ""
    var fontId: Int = -1
    var fontCategoryId: Int = -1
    var fontServerUrl: String = ""

    //TODO: Background
    var resourceCategoryId: Int = -1
    var resourceId: Int = -1
    var resourceName: String = ""
    var resourceUrl: String = ""
    var colorStringName: String = ""
    var isGradientEnabled = false
    var img: String = ""
    var isReset: Boolean = false

    private var imageLeftWrap: ImageView? = null
    private var imageRightWrap: ImageView? = null
    var margl = 0f
    var margr = 0f
    var margt = 0f
    var pivx: Int = 0
    var pivy: Int = 0
    lateinit var size: IntArray
    var mHViewBaseLine: View? = null
    var mVViewBaseLine: View? = null

    var mEventCallbackListener: EventCallbackListener? = null
    private var rotationGestureDetector1: CustomRotationGestureDetector? = null
    var scaleListener: ScaleListener? = null
    var scaleRightListener: ScaleRightListener? = null
    private var mScaleGestureDetector: CustomScaleGestureDetector? =
        null

    var textGravityIndex: Int = 1
        set(value) {
            field = value
            when (value) {
                0 -> gravity = Gravity.START or Gravity.CENTER_VERTICAL
                1 -> gravity = Gravity.CENTER
                2 -> gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
        }

    fun adjustAngle(degrees: Float): Float {
        var degrees = degrees
        if (degrees > 180.0f) {
            degrees -= 360.0f
        } else if (degrees < -180.0f) {
            degrees += 360.0f
        }
        rotation = degrees
        return degrees
    }

    private fun convertDpToPixel(dp: Float, context: Context): Int {
        val resources = context.resources
        val metrics = resources.displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px.toInt()
    }

    fun setup() {
        includeFontPadding = false
        rotationGestureDetector1 = CustomRotationGestureDetector(context, this)
        setupView(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = OnTouchListener { view, event ->
        if (currentSelectedViewId != -1 && currentSelectedViewId != id && mEventCallbackListener != null) {
            mEventCallbackListener!!.onEventDetected(event)
            if (isMotionEventInsideView(event)) {
                gestureDetector!!.onTouchEvent(event)
                if (!enableBorder) return@OnTouchListener true
            }
            if (!enableBorder) return@OnTouchListener false
        }
        maxWrap = maxWidth().toInt() + extraMaxWidth1
        val mParentView = getParent() as View
        if (isParentTouch) {
            return@OnTouchListener false
        }
        if (currentSelectedViewType == ElementType.NONE || currentSelectedViewType == ElementType.TEXT) {
            isParentTouch = false
            gestureDetector!!.onTouchEvent(event)
            val mLayoutParams = layoutParams as FrameLayout.LayoutParams
            if (enableBorder) {
                if (event.pointerCount == 1) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            move_orgX = event.rawX
                            move_orgY = event.rawY
                            basex = event.rawX.toInt()
                            basey = event.rawY.toInt()
                            margl = mLayoutParams.leftMargin.toFloat()
                            margr = mLayoutParams.rightMargin.toFloat()
                            margt = mLayoutParams.topMargin.toFloat()
                            basew = width
                            baseh = height
                            baseWidth = width
                            centerX =
                                (x + (getParent() as View).x + width.toFloat() / 2).toDouble()
                            isClick = true
                            val rightRect = Rect()
                            if (imageRightWrap != null)
                                imageRightWrap!!.getGlobalVisibleRect(rightRect)
                            val leftRect = Rect()
                            if (imageLeftWrap != null)
                                imageLeftWrap!!.getGlobalVisibleRect(leftRect)

                            if (rightRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                                if (imageRightWrap != null)
                                    scaleRightListener!!.onTouch(imageRightWrap!!, event)
                                mode = Mode.RIGHT_WRAP
                            } else if (leftRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                                if (imageLeftWrap != null)
                                    scaleListener!!.onTouch(imageLeftWrap!!, event)
                                mode = Mode.LEFT_WRAP
                            } else mode = Mode.DRAG
                        }

                        MotionEvent.ACTION_MOVE -> {
                            isClick = true
                            val currx = event.rawX.toInt()
                            val curry = event.rawY.toInt()
                            var agl = Math.toDegrees(
                                atan2(
                                    (curry - basey).toDouble(),
                                    (currx - basex).toDouble()
                                )
                            ).toFloat()
                            if (agl < 0.0f) {
                                agl += 360.0f
                            }
                            var changesWN =
                                if (rotation in 90.0..270.0) currx - basex else basex - currx
                            val changesHN =
                                if (rotation in 90.0..270.0) curry - basey else basey - curry
                            if (rotation == 270f || rotation == 90f) changesWN =
                                (sqrt((changesWN * changesWN + changesHN * changesHN).toDouble()) * cos(
                                    Math.toRadians((agl - rotation).toDouble())
                                )).toInt()
                            if (mode == Mode.DRAG) {
                                hideAllView()
//                                draggingView(mParentView, event)
                                verticalSnap(move_orgX, mParentView, event)
                                horizontalSnap(move_orgY, mParentView, event)
                            }
                            if (mode == Mode.LEFT_WRAP) {
                                if (imageLeftWrap != null)
                                    scaleListener!!.onTouch(imageLeftWrap!!, event)
                            }
                            if (mode == Mode.RIGHT_WRAP) {
                                if (imageRightWrap != null)
                                    scaleRightListener!!.onTouch(imageRightWrap!!, event)
                            }
//                            if (!isXBaseLineDetected)
                            move_orgX = event.rawX
//                            if (!isYBaseLineDetected)
                            move_orgY = event.rawY
                            requestLayout()
                            invalidate()
                        }

                        MotionEvent.ACTION_UP -> {
                            visibleAllView()
                            val bounds = Rect()
                            getGlobalVisibleRect(bounds)
                            isClick = false
                            if (mode == Mode.LEFT_WRAP) {
                                if (imageLeftWrap != null)
                                    scaleListener!!.onTouch(imageLeftWrap!!, event)
                            }
                            if (mode == Mode.RIGHT_WRAP) {
                                if (imageRightWrap != null)
                                    scaleRightListener!!.onTouch(imageRightWrap!!, event)
                            }
                            if (mode == Mode.DRAG) {
                                if (iStickerOperation != null) {
                                    iStickerOperation!!.onDragEnd(mTag)
                                }
                            }
                            mode = Mode.NONE
                            isParentTouch = false
                        }
                    }
                }

                if (iStickerOperation != null && enableBorder) {
                    iStickerOperation!!.onEdit(mTag, event.action != MotionEvent.ACTION_MOVE)
//                    iStickerOperation!!.onEdit(mTag, true)
                }

                if (event.pointerCount == 2) {
                    mScaleGestureDetector!!.onTouchEvent(this@CustomTextView, event)
                    return@OnTouchListener true
                }
            } else {
                gestureDetector!!.onTouchEvent(event)
            }
            true
        } else false
    }

    init {
        isCursorVisible = false
        gravity = Gravity.CENTER
        textGravityIndex = 1
        setup()

        mVViewBaseLine = viewVertical
        mHViewBaseLine = viewHorizontal
        basex = parent.width / 2
        basey = parent.height / 2
        pivx = parent.width / 2
        pivy = parent.height / 2

        gestureDetector = GestureDetector(cntx, ClickGesture())
        mScaleGestureDetector =
            CustomScaleGestureDetector(
                ScaleGestureListener()
            )
        setOnTouchListener(mTouchListener)
        gestureDetector = GestureDetector(context, ClickGesture())

        scaleListener = ScaleListener()
        scaleRightListener = ScaleRightListener()
        if (imageLeftWrap != null)
            imageLeftWrap!!.setOnTouchListener(scaleListener)
        if (imageRightWrap != null)
            imageRightWrap!!.setOnTouchListener(scaleRightListener)
    }

    private val unlimitedHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, unlimitedHeight)
    }

    override fun onRotate(detector: CustomRotationGestureDetector?): Boolean {
        if (detector != null) {
            this.animate().rotationBy(detector.getRotationDelta()).setDuration(0)
                .setInterpolator(LinearInterpolator()).start()
        }
        return true
    }

    override fun onRotationBegin(detector: CustomRotationGestureDetector?): Boolean {
        return true
    }

    override fun onRotationEnd(detector: CustomRotationGestureDetector?) {
    }

    //=====================

    enum class State {
        NONE
    }

    var state: State? = null

    private var mDetector: GestureDetector? = null

    fun setupView(context: Context) {
        mDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    isEditEnabled = true
                    invalidate()
                    return true
                }

                override fun onDoubleTap(motionEvent: MotionEvent): Boolean {
                    if (isEditEnabled && (motionEvent != null)) {
                        val localRect = Rect(left, top, right, bottom)
                        if (localRect.contains(
                                motionEvent.x.roundToInt(),
                                motionEvent.y.roundToInt()
                            )
                        ) {
                            return true
                        }
                    }
                    return super.onDoubleTap(motionEvent)
                }
            })
    }

    fun applyColor(colorCode: String = "#000000", byUser: Boolean = false) {

        this.colorName = colorCode
        paint.shader = null
        setTextColor(Color.parseColor(colorCode))
        setTextColor(textColors.withAlpha(elementAlpha * 255 / 100))
        if (byUser) isProjectHasChanges = true
    }

    fun applyLetterSpacing(mLetterSpacing: Float = 0F, byUser: Boolean = false) {
        letterSpacing = mLetterSpacing
        if (iStickerOperation != null && enableBorder) {
            iStickerOperation!!.onEdit(mTag, true)
        }
        if (byUser) isProjectHasChanges = true
    }

    fun applyLineSpacing(mLineSpacing: Float = 0F, byUser: Boolean = false) {
        this.mLineSpacing = mLineSpacing
        setLineSpacing(mLineSpacing, 0.0f)
        updateScaleSize()
        if (iStickerOperation != null && enableBorder) {
            iStickerOperation!!.onEdit(mTag, true)
        }
        if (byUser) isProjectHasChanges = true
    }

    open fun draggingView(mParentView: View, event: MotionEvent) {
        val parentViewWidth = mParentView.width / 2
        val viewWidth = width / 2
        val newMoveX: Float =
            x + if (isXBaseLineDetected) (event.rawX - move_orgX) * 2 else event.rawX - move_orgX
        if (newMoveX + viewWidth > parentViewWidth - 10 && newMoveX + viewWidth < parentViewWidth + 10) {
            if (mVViewBaseLine != null) mVViewBaseLine!!.visibility = VISIBLE
            x = (parentViewWidth - viewWidth).toFloat()
            isXBaseLineDetected = true
        } else {
            if (mVViewBaseLine != null) mVViewBaseLine!!.visibility = GONE
            x = newMoveX
            isXBaseLineDetected = false
        }
        val parentViewHeight = mParentView.height / 2
        val viewHeight = height / 2
        val newMoveY: Float =
            y + if (isYBaseLineDetected) (event.rawY - move_orgY) * 2 else event.rawY - move_orgY
        if (newMoveY + viewHeight > parentViewHeight - 10 && newMoveY + viewHeight < parentViewHeight + 10) {
            if (mHViewBaseLine != null) mHViewBaseLine!!.visibility = VISIBLE
            y = (parentViewHeight - viewHeight).toFloat()
            isYBaseLineDetected = true
        } else {
            if (mHViewBaseLine != null) mHViewBaseLine!!.visibility = GONE
            y = newMoveY
            isYBaseLineDetected = false
        }
        if (!isXBaseLineDetected) move_orgX = event.rawX
        if (!isYBaseLineDetected) move_orgY = event.rawY
    }

    private fun horizontalSnap(move_orgY: Float, mParentView: View, event: MotionEvent) {
        val parentViewHeight = mParentView.height / 2
        val viewHeight = height / 2
        val newMoveY =
            y + if (isYBaseLineDetected) ((event.rawY - move_orgY) * 2) else ((event.rawY - move_orgY))
        val hitRect = Rect()
        getHitRect(hitRect)
        val top = hitRect.top
        val bottom = hitRect.bottom
        val newMove =
            top + if (isYBaseLineDetected) ((event.rawY - move_orgY) * 2) else ((event.rawY - move_orgY))

        elementPositionList.forEach { (elementId, elementPositionData) ->
            if (elementId != this.id) {
                val currentElementCenter =
                    elementPositionData.top!! + ((elementPositionData.bottom!! - elementPositionData.top!!) / 2)

                if (newMove > (elementPositionData.top!! - 3f) && newMove < (elementPositionData.top!! + 3f)
                ) {
                    // current Top Other Top
                    updateHorizontalSnap(
                        newMoveY + (elementPositionData.top!! - newMove),
                        true,
                        (elementPositionData.top!!).toInt(),
                        true
                    )
                    return
                } else if (newMove > (elementPositionData.bottom!! - 3f) && newMove < (elementPositionData.bottom!! + 3f)
                ) {
                    // current Top Other Bottom
                    updateHorizontalSnap(
                        newMoveY + (elementPositionData.bottom!! - newMove),
                        true,
                        elementPositionData.bottom!!.toInt(),
                        true
                    )
                    return
                } else if (bottom > elementPositionData.top!! - 3f && bottom < elementPositionData.top!! + 3f
                ) {
                    // current Bottom Other Top
                    updateHorizontalSnap(
                        newMoveY + (elementPositionData.top!! - bottom),
                        true,
                        elementPositionData.top!!.toInt(),
                        true
                    )
                    return
                } else if (bottom > elementPositionData.bottom!! - 3f && bottom < elementPositionData.bottom!! + 3f
                ) {
                    // current Bottom Other Bottom
                    updateHorizontalSnap(
                        newMoveY + (elementPositionData.bottom!! - bottom),
                        true,
                        elementPositionData.bottom!!.toInt(),
                        true
                    )
                    return
                } else if (newMoveY + viewHeight > currentElementCenter - 3f && newMoveY + viewHeight < currentElementCenter + 3f) {
                    // current Center Other Center
                    updateHorizontalSnap(
                        (currentElementCenter - viewHeight),
                        true,
                        currentElementCenter.toInt(),
                        true
                    )
                    return
                } else if (newMove > currentElementCenter - 3f && newMove < currentElementCenter + 3f
                ) {
                    // current Top Other Center
                    updateHorizontalSnap(
                        newMoveY + (currentElementCenter - newMove),
                        true,
                        currentElementCenter.toInt(),
                        true
                    )
                    return
                } else if (bottom > currentElementCenter - 3f && bottom < currentElementCenter + 3f
                ) {
                    // current Bottom Other Center
                    updateHorizontalSnap(
                        newMoveY + (currentElementCenter - bottom),
                        true,
                        currentElementCenter.toInt(),
                        true
                    )
                    return
                } else if (newMoveY + viewHeight > elementPositionData.top!! - 3f && newMoveY + viewHeight < elementPositionData.top!! + 3f) {
                    // current Center Other Top
                    updateHorizontalSnap(
                        (elementPositionData.top!! - viewHeight),
                        true,
                        elementPositionData.top!!.toInt(),
                        true
                    )
                    return
                } else if (newMoveY + viewHeight > elementPositionData.bottom!! - 3f && newMoveY + viewHeight < elementPositionData.bottom!! + 3f) {
                    // current Center Other Bottom
                    updateHorizontalSnap(
                        (elementPositionData.bottom!! - viewHeight),
                        true,
                        elementPositionData.bottom!!.toInt(),
                        true
                    )
                    return
                } else if (newMoveY + viewHeight > parentViewHeight - 3f && newMoveY + viewHeight < parentViewHeight + 3f) {
                    // current Center Main Center
                    updateHorizontalSnap(
                        (parentViewHeight - viewHeight).toFloat(),
                        true,
                        parentViewHeight
                    )
                    return
                } else if (newMoveY + viewHeight > mParentView.y - 3f && newMoveY + viewHeight < mParentView.y + 3f) {
                    // current Center Main Top
                    updateHorizontalSnap(
                        (mParentView.y - viewHeight).toFloat(),
                        true,
                        (mParentView.y).toInt()
                    )
                    return
                } else if (newMoveY + viewHeight > mParentView.height - 3f && newMoveY + viewHeight < mParentView.height + 3f) {
                    // current Center Main Bottom
                    updateHorizontalSnap(
                        (mParentView.height - viewHeight).toFloat(),
                        true,
                        mParentView.height - 3
                    )
                    return
                } else if (newMove > mParentView.y - 3f && newMove < mParentView.y + 3f) {
                    // current Top Main Top
                    updateHorizontalSnap(
                        newMoveY + (mParentView.y - newMove),
                        true,
                        (mParentView.y).toInt()
                    )
                    return
                } else if (bottom > mParentView.height - 3f && bottom < mParentView.height + 3f) {
                    // current Bottom Main Bottom
                    updateHorizontalSnap(
                        newMoveY + (mParentView.height - bottom),
                        true,
                        mParentView.height - 3
                    )
                    return
                } else if (bottom > parentViewHeight - 3f && bottom < parentViewHeight + 3f) {
                    // current Bottom Main Center
                    updateHorizontalSnap(
                        newMoveY + (parentViewHeight - bottom),
                        true,
                        parentViewHeight
                    )
                    return
                } else if (newMove > parentViewHeight - 3f && newMove < parentViewHeight + 3f) {
                    // current Top Main Center
                    updateHorizontalSnap(
                        newMoveY + (parentViewHeight.toFloat() - newMove),
                        true,
                        parentViewHeight
                    )
                    return
                }/*else if (newMove > mParentView.height - 3f && newMove < mParentView.height + 3f) {
                    // current Top Main Bottom
                    updateHorizontalSnap(
                        newMoveY + (mParentView.height - newMove),
                        true,
                        mParentView.height - 3
                    )
                    return
                }else if (bottom > mParentView.y - 3f && bottom < mParentView.y + 3f) {
                    // current Bottom Main Top
                    updateHorizontalSnap(
                        newMoveY + (mParentView.y - bottom),
                        true,
                        (mParentView.y).toInt()
                    )
                    return
                }*/ else {
                    updateHorizontalSnap(newMoveY, false, parentViewHeight, true)
                    return@forEach
                }
            }

        }

        if (elementPositionList.size == 1) {
            commonHorizontalSnap(move_orgY, mParentView, event)
        }

    }

    private fun commonHorizontalSnap(move_orgY: Float, mParentView: View, event: MotionEvent) {
        val parentViewHeight = mParentView.height / 2
        val viewHeight = height / 2
        val newMoveY =
            y + if (isYBaseLineDetected) ((event.rawY - move_orgY) * 2) else (event.rawY - move_orgY)
        val hitRect = Rect()
        getHitRect(hitRect)
        val top = hitRect.top
        val bottom = hitRect.bottom
        val newMove =
            top + if (isYBaseLineDetected) ((event.rawY - move_orgY) * 2) else (event.rawY - move_orgY)

        if (newMoveY + viewHeight > parentViewHeight - 3f && newMoveY + viewHeight < parentViewHeight + 3f) {
            // current Center Main Center
            updateHorizontalSnap((parentViewHeight - viewHeight).toFloat(), true, parentViewHeight)
            return
        } else if (newMoveY + viewHeight > mParentView.y - 3f && newMoveY + viewHeight < mParentView.y + 3f) {
            // current Center Main Top
            updateHorizontalSnap(
                (mParentView.y - viewHeight).toFloat(),
                true,
                (mParentView.y).toInt()
            )
            return
        } else if (newMoveY + viewHeight > mParentView.height - 3f && newMoveY + viewHeight < mParentView.height + 3f) {
            // current Center Main Bottom
            updateHorizontalSnap(
                (mParentView.height - viewHeight).toFloat(),
                true,
                mParentView.height - 3
            )
            return
        } else if (newMove > mParentView.y - 3f && newMove < mParentView.y + 3f) {
            // current Top Main Top
            updateHorizontalSnap(
                newMoveY + (mParentView.y - newMove),
                true,
                (mParentView.y).toInt()
            )
            return
        } else if (bottom > mParentView.height - 3f && bottom < mParentView.height + 3f) {
            // current Bottom Main Bottom
            updateHorizontalSnap(
                newMoveY + (mParentView.height - bottom),
                true,
                mParentView.height - 3
            )
            return
        } else if (bottom > parentViewHeight - 3f && bottom < parentViewHeight + 3f) {
            // current Bottom Main Center
            updateHorizontalSnap(
                newMoveY + (parentViewHeight - bottom),
                true,
                parentViewHeight
            )
            return
        } else if (newMove > parentViewHeight - 3f && newMove < parentViewHeight + 3f) {
            // current Top Main Center
            updateHorizontalSnap(
                newMoveY + (parentViewHeight.toFloat() - newMove),
                true,
                parentViewHeight
            )
            return
        }/*else if (newMove > mParentView.height - 3f && newMove < mParentView.height + 3f) {
            // current Top Main Bottom
            updateHorizontalSnap(
                newMoveY + (mParentView.height - newMove),
                true,
                mParentView.height - 3
            )
            return
        }else if (bottom > mParentView.y - 3f && bottom < mParentView.y + 3f) {
            // current Bottom Main Top
            updateHorizontalSnap(
                newMoveY + (mParentView.height - bottom),
                true,
                (mParentView.y).toInt()
            )
            return
        }*/ else {
            updateHorizontalSnap(newMoveY, false, parentViewHeight)
            return
        }
    }

    private fun updateHorizontalSnap(
        newY: Float,
        isHorizontalBaseLine: Boolean,
        updateY: Int,
        isOtherElement: Boolean = false
    ) {

        showHideBaseLine(-1, if (isHorizontalBaseLine) VISIBLE else GONE)
        mHViewBaseLine!!.y = updateY.toFloat()
        mHViewBaseLine!!.bringToFront()
        mHViewBaseLine!!.setBackgroundColor(
            ContextCompat.getColor(
                context,
                if (isOtherElement) R.color.snap_line_color else R.color.snap_line_edit_area
            )
        )
//        if (!isYBaseLineDetected)
        y = newY
        isYBaseLineDetected = isHorizontalBaseLine
    }

    private fun verticalSnap(move_orgX: Float, mParentView: View, event: MotionEvent) {
        val parentViewWidth = mParentView.width / 2
        val newMoveX =
            x + if (isXBaseLineDetected) ((event.rawX - move_orgX) * 2) else ((event.rawX - move_orgX))
        val hitRect = Rect()
        getHitRect(hitRect)
        val start = hitRect.left
        val end = hitRect.right
        val newMove =
            start + if (isXBaseLineDetected) ((event.rawX - move_orgX) * 2) else ((event.rawX - move_orgX))

        elementPositionList.forEach { (elementId, elementPositionData) ->
            if (elementId != this.id) {
                val otherElementCenter =
                    elementPositionData.start!! + ((elementPositionData.end!! - elementPositionData.start!!) / 2)

                if (newMove > (elementPositionData.start!! - 3f) && newMove < (elementPositionData.start!! + 3f)
                ) {
                    // current Start Other Start
                    updateVerticalSnap(
                        newMoveX + (elementPositionData.start!! - newMove),
                        true,
                        (elementPositionData.start!!).toInt(),
                        true
                    )
//                    return@forEach
                    return
                } else if (newMove > (elementPositionData.end!! - 3f) && newMove < (elementPositionData.end!! + 3f)
                ) {
                    // current Start Other End
                    updateVerticalSnap(
                        newMoveX + (elementPositionData.end!! - newMove),
                        true,
                        elementPositionData.end!!.toInt(),
                        true
                    )
                    return
//                    return@forEach
                } else if (end > elementPositionData.start!! - 3f && end < elementPositionData.start!! + 3f) {
                    // current End Other Start
                    updateVerticalSnap(
                        newMoveX + (elementPositionData.start!! - end),
                        true,
                        elementPositionData.start!!.toInt(),
                        true
                    )
                    return
//                    return@forEach
                } else if (end > elementPositionData.end!! - 3f && end < elementPositionData.end!! + 3f) {
                    // current End Other End
                    updateVerticalSnap(
                        newMoveX + (elementPositionData.end!! - end),
                        true,
                        elementPositionData.end!!.toInt(),
                        true
                    )
                    return
//                    return@forEach
                } else if (newMoveX + (width / 2) > otherElementCenter - 3f && newMoveX + (width / 2) < otherElementCenter + 3f) {
                    // current Center Other Center
                    updateVerticalSnap(
                        (otherElementCenter - (width / 2)),
                        true,
                        otherElementCenter.toInt(),
                        true
                    )
                    return
//                    return@forEach
                } else if (newMove > otherElementCenter - 3f && newMove < otherElementCenter + 3f) {
                    // current Start Other Center
                    updateVerticalSnap(
                        newMoveX + (otherElementCenter - newMove),
                        true,
                        otherElementCenter.toInt(),
                        true
                    )
                    return
//                    return@forEach
                } else if (end > otherElementCenter - 3f && end < otherElementCenter + 3f) {
                    // current End Other Center
                    updateVerticalSnap(
                        newMoveX + (otherElementCenter - end),
                        true,
                        otherElementCenter.toInt(),
                        true
                    )
                    return
//                    return@forEach
                } else if (newMoveX + (width / 2) > elementPositionData.start!! - 3f && newMoveX + (width / 2) < elementPositionData.start!! + 3f) {
                    // current Center Other Start
                    updateVerticalSnap(
                        (elementPositionData.start!! - (width / 2)),
                        true,
                        elementPositionData.start!!.toInt(),
                        true
                    )
                    return
//                    return@forEach
                } else if (newMoveX + (width / 2) > elementPositionData.end!! - 3f && newMoveX + (width / 2) < elementPositionData.end!! + 3f) {
                    // current Center Other End
                    updateVerticalSnap(
                        (elementPositionData.end!! - (width / 2)),
                        true,
                        elementPositionData.end!!.toInt(),
                        true
                    )
                    return
//                    return@forEach
                } else if (newMoveX + (width / 2) > parentViewWidth - 3f && newMoveX + (width / 2) < parentViewWidth + 3f) {
                    // current Center Main Center
                    updateVerticalSnap(
                        (parentViewWidth - (width / 2)).toFloat(),
                        true,
                        parentViewWidth
                    )
                    return
//                    return@forEach
                } else if (newMoveX + (width / 2) > mParentView.x - 3f && newMoveX + (width / 2) < mParentView.x + 3f) {
                    // current Center Main Start
                    updateVerticalSnap(
                        (mParentView.x - (width / 2)).toFloat(),
                        true,
                        (mParentView.x).toInt()
                    )
                    return
//                    return@forEach
                } else if (newMoveX + (width / 2) > mParentView.width - 3f && newMoveX + (width / 2) < mParentView.width + 3f) {
                    // current Center Main End
                    updateVerticalSnap(
                        (mParentView.width - (width / 2)).toFloat(),
                        true,
                        mParentView.width - 3
                    )
                    return
//                    return@forEach
                } else if (newMove > mParentView.x - 3f && newMove < mParentView.x + 3f) {
                    // current Start Main Start
                    updateVerticalSnap(
                        newMoveX + (mParentView.x - newMove),
                        true,
                        (mParentView.x).toInt()
                    )
                    return
//                    return@forEach
                } else if (end > mParentView.width - 2.5f && end < mParentView.width + 2.5f) {
                    // current End Main End
                    updateVerticalSnap(
                        (newMoveX + (mParentView.width - end)),
                        true,
                        mParentView.width - 3
                    )
                    return
//                    return@forEach
                } else if (end > parentViewWidth - 3f && end < parentViewWidth + 3f) {
                    // current End Main Center
                    updateVerticalSnap(
                        newMoveX + (parentViewWidth - end),
                        true,
                        parentViewWidth
                    )
                    return
//                    return@forEach
                } else if (newMove > parentViewWidth - 3f && newMove < parentViewWidth + 3f) {
                    // current Start Main Center
                    updateVerticalSnap(
                        newMoveX + (parentViewWidth.toFloat() - newMove),
                        true,
                        parentViewWidth
                    )
                    return
//                    return@forEach
                }/* else if (end > mParentView.x - 3f && end < mParentView.x + 3f) {
                    // current End Main Start
                    updateVerticalSnap(
                        (newMoveX + (mParentView.x - end)),
                        true,
                        mParentView.x.toInt()
                    )
                    return
//                    return@forEach
                } else if (newMove > mParentView.width - 2.5f && newMove < mParentView.width + 2.5f) {
                    // current Start Main End
                    updateVerticalSnap(
                        (newMoveX + (mParentView.width - newMove)),
                        true,
                        mParentView.width - 3
                    )
                    return
//                    return@forEach
                }*/ else {
                    updateVerticalSnap(newMoveX, false, parentViewWidth)
//                    return
                    return@forEach
                }

            }

        }

        if (elementPositionList.size == 1) {
            commonVerticalSnap(move_orgX, mParentView, event)
        }
    }

    private fun commonVerticalSnap(move_orgX: Float, mParentView: View, event: MotionEvent) {
        val parentViewWidth = mParentView.width / 2
        val hitRect = Rect()
        getHitRect(hitRect)
        val start = hitRect.left
        val end = hitRect.right
        val newMoveX =
            x + if (isXBaseLineDetected) ((event.rawX - move_orgX) * 2) else ((event.rawX - move_orgX))
        val newMove =
            start + if (isXBaseLineDetected) ((event.rawX - move_orgX) * 2) else ((event.rawX - move_orgX))
/*

        val rect = Rect()
        mParentView.getLocalVisibleRect(rect)

        val offsetViewBounds = Rect()
//returns the visible bounds
//returns the visible bounds
        getDrawingRect(offsetViewBounds)
// calculates the relative coordinates to the parent
// calculates the relative coordinates to the parent
//        rootView.offsetDescendantRectToMyCoords(this, offsetViewBounds)

        val parentLocation = IntArray(2)
        val viewLocation = IntArray(2)
        val parent = getParent() as View
        getLocationOnScreen(viewLocation)
        parent.getLocationOnScreen(parentLocation)

*/


        if (newMoveX + (width / 2) > parentViewWidth - 3f && newMoveX + (width / 2) < parentViewWidth + 3f) {
            // current Center Main Center
            updateVerticalSnap(
                (parentViewWidth - (width / 2)).toFloat(),
                true,
                parentViewWidth
            )
//            return
        } else if (newMoveX + (width / 2) > mParentView.x - 3f && newMoveX + (width / 2) < mParentView.x + 3f) {
            // current Center Main Start
            updateVerticalSnap(
                (mParentView.x - (width / 2)).toFloat(),
                true,
                (mParentView.x).toInt()
            )
//            return
        } else if (newMoveX + (width / 2) > mParentView.width - 3f && newMoveX + (width / 2) < mParentView.width + 3f) {
            // current Center Main End
            updateVerticalSnap(
                (mParentView.width - (width / 2)).toFloat(),
                true,
                mParentView.width - 3
            )
//            return
        } else if (newMove > mParentView.x - 3f && newMove < mParentView.x + 3f) {
            // current Start Main Start
            updateVerticalSnap(
                newMoveX + (mParentView.x - newMove),
                true,
                (mParentView.x).toInt()
            )
//            return
        } else if (end > mParentView.width - 3f && end < mParentView.width + 3f) {
            // current End Main End
            updateVerticalSnap(
                newMoveX + (mParentView.width - end),
                true,
                mParentView.width - 3
            )
//            return
        } else if (end > parentViewWidth - 3f && end < parentViewWidth + 3f) {
            // current End Main Center
            updateVerticalSnap(
                newMoveX + (parentViewWidth - end),
                true,
                parentViewWidth
            )
//            return
        } else if (newMove > parentViewWidth - 3f && newMove < parentViewWidth + 3f) {
            // current Start Main Center
            updateVerticalSnap(
                newMoveX + (parentViewWidth - newMove),
                true,
                parentViewWidth
            )
//            return
        }/*else if (end > mParentView.x - 3f && end < mParentView.x + 3f) {
            // current End Main Start
            updateVerticalSnap(
                (newMoveX + (mParentView.x - end)),
                true,
                mParentView.x.toInt()
            )
            return
//                    return@forEach
        } else if (newMove > mParentView.width - 2.5f && newMove < mParentView.width + 2.5f) {
            // current Start Main End
            updateVerticalSnap(
                (newMoveX + (mParentView.width - newMove)),
                true,
                mParentView.width - 3
            )
            return
//                    return@forEach
        }*/ else {
            updateVerticalSnap(newMoveX, false, parentViewWidth)
//            return
        }


    }

    private fun updateVerticalSnap(
        newX: Float,
        isVerticalBaseLine: Boolean,
        updateX: Int,
        isOtherElement: Boolean = false
    ) {
        showHideBaseLine(if (isVerticalBaseLine) VISIBLE else GONE, -1)
        mVViewBaseLine!!.x = updateX.toFloat()
        mVViewBaseLine!!.bringToFront()
        mVViewBaseLine!!.setBackgroundColor(
            ContextCompat.getColor(
                context,
                if (isOtherElement) R.color.snap_line_color else R.color.snap_line_edit_area
            )
        )
        x = newX
        isXBaseLineDetected = isVerticalBaseLine
    }

    var numberOfTaps = 0
    var lastTapTimeMs: Long = 0
    var touchDownMs: Long = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (currentSelectedViewType == ElementType.NONE || currentSelectedViewType == ElementType.TEXT) {
            maxWrap = maxWidth().toInt() + extraMaxWidth1
            val mLayoutParams = layoutParams as FrameLayout.LayoutParams
            val mParentView = this.parent as View
            if (enableBorder) {
                if (event.pointerCount == 1) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            touchDownMs = System.currentTimeMillis()
                            move_orgX = event.rawX
                            move_orgY = event.rawY
                            basex = (event.rawX - mLayoutParams.leftMargin.toFloat()).toInt()
                            basey = (event.rawY - mLayoutParams.topMargin.toFloat()).toInt()
                            basew = width
                            baseh = height
                            baseWidth = width
                            centerX =
                                (this.x + (this.parent as View).x + this.width.toFloat() / 2).toDouble()
                            isClick = true
                            val rightRect = Rect()
                            if (imageRightWrap != null)
                                imageRightWrap!!.getGlobalVisibleRect(rightRect)
                            val leftRect = Rect()
                            if (imageLeftWrap != null)
                                imageLeftWrap!!.getGlobalVisibleRect(leftRect)

                            if (rightRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                                if (imageRightWrap != null)
                                    scaleRightListener!!.onTouch(imageRightWrap!!, event)
                                mode = Mode.RIGHT_WRAP
                            } else if (leftRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                                if (imageLeftWrap != null)
                                    scaleListener!!.onTouch(imageLeftWrap!!, event)
                                mode = Mode.LEFT_WRAP
                            } else mode = Mode.DRAG
                        }

                        MotionEvent.ACTION_MOVE -> {
                            isClick = true
                            val currx = event.rawX.toInt()
                            val curry = event.rawY.toInt()
                            var agl = Math.toDegrees(
                                Math.atan2(
                                    (curry - basey).toDouble(),
                                    (currx - basex).toDouble()
                                )
                            ).toFloat()
                            if (agl < 0.0f) {
                                agl += 360.0f
                            }
                            var changesWN =
                                if (rotation >= 90 && rotation <= 270) currx - basex else basex - currx
                            val changesHN =
                                if (rotation >= 90 && rotation <= 270) curry - basey else basey - curry
                            if (rotation == 270f || rotation == 90f) changesWN =
                                (Math.sqrt((changesWN * changesWN + changesHN * changesHN).toDouble()) * Math.cos(
                                    Math.toRadians((agl - rotation).toDouble())
                                )).toInt()
                            if (mode == Mode.DRAG) {
                                hideAllView()
//                                draggingView(mParentView, event)
                                verticalSnap(move_orgX, mParentView, event)
                                horizontalSnap(move_orgY, mParentView, event)
                            }
                            if (mode == Mode.LEFT_WRAP) {
                                if (imageLeftWrap != null)
                                    scaleListener!!.onTouch(imageLeftWrap!!, event)
                            }
                            if (mode == Mode.RIGHT_WRAP) {
                                if (imageRightWrap != null)
                                    scaleRightListener!!.onTouch(imageRightWrap!!, event)
                            }
                            move_orgX = event.rawX
                            move_orgY = event.rawY
//                            if (!isXBaseLineDetected)
//                                move_orgX = event.rawX
//                            if (!isYBaseLineDetected)
//                                move_orgY = event.rawY
                            requestLayout()
                        }

                        MotionEvent.ACTION_UP -> {
                            //TODO: https://stackoverflow.com/questions/27757099/android-detect-doubletap-and-tripletap-on-view
                            //TODO: https://medium.com/mobile-app-development-publication/understanding-android-touch-flow-control-bcc413e6a57e
                            if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                                //it was not a tap
                                numberOfTaps = 0
                                lastTapTimeMs = 0
                            }
                            if (numberOfTaps > 0
                                && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()
                            ) {
                                numberOfTaps += 1
                            } else {
                                numberOfTaps = 1
                            }
                            lastTapTimeMs = System.currentTimeMillis()
                            if (numberOfTaps == 2) {
                                handler.postDelayed({ //handle double tap
                                    if (enableBorder && isMotionEventInsideView(event)) {
                                        if (!isParentTouch) if (iStickerOperation != null) iStickerOperation!!.onDoubleClick(
                                            mTag
                                        )
                                    }
                                    showHideBaseLine(GONE, GONE)
                                }, ViewConfiguration.getDoubleTapTimeout().toLong())
                            }

                            visibleAllView()
                            val bounds = Rect()
                            getGlobalVisibleRect(bounds)
                            if (mode == Mode.LEFT_WRAP) {
                                if (imageLeftWrap != null)
                                    scaleListener!!.onTouch(imageLeftWrap!!, event)
                            }
                            if (mode == Mode.RIGHT_WRAP) {
                                if (imageRightWrap != null)
                                    scaleRightListener!!.onTouch(imageRightWrap!!, event)
                            }
                            showHideBaseLine(GONE, GONE)
                            isClick = false
                            if (mode == Mode.DRAG) {
                                if (iStickerOperation != null) {
                                    iStickerOperation!!.onDragEnd(mTag)
                                }
                            }
                            mode = Mode.NONE
                            isParentTouch = false
                        }
                    }
                }

                if (iStickerOperation != null && enableBorder) {
                    iStickerOperation!!.onEdit(mTag, event.action != MotionEvent.ACTION_MOVE)
//                    iStickerOperation!!.onEdit(mTag, true)
                }

                if (event.pointerCount == 2) {
                    mScaleGestureDetector!!.onTouchEvent(this, event)
                    return true
                }
            }
            if (isMotionEventInsideView(event)) {
                gestureDetector!!.onTouchEvent(event)
            }
            return true
        }
        if (isLock == 0) {
            if (mEventCallbackListener != null) {
                if (/*com.hashone.module.textview.utils.Constants*/currentSelectedViewId != -1 && id != currentSelectedViewId) {
                    mEventCallbackListener!!.onEventDetected(event)
                }
            }
            if (isMotionEventInsideView(event)) {
                gestureDetector!!.onTouchEvent(event)
                return true
            }
        }
        return false
    }

    private fun visibleSingleView(selectView: ImageView) {
        if (imageLeftWrap != null)
            imageLeftWrap!!.visibility =
                if (selectView == imageLeftWrap) VISIBLE else INVISIBLE
        if (imageRightWrap != null)
            imageRightWrap!!.visibility =
                if (selectView == imageRightWrap) VISIBLE else INVISIBLE
    }

    fun hideAllView() {
        if (imageLeftWrap != null)
            imageLeftWrap!!.visibility = INVISIBLE
        if (imageRightWrap != null)
            imageRightWrap!!.visibility = INVISIBLE
    }

    fun hideWrapView() {
        if (imageLeftWrap != null)
            imageLeftWrap!!.visibility = INVISIBLE
        if (imageRightWrap != null)
            imageRightWrap!!.visibility = INVISIBLE
    }

    fun visibleAllView() {
        showHideBaseLine(GONE, GONE)
        if (imageLeftWrap != null)
            imageLeftWrap!!.visibility = VISIBLE
        if (imageRightWrap != null)
            imageRightWrap!!.visibility = VISIBLE
    }

    override fun onScaleBegin(scaleDetector: android.view.ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScale(scaleDetector: android.view.ScaleGestureDetector): Boolean {
        val sizeWidth = convertDpToPixel(SELF_WIDTH_SIZE_DP.toFloat(), context)
        val sizeHeight = convertDpToPixel(SELF_HEIGHT_SIZE_DP.toFloat(), context)
        val scaleFactor = scaleDetector.scaleFactor
        val _width = (width.toFloat() * scaleFactor).toInt()
        val _height = (height.toFloat() * scaleFactor).toInt()
        if (_width > 100.coerceAtMost(sizeWidth / 2) && _height > 100.coerceAtMost(sizeHeight / 2) && _width < Math.min(
                1080,
                sizeWidth * 3
            ) && _height < 1080.coerceAtMost(sizeHeight * 3)
        ) {
            if (lastScaleFactor == 0.0f || sign(scaleFactor) == sign(
                    lastScaleFactor
                )
            ) {
                scale *= scaleFactor
                scale = MIN_ZOOM.coerceAtLeast(
                    scale.coerceAtMost(
                        MAX_ZOOM
                    )
                )
                lastScaleFactor = scaleFactor
            } else {
                lastScaleFactor = 0.0f
            }
            val params = layoutParams as FrameLayout.LayoutParams
            params.height = _height
            params.width = _width
            layoutParams = params
        }

        if (iStickerOperation != null && enableBorder) {
            iStickerOperation!!.onEdit(mTag, true)
        }
        return true
    }

    override fun onScaleEnd(scaleDetector: android.view.ScaleGestureDetector) {}

    fun minMaxLengthWords(input: String?): Float {
        var maxWord = input
        val minWord: String = "W"
        maxWord = Arrays.stream(text.toString().split("\n").toTypedArray()).max(
            Comparator.comparingInt { obj: String? -> obj!!.length }).orElse(null)
        minWidth = minWord.length.toFloat()
        minWrap = paint.measureText(minWord, 0, minWord.length).roundToInt()
        minWrapI = paint.measureText("I", 0, ("I").length)
            .roundToInt()
        assert(maxWord != null)
        maxWidth = paint.measureText(maxWord, 0, maxWord!!.length)
        return maxWidth
    }

    fun maxWidth(): Float {
        minMaxLengthWords(text.toString())
        return maxWidth
    }

    fun largeWidth() {
        lineCountForLargeWidth = getLineWiseText()
    }


    fun setEventCallbackListener(mEventCallbackListener: EventCallbackListener?) {
        this.mEventCallbackListener = mEventCallbackListener
    }

    internal inner class ClickGesture : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (enableBorder) {
                if (currentSelectedViewId == id)
                    if (iStickerOperation != null) {
                        iStickerOperation!!.onDoubleClick(mTag)
                    }
                showHideBaseLine(GONE, GONE)
            }
            return false
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (!enableBorder) {
                if (isMotionEventInsideView(e)) {
                    enableBorder = true
                    isEditEnabled = true
                    mSelectedTag = tag.toString()
                    currentSelectedViewId = id
                    if (iStickerOperation != null) iStickerOperation!!.onSelect(mTag)
                    return true
                }
            }
            if (mEventCallbackListener != null) {
                mEventCallbackListener!!.onNewElementDetected(e)
                return false
            }
            return true
        }
    }

    fun setOnSelectListener(iStickerOperation: IStickerOperation?) {
        this.iStickerOperation = iStickerOperation
    }

    inner class ScaleListener : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (isEditEnabled) {
                updateScaleSize()
                maxWrap = maxWidth().toInt() + extraMaxWidth1
                val x_cord = event.rawX.toInt()
                val y_cord = event.rawY.toInt()
                val mLayoutParams = layoutParams as MarginLayoutParams
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //TODO: Only Apply to View NOT VIEW GROUP
                        layGroupX = x
                        layGroupY = y
                        basex = x_cord
                        basey = y_cord
                        basew = width
                        baseh = height
                        margl = mLayoutParams.leftMargin.toFloat()
                        margr = mLayoutParams.rightMargin.toFloat()
                        margt = mLayoutParams.topMargin.toFloat()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (imageLeftWrap != null)
                            visibleSingleView(imageLeftWrap!!)

                        if (!updatedByUser) {
                            val contentString = text!!.replace("\n".toRegex(), " ")
                            text = contentString
                        }
                        var angle = Math.toDegrees(
                            atan2(
                                (basey - y_cord).toDouble(),
                                (basex - x_cord).toDouble()
                            )
                        ).toFloat()
                        if (angle < 0.0f) {
                            angle += 360.0f
                        }
                        var changesW = basex - x_cord
                        var changesH = basey - y_cord
                        changesW =
                            (sqrt((changesW * changesW + changesH * changesH).toDouble()) * cos(
                                Math.toRadians((angle - rotation).toDouble())
                            )).toInt()
                        changesH =
                            (sqrt((changesW * changesW + changesH * changesH).toDouble()) * sin(
                                Math.toRadians((angle - rotation).toDouble())
                            )).toInt()
                        val width = changesW * 2 + basew
                        if (width in (minWrap + 1) until maxWrap) {
                            layoutParams.width = width
                        } else {
                            if (width < 0) {
                                layoutParams.width = minWrap
                            }
                            if (width > maxWrap) {
                                layoutParams.width = maxWrap
                            }
                        }
                        if (iStickerOperation != null && enableBorder) {
                            iStickerOperation!!.onEdit(mTag, true)
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        visibleAllView()
                        largeWidth()
                    }
                }
                invalidate()
            }
            return false
        }
    }

    inner class ScaleRightListener : OnTouchListener {
        @SuppressLint("NewApi")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (isEditEnabled) {
                updateScaleSize()
                maxWrap = maxWidth().toInt() + extraMaxWidth1
                val x_cord = event.rawX.toInt()
                val y_cord = event.rawY.toInt()
                val mLayoutParams = layoutParams as MarginLayoutParams
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        //TODO: Only Apply to View NOT VIEW GROUP
                        basex = x_cord
                        basey = y_cord
                        basew = width
                        baseh = height
                        margl = mLayoutParams.leftMargin.toFloat()
                        margr = mLayoutParams.rightMargin.toFloat()
                        margt = mLayoutParams.topMargin.toFloat()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (imageRightWrap != null)
                            visibleSingleView(imageRightWrap!!)

                        if (!updatedByUser) {
                            val contentString = text!!.replace("\n".toRegex(), " ")
                            text = contentString
                        }
                        var angle = Math.toDegrees(
                            Math.atan2(
                                (y_cord - basey).toDouble(),
                                (x_cord - basex).toDouble()
                            )
                        ).toFloat()
                        if (angle < 0.0f) {
                            angle += 360.0f
                        }
                        var changesW = x_cord - basex
                        var changesH = y_cord - basey
                        changesW =
                            (Math.sqrt((changesW * changesW + changesH * changesH).toDouble()) * Math.cos(
                                Math.toRadians((angle - rotation).toDouble())
                            )).toInt()
                        changesH =
                            (Math.sqrt((changesW * changesW + changesH * changesH).toDouble()) * Math.sin(
                                Math.toRadians((angle - rotation).toDouble())
                            )).toInt()
                        val width = changesW * 2 + basew
                        if (width in (minWrap + 1) until maxWrap) {
                            layoutParams.width = width
                        } else {
                            if (width < 0) {
                                layoutParams.width = minWrap
                            }
                            if (width > maxWrap) {
                                layoutParams.width = maxWrap
                            }
                        }

                        if (iStickerOperation != null && enableBorder) {
                            iStickerOperation!!.onEdit(mTag, true)
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        visibleAllView()
                        largeWidth()
                    }
                }

                invalidate()
            }
            return false
        }
    }

    private inner class ScaleGestureListener :
        CustomScaleGestureDetector.SimpleOnScaleGestureListener() {
        private val mPrevSpanVector = Vector2D()
        private var mPivotX = 0f
        private var mPivotY = 0f
        override fun onScaleBegin(
            view: View,
            detector: CustomScaleGestureDetector
        ): Boolean {
            mPivotX = detector.focusX
            mPivotY = detector.focusY
            mPrevSpanVector.set(detector.currentSpanVector)
            startAng = view.rotation.toDouble()
            startScale = view.scaleX.toDouble()
            hideAllView()
            mode = Mode.ZOOM
            return true
        }

        override fun onScale(
            view: View,
            detector: CustomScaleGestureDetector
        ): Boolean {
            val info = TransformInfo()
            info.deltaScale = detector.scaleFactor
            info.deltaAngle = Vector2D.getAngle(mPrevSpanVector, detector.currentSpanVector)
            info.deltaX = detector.focusX - mPivotX
            info.deltaY = detector.focusY - mPivotY
            info.pivotX = mPivotX
            info.pivotY = mPivotY
            info.minimumScale = minimumScale
            info.maximumScale = maximumScale
            move(view, info)
            return false
        }

        override fun onScaleEnd(view: View, detector: CustomScaleGestureDetector) {
            visibleAllView()
            isParentTouch = false
            super.onScaleEnd(view, detector)
        }
    }

    private inner class TransformInfo {
        var deltaX = 0f
        var deltaY = 0f
        var deltaScale = 0f
        var deltaAngle = 0f
        var pivotX = 0f
        var pivotY = 0f
        var minimumScale = 0f
        var maximumScale = 0f
    }

    private fun move(view: View, info: TransformInfo) {
        val lineCount = lineCount
        scale =
            if (isParentTouch) (startScale * info.deltaScale).toFloat() else view.scaleX * info.deltaScale
        scale = minimumScale.coerceAtLeast(info.maximumScale.coerceAtMost(scale))
        val currentSize = view.height * scale
        val minHeight: Float = if (lineCount == 1) 20F else (25F)
        if (currentSize > minHeight) {
            view.scaleX = scale
            view.scaleY = scale
            requestLayout()
            invalidate()
        }
        var rotation =
            adjustAngle(if (isParentTouch) (startAng + info.deltaAngle).toFloat() else rotation + info.deltaAngle)
        if (rotation < 0) {
            rotation += 360f
        }
        rotation =
            if (rotation < 4 || rotation > 356) 0F else if (rotation > 41 && rotation < 49) 45F else if (rotation > 86 && rotation < 94) 90F else if (rotation > 131 && rotation < 139) 135F else if (rotation > 176 && rotation < 184) 180F else if (rotation > 211 && rotation < 229) 225F else if (rotation > 266 && rotation < 274) 270F else if (rotation > 311 && rotation < 319) 315F else rotation
        if (rotation >= 0.0f) {
            animate().rotation(rotation).setDuration(0L).start()
        }
        isScaleFirstTime = true
    }

    fun visibleAll() {
        isEditEnabled = true
        enableBorder = true
        if (imageLeftWrap != null)
            imageLeftWrap!!.visibility = VISIBLE
        if (imageRightWrap != null)
            imageRightWrap!!.visibility = VISIBLE
    }

    fun select(isVisible: Boolean) {
        enableBorder = isVisible
        setControlItemsHidden(!isVisible)
        if (isVisible) visibleAll() else disableAll()
        invalidate()
    }

    fun disableAll() {
        if (imageLeftWrap != null)
            imageLeftWrap!!.visibility = GONE
        if (imageRightWrap != null)
            imageRightWrap!!.visibility = GONE
        invalidate()
        isEditEnabled = false
        enableBorder = false
    }

    fun changeTextFocus(isFocus: Boolean) {
        isSelected = isFocus
        isClickable = isFocus
    }

    fun setFont(tf: Typeface?, byUser: Boolean = false) {
        typeFace = tf
        setTypeface(null, Typeface.NORMAL)
        typeface = tf
        if (byUser) isProjectHasChanges = true
    }

    fun setText(mTextString: String?, byUser: Boolean) {
        text = mTextString
        if (byUser) isProjectHasChanges = true
    }

    fun setElementAlpha(mAlpha: Int, byUser: Boolean) {
        elementAlpha = mAlpha
        setTextColor(textColors.withAlpha(mAlpha * 255 / 100))
    }

    fun updateScaleSize() {
        maxWrap = maxWidth().toInt()
        layoutParams.height = height + Utils.dpToPx(65F).roundToInt()
        requestLayout()
        invalidate()
        if (iStickerOperation != null && enableBorder) {
            iStickerOperation!!.onEdit(mTag, true)
        }
    }

    fun updateScaleSize(updateGravity: Boolean) {
        if (updateGravity) {
            val largeWidth = getLineWiseText(lineCountForLargeWidth)
            previewsWidth = layoutParams.width.toFloat()
            layoutParams.width = (largeWidth).roundToInt()
            if (iStickerOperation != null && enableBorder) {
                iStickerOperation!!.onEdit(mTag, true)
            }
        }
    }

    fun changeCaseType(currentTextCaseIndex: TextCaseType, byUser: Boolean = false) {
        textCaseIndex = currentTextCaseIndex
        when (currentTextCaseIndex) {
            TextCaseType.NONE -> capsFirstWordOnly(text!!.toString(), byUser)
            TextCaseType.LOWER_CASE -> convertToLowerCase(text!!.toString(), byUser)
            TextCaseType.TITLE_CASE -> setText(toTitleCase(text!!.toString()), byUser)
            TextCaseType.UPPER_CASE -> convertToUpperCase(text!!.toString(), byUser)
            else -> {}
        }
    }

    fun convertToLowerCase(str: String, byUser: Boolean = false) {
        try {
            setText(str.lowercase(Locale.getDefault()), byUser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun convertToUpperCase(str: String, byUser: Boolean = false) {
        try {
            setText(str.uppercase(Locale.getDefault()), byUser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toTitleCase(str: String?): String? {
        if (str == null) {
            return null
        }
        var space = true
        val builder = StringBuilder(str)
        val len = builder.length
        for (i in 0 until len) {
            val c = builder[i]
            if (space) {
                if (!Character.isWhitespace(c)) {
                    builder.setCharAt(i, c.titlecaseChar())
                    space = false
                }
            } else if (Character.isWhitespace(c)) {
                space = true
            } else if (Character.isWhitespace(c) || c == '.' || c == '!' || c == '?') {
                space = true
            } else {
                builder.setCharAt(i, c.lowercaseChar())
            }
        }
        return builder.toString()
    }

    fun capsFirstWordOnly(str: String, byUser: Boolean = false) {
        var str = str
        try {
            str = str.lowercase(Locale.getDefault())
            str = str.substring(0, 1).uppercase(Locale.getDefault()) + str.substring(1)
            setText(str, byUser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface EventCallbackListener {
        fun onEventDetected(motionEvent: MotionEvent?)
        fun onNewElementDetected(motionEvent: MotionEvent?)
    }

    fun isMotionEventInsideView(event: MotionEvent): Boolean {
        val viewRect = Rect()
        getGlobalVisibleRect(viewRect)
        val leftRect = Rect()
        if (imageLeftWrap != null)
            imageLeftWrap!!.getGlobalVisibleRect(leftRect)
        val rightRect = Rect()
        if (imageRightWrap != null)
            imageRightWrap!!.getGlobalVisibleRect(rightRect)
        return viewRect.contains(
            event.rawX.toInt(),
            event.rawY.toInt()
        ) || leftRect.contains(
            event.rawX.toInt(),
            event.rawY.toInt()
        ) || rightRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }

    fun showHideBaseLine(isVerticalVisible: Int, isHorizontalVisible: Int) {
        if (mVViewBaseLine != null && isVerticalVisible != -1) mVViewBaseLine!!.visibility =
            isVerticalVisible
        if (mHViewBaseLine != null && isHorizontalVisible != -2) mHViewBaseLine!!.visibility =
            isHorizontalVisible
    }

    private fun setControlItemsHidden(isHidden: Boolean) {
        if (isHidden) {
            if (imageLeftWrap != null)
                imageLeftWrap!!.visibility = INVISIBLE
            if (imageRightWrap != null)
                imageRightWrap!!.visibility = INVISIBLE
        } else {
            if (imageLeftWrap != null)
                imageLeftWrap!!.visibility = VISIBLE
            if (imageRightWrap != null)
                imageRightWrap!!.visibility = VISIBLE
        }
    }

    fun getLineWiseText(): String {
        var largeWidth = 0f
        var tempLine = ""
        val lineCount: Int = lineCount
        for (line in 0 until lineCount) {

            val start = layout.getLineStart(line)
            val end = layout.getLineEnd(line)
            val substring = text!!.subSequence(start, end)
            val maxWidth = paint.measureText(substring, 0, substring.length)

            largeWidth = if (maxWidth > largeWidth) {
                tempLine = substring.toString()
                maxWidth
            } else {
                largeWidth
            }
        }
        return tempLine
    }

    fun getLineWiseText(substring: String = ""): Float {
        return paint.measureText(substring, 0, substring.length)
    }

    fun setWrapImages(leftWrap: ImageView?, rightWrap: ImageView) {
        imageLeftWrap = leftWrap
        imageRightWrap = rightWrap
        if (imageLeftWrap != null)
            imageLeftWrap!!.setOnTouchListener(scaleListener)
        if (imageRightWrap != null)
            imageRightWrap!!.setOnTouchListener(scaleRightListener)
    }

    fun updateViewScale(scale: Float) {
        scaleX = scale
        scaleY = scale
    }

    //TODO: Flip Horizontal/Vertical Text Content
    override fun onDraw(canvas: Canvas?) {
        if (canvas != null) {
            if (flipX == 1) {
                canvas.translate(width.toFloat(), 0F)
                canvas.scale(-1F, 1F)
            }
            if (flipY == 1) {
                canvas.translate(0F, height.toFloat())
                canvas.scale(1F, -1F)
            }
        }
        super.onDraw(canvas)
    }

    fun flipText(isHorizontal: Boolean = false, isVertical: Boolean = false) {
        try {
            if (isHorizontal) if (flipX == 1) flipX = 0 else flipX = 1
            if (isVertical) if (flipY == 1) flipY = 0 else flipY = 1
            invalidate()
            requestLayout()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun moveViewPositionBy(direction: Int) {
        try {
            val mLayoutParams = layoutParams as ViewGroup.MarginLayoutParams
            when (direction) {
                0 -> { //TODO: 0 -> LEFT
                    mLayoutParams.setMargins(
                        mLayoutParams.leftMargin - 6,
                        mLayoutParams.topMargin,
                        mLayoutParams.rightMargin + 6,
                        mLayoutParams.bottomMargin
                    )
                }
                1 -> { //TODO: 1 -> TOP
                    mLayoutParams.setMargins(
                        mLayoutParams.leftMargin,
                        mLayoutParams.topMargin - 6,
                        mLayoutParams.rightMargin,
                        mLayoutParams.bottomMargin + 6
                    )
                }
                2 -> { //TODO: 1 -> RIGHT
                    mLayoutParams.setMargins(
                        mLayoutParams.leftMargin + 6,
                        mLayoutParams.topMargin,
                        mLayoutParams.rightMargin - 6,
                        mLayoutParams.bottomMargin
                    )
                }
                3 -> { //TODO: 1 -> BOTTOM
                    mLayoutParams.setMargins(
                        mLayoutParams.leftMargin,
                        mLayoutParams.topMargin + 6,
                        mLayoutParams.rightMargin,
                        mLayoutParams.bottomMargin - 6
                    )
                }
            }
            layoutParams = mLayoutParams

            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyMask(maskBitmap: Bitmap, resourceName: String, byUser: Boolean = false) {
        try {
            colorName = ""
            val shader: Shader = BitmapShader(
                maskBitmap,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
            )
            isGradientEnabled = true
            paint.shader = shader
            maskImage = resourceName
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}