package com.hashone.module.textview.utils

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.customview.widget.ViewDragHelper
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.hashone.commonutils.utils.AnimUtils
import com.hashone.commonutils.utils.Constants
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.R
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.databinding.GoogleBottomSheetFontsBinding
import com.hashone.module.textview.databinding.PanelItemMainBinding
import com.hashone.module.textview.fragment.FontsFragment
import com.hashone.module.textview.interfaces.FontItemCallback
import com.hashone.module.textview.model.ContentData
import com.hashone.module.textview.model.DataResponse
import kotlin.math.roundToInt

class GoogleBottomSheet @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ViewGroup(context, attrs, defStyle) {

    private lateinit var mDragHelper: ViewDragHelper
    private var layoutParentEdit: View? = null
    private var layoutFontUIDrag: View? = null
    private var textViewFontsDrag: View? = null
    private var myGoogleBottomSheet: View? = null

    private var mHeaderView: View? = null
    private var imageViewClose: AppCompatImageView? = null
    private var fontBackground: View? = null

    private var title: FrameLayout? = null

    var elementFontCategoryTabs: TabLayout? = null
    private var fontsViewPager: ViewPager2? = null
    private var mInitialMotionX = 0f
    private var mInitialMotionY = 0f
    private var mDragRange = 0
    private var mTop = 1000
    private var mTopMargin = 300
    private var mDragOffset = 0f
    var mItemCallback: FontItemCallback? = null

    var selectedFontName: String = ""
    var selectedFontData: ContentData? = null

    var isTabsLayoutTouch: Boolean = false

    override fun onFinishInflate() {
        super.onFinishInflate()
        mTop = (Utils.getScreenHeight(context) * .7F).roundToInt() + Utils.dpToPx(56F).roundToInt()
        mTopMargin =
            (Utils.getScreenHeight(context) * .3F).roundToInt() + Utils.dpToPx(56F).roundToInt()

        layoutParentEdit = findViewById(R.id.layoutParentEdit)
        mHeaderView = findViewById(R.id.layoutFontHeader)
        imageViewClose = findViewById(R.id.imageViewClose)
        title = findViewById(R.id.title)
        elementFontCategoryTabs = findViewById(R.id.elementFontCategoryTabs)
        fontsViewPager = findViewById(R.id.fontsViewPager)
        layoutFontUIDrag = findViewById(R.id.layoutFontUIDrag)
        textViewFontsDrag = findViewById(R.id.textViewFontsDrag)
        myGoogleBottomSheet = findViewById(R.id.myGoogleBottomSheet)

        imageViewClose!!.setOnClickListener {
            AnimUtils.toggleSlide(false, this, this)
            AnimUtils.toggleSlide(false, this, fontBackground!!)
        }

        if (textViewFontsDrag != null) {
            if (!MyApplication.instance.storeUserData.getBoolean(Constants.KEY_IS_FONT_MESSAGE_SHOWN)) {
                textViewFontsDrag!!.isVisible = true
                layoutFontUIDrag!!.isVisible = false
                MyApplication.instance.storeUserData.setBoolean(Constants.KEY_IS_FONT_MESSAGE_SHOWN, true)
            } else {
                textViewFontsDrag!!.isVisible = false
                layoutFontUIDrag!!.isVisible = true
            }
        }

        setupFontCategories()
//
//        elementFontCategoryTabs!!.setOnTouchListener { view, motionEvent ->
//            when(motionEvent.actionMasked) {
//                MotionEvent.ACTION_DOWN -> {
//                    view.parent.requestDisallowInterceptTouchEvent(true);
//                }
//                MotionEvent.ACTION_UP -> {
//                    view.parent.requestDisallowInterceptTouchEvent(false);
//                }
//            }
//            return@setOnTouchListener false
//        }

        mDragHelper = ViewDragHelper.create(this, 1f, DragHelperCallback())
    }

    fun updateFontsList() {

    }

    fun smoothSlideTo(slideOffset: Float): Boolean {
        val topBound = paddingTop
        val y = (topBound + slideOffset * mDragRange).toInt()
        val isSmoothScroll = mDragHelper.smoothSlideViewTo(mHeaderView!!, mHeaderView!!.left, y)
        if (isSmoothScroll) {
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    override fun computeScroll() {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)
        if (action != MotionEvent.ACTION_DOWN) {
            mDragHelper.cancel()
            return super.onInterceptTouchEvent(ev)
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel()
            return false
        }
        val x = ev.x
        val y = ev.y
        var interceptTap = false
        var closeTap = false
        var isTabTapped = false
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mInitialMotionX = x
                mInitialMotionY = y
                interceptTap = mDragHelper.isViewUnder(mHeaderView, x.toInt(), y.toInt())
                closeTap = isViewHit(imageViewClose, x.toInt(), y.toInt())
                isTabTapped = isViewHit(elementFontCategoryTabs, x.toInt(), y.toInt())
            }

            MotionEvent.ACTION_MOVE -> {
                val adx = Math.abs(x - mInitialMotionX)
                val ady = Math.abs(y - mInitialMotionY)
                val slop = mDragHelper.touchSlop
                if (ady > slop && adx > ady) {
                    mDragHelper.cancel()
                    return false
                }
            }
        }
        val isIntercept =
            mDragHelper.shouldInterceptTouchEvent(ev) || (interceptTap && !closeTap)
        return isIntercept
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        mDragHelper.processTouchEvent(ev)
        val action = ev.action
        val x = ev.x
        val y = ev.y
        val isHeaderViewUnder = mDragHelper.isViewUnder(mHeaderView, x.toInt(), y.toInt())
        val closeTap = isViewHit(imageViewClose, x.toInt(), y.toInt())
        isTabsLayoutTouch = isViewHit(elementFontCategoryTabs, x.toInt(), y.toInt())

        when (action and MotionEventCompat.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mInitialMotionX = x
                mInitialMotionY = y

                if (textViewFontsDrag != null && textViewFontsDrag!!.isVisible)
                    textViewFontsDrag!!.isVisible = false
                if (layoutFontUIDrag != null && !layoutFontUIDrag!!.isVisible)
                    layoutFontUIDrag!!.isVisible = true
            }

            MotionEvent.ACTION_UP -> {
                val dx = x - mInitialMotionX
                val dy = y - mInitialMotionY
                val slop = mDragHelper.touchSlop
                if ((dx * dx + dy * dy < slop * slop) && isHeaderViewUnder && (!closeTap)) {
                    if (mDragOffset == 0f) {
                        smoothSlideTo(1f)
                    } else {
                        smoothSlideTo(0f)
                    }
                }
//
//                val actionIntent = Intent()
//                actionIntent.action = "UPDATE_VIEW_SIZE"
//                actionIntent.putExtra("padding", mTop)
//                context.sendBroadcast(actionIntent)
            }
        }

        return isHeaderViewUnder && (!closeTap) && isViewHit(
            mHeaderView,
            x.toInt(),
            y.toInt()
        ) ||
                isViewHit(fontsViewPager, x.toInt(), y.toInt()) || isViewHit(
            elementFontCategoryTabs,
            x.toInt(),
            y.toInt()
        )
    }

    private fun isViewHit(view: View?, x: Int, y: Int): Boolean {
        val viewLocation = IntArray(2)
        view!!.getLocationOnScreen(viewLocation)
        val parentLocation = IntArray(2)
        getLocationOnScreen(parentLocation)
        val screenX = parentLocation[0] + x
        val screenY = parentLocation[1] + y
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.width && screenY >= viewLocation[1] && screenY < viewLocation[1] + view.height
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
            resolveSizeAndState(maxHeight, heightMeasureSpec, 0)
        )
    }

    var extraHeight = 0

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mDragRange = height - mTop
        mTop = if (mDragRange < mTopMargin) (mTop - (mTopMargin - mDragRange)) else mTop
        mDragRange = if (mDragRange < mTopMargin) mTopMargin else mDragRange

        val newHeight = height - mTop
        val mLayoutParams = fontsViewPager!!.layoutParams as ViewGroup.LayoutParams
        mLayoutParams.height = newHeight
        fontsViewPager!!.layoutParams = mLayoutParams

        mHeaderView!!.layout(
            0,
            mTop,
            r,
            mTop + mHeaderView!!.measuredHeight
        )
        elementFontCategoryTabs!!.layout(
            0,
            mTop + mHeaderView!!.measuredHeight,
            r,
            mTop + mHeaderView!!.measuredHeight + elementFontCategoryTabs!!.measuredHeight
        )
        findViewById<View>(R.id.viewDivider2)!!.layout(
            0,
            mTop + mHeaderView!!.measuredHeight + elementFontCategoryTabs!!.measuredHeight,
            r,
            mTop + mHeaderView!!.measuredHeight + elementFontCategoryTabs!!.measuredHeight + findViewById<View>(
                R.id.viewDivider2
            )!!.measuredHeight
        )
        fontsViewPager!!.layout(
            0,
            mTop + mHeaderView!!.measuredHeight + elementFontCategoryTabs!!.measuredHeight + findViewById<View>(
                R.id.viewDivider2
            )!!.measuredHeight,
            r,
            b
        )

        extraHeight = mHeaderView!!.measuredHeight + elementFontCategoryTabs!!.measuredHeight
    }

    private inner class DragHelperCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child === mHeaderView
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            mTop = top
            mDragOffset = top.toFloat() / mDragRange
            requestLayout()
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            var top = paddingTop
            if (yvel > 0 || yvel == 0f && mDragOffset > 0.5f) {
                top += mDragRange
            }
            mDragHelper.settleCapturedViewAt(releasedChild.left, top)
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return mDragRange
        }

        override fun clampViewPositionVertical(
            child: View,
            top: Int,
            dy: Int
        ): Int {
            val topBound = paddingTop
            val bottomBound = height
            return Math.min(Math.max(top, topBound), bottomBound)
        }
    }

    fun setFontBackground(fontBackground: View) {
        this.fontBackground = fontBackground
    }

    fun selectTextFont(fontId: Int, fontName: String) {
        selectedFontName = fontName

        if (elementFontCategoryTabs != null && fragmentList.isNotEmpty()) {
            (fragmentList[elementFontCategoryTabs!!.selectedTabPosition] as FontsFragment).selectTextFont(selectedFontData, selectedFontName)
        }

        if (fontId != -1 || fontName.isNotEmpty()) {
            val fontDatas: DataResponse? =
                MyApplication.instance.resourcesResponseData.getDataResponse(Constants.RESPONSE_FONTS)
            if (fontDatas != null) {
                if (fontDatas.data.isNotEmpty()) {
                    fontDatas.data.forEach {
                        if (it.id == fontId || it.name.equals(fontName, ignoreCase = true)) {
                            selectedFontData = it
                            if (elementFontCategoryTabs != null && fragmentList.isNotEmpty()) {
                                (fragmentList[elementFontCategoryTabs!!.selectedTabPosition] as FontsFragment).selectTextFont(selectedFontData, selectedFontName)
                            }
                            return@forEach
                        }
                    }
                }
            }
        }
    }

    var projectsFonts = ArrayList<String>()
    fun filterProjectFonts(projectFonts: ArrayList<String>) {
        this.projectsFonts = projectFonts
        if (elementFontCategoryTabs != null) {
            if (elementFontCategoryTabs!!.tabCount > 0 && fragmentList.isNotEmpty()) {
                (fragmentList[elementFontCategoryTabs!!.selectedTabPosition] as FontsFragment).filterProjectFonts(
                    projectFonts
                )
            }
        }
    }

    private var fontCategoriesList: ArrayList<ContentData> = ArrayList()
    private var fontTabsAdapter: TopPagerAdapter? = null
    private fun setupFontCategories() {
        try {
            val fontCategoriesData: DataResponse? =
                MyApplication.instance.resourcesResponseData.getDataResponse(Constants.RESPONSE_FONT_CATEGORIES)
            if (fontCategoriesData != null) {
                fontCategoriesList = ArrayList()
                fontCategoriesList = fontCategoriesData.data

                fontCategoriesList.add(
                    0,
                    ContentData(name = MyApplication.instance.context!!.getString(R.string.label_all))
                )

                elementFontCategoryTabs!!.removeAllTabs()
                elementFontCategoryTabs!!.isSmoothScrollingEnabled = true

                fontCategoriesList.forEach {
                    elementFontCategoryTabs!!.addTab(
                        elementFontCategoryTabs!!.newTab().setText(it.name)
                    )
                }
                changeTabsFont(elementFontCategoryTabs!!)

                fontTabsAdapter = TopPagerAdapter(context as AppCompatActivity, fontCategoriesList)
                fontsViewPager!!.adapter = fontTabsAdapter
                fontsViewPager!!.offscreenPageLimit = fontCategoriesList.size
                fontsViewPager!!.isUserInputEnabled = false

                elementFontCategoryTabs!!.addOnTabSelectedListener(object :
                    TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        if (tab != null) {
                            if (tab.position < fragmentList.size) {
                                fontsViewPager!!.setCurrentItem(tab.position, false)
                                (fragmentList[tab.position] as FontsFragment).selectTextFont(selectedFontData, selectedFontName)
                            }
                        }
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {

                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {

                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var fragmentList = ArrayList<Fragment>()


    inner class TopPagerAdapter(activity: AppCompatActivity, tabList: ArrayList<ContentData>) :
        FragmentStateAdapter(activity) {
        val tabNameList = tabList

        override fun createFragment(position: Int): Fragment {
            val fragment = if (position == 0) {
                FontsFragment.newInstance(position, null)
            } else {
                FontsFragment.newInstance(position, tabNameList[position])
            }
            fragment.setFontUIView(myGoogleBottomSheet as GoogleBottomSheet)
            fragmentList.add(fragment)
            return fragment
        }

        override fun getItemCount(): Int {
            return tabNameList.size
        }

    }

    fun ViewPager2.getRecyclerView(): RecyclerView {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        return recyclerViewField.get(this) as RecyclerView
    }

    fun changeTabsFont(tabLayout: TabLayout) {
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabsCount = vg.childCount
        for (j in 0 until tabsCount) {
            val vgTab = vg.getChildAt(j) as ViewGroup
            val tabChildsCount = vgTab.childCount
            for (i in 0 until tabChildsCount) {
                val tabViewChild = vgTab.getChildAt(i)
                if (tabViewChild is TextView) {
                    tabViewChild.isAllCaps = false
                    tabViewChild.setTypeface(
                        ResourcesCompat.getFont(context, R.font.roboto_medium), Typeface.NORMAL
                    )
                    tabViewChild.textSize = Utils.dpToPx(14f)
                }
            }
        }
    }
}