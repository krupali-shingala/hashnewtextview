package com.hashone.module.textview.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.hashone.module.textview.retrofit.DownloadFile
import com.hashone.module.textview.retrofit.Resource
import com.hashone.module.textview.screenshot.ScreenshotUtils
import com.hashone.commonutils.enums.ContentType
import com.hashone.commonutils.enums.DownloadState
import com.hashone.commonutils.enums.ElementType
import com.hashone.commonutils.enums.PanelType
import com.hashone.commonutils.utils.*
import com.hashone.commonutils.utils.Constants.COLOR_ID_BLANK
import com.hashone.commonutils.utils.Constants.COLOR_ID_DROPPER
import com.hashone.commonutils.utils.Constants.KEY_CONTENT_DATA
import com.hashone.commonutils.utils.Constants.currentSelectedViewId
import com.hashone.commonutils.utils.Constants.currentSelectedViewType
import com.hashone.commonutils.utils.Constants.isProjectHasChanges
import com.hashone.module.textview.R
import com.hashone.module.textview.activities.EditActivity
import com.hashone.module.textview.adapters.BottomPanelAdapter
import com.hashone.module.textview.adapters.ElementPanelAdapter
import com.hashone.module.textview.base.BaseFragment
import com.hashone.module.textview.base.CoroutineAsyncTask
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.databinding.FragmentPanelContainerBinding
import com.hashone.module.textview.databinding.TabItemBinding
import com.hashone.module.textview.databinding.TabTitleItemBinding
import com.hashone.module.textview.interfaces.ItemCallback
import com.hashone.module.textview.model.ContentData
import com.hashone.module.textview.model.DataResponse
import com.hashone.module.textview.utils.AppFileUtils
import com.hashone.module.textview.utils.UrlUtils
import com.hashone.module.textview.viewmodel.FontViewModel
import com.hashone.module.textview.viewmodel.HomeViewModel
import com.hashone.module.textview.views.BackgroundImageView
import com.hashone.module.textview.views.pickerview.ColorPickerView
import com.hashone.module.textview.views.snappysmoothscroller.SnapType
import com.hashone.module.textview.views.snappysmoothscroller.SnappyLayoutManager
import com.hashone.module.textview.views.snappysmoothscroller.SnappyLinearLayoutManager
import com.hashone.textview.textviewnew.CustomTextView
import com.hashone.textview.textviewnew.TextCaseType
import kotlin.math.roundToInt

class PanelContainerFragment : BaseFragment(), DownloadFile.DownloadListener {

    private lateinit var binding: FragmentPanelContainerBinding

    lateinit var panelType: PanelType
    private var contentData: ContentData? = null

    private var editorView: ViewGroup? = null
    private var editParentWrapper: FrameLayout? = null
    private var colorPickerView: ColorPickerView? = null
    private var tempColorHexCode: String = ""

    private var projectName: String = ""
    private var projectFileName: String = ""
    private var fromMyProjects: Boolean = false

    lateinit var homeViewModel: HomeViewModel
    lateinit var fontViewModel: FontViewModel


    companion object {
        fun newInstance(
            panelType: PanelType,
            contentData: ContentData? = null
        ): PanelContainerFragment {
            val args = Bundle()
            args.putString("panelType", panelType.name)
            if (contentData != null)
                args.putSerializable(KEY_CONTENT_DATA, contentData)

            val fragment = PanelContainerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPanelContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        panelType = PanelType.valueOf(requireArguments().getString("panelType", ""))
        if (requireArguments().containsKey(KEY_CONTENT_DATA))
            contentData = requireArguments().getSerializable(KEY_CONTENT_DATA) as ContentData?

        homeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        fontViewModel = ViewModelProvider(requireActivity())[FontViewModel::class.java]

        when (panelType) {
            PanelType.TEXT_MASK -> {
                downloadFile = DownloadFile(
                    mActivity,
                    ContentType.TEXT_MASK,
                    FileUtils.getInternalBackgroundDir(mActivity).absolutePath
                )
                downloadFile!!.setDownloadListener(this)
                prepareBackgroundLayout()
            }

            PanelType.COLORS -> {
                prepareColorPanel()
            }

            PanelType.OPACITY -> {
                prepareOpacityLayout()
            }

            PanelType.SPACING -> {
                prepareSpacingLayout()
            }

            PanelType.FORMAT -> {
                prepareTextFormatLayout()
            }

            PanelType.ORDER -> {
                prepareNudgeOrderLayout(panelType)
            }

            PanelType.NUDGE -> {
                prepareNudgeOrderLayout(panelType)
            }

            PanelType.ROTATE -> {
                prepareRotateLayout()
            }

            PanelType.FONTS -> {
                downloadFile = DownloadFile(
                    mActivity,
                    ContentType.FONT,
                    FileUtils.getInternalFontDir(mActivity).absolutePath
                )
                downloadFile!!.setDownloadListener(this)
                prepareFontLayout()
            }

            else -> {

            }
        }

        if (colorPickerView != null)
            colorPickerView!!.visibility = View.GONE
    }

    fun setEditorView(
        selectedElementView: ViewGroup?,
        editParentWrapper: FrameLayout,
        colorPickerView: ColorPickerView
    ) {
        this.editorView = selectedElementView
        this.editParentWrapper = editParentWrapper
        this.colorPickerView = colorPickerView
    }

    fun setProjectDetails(projectName: String, projectFileName: String, fromMyProjects: Boolean) {
        this.projectName = projectName
        this.projectFileName = projectFileName
        this.fromMyProjects = fromMyProjects
    }

    //TODO: Colors
    private val colorsList = ArrayList<ContentData>()
    private var colorsAdapter: ElementPanelAdapter? = null


    fun setDropperColor(hexCode: String) {
        if (colorsAdapter != null)
            colorsAdapter!!.setDropperColorSelection(hexCode)
    }

    private fun prepareColorPanel() {
        val staticColors = resources.getIntArray(R.array.staticColors)

        colorsList.clear()

        if (panelType == PanelType.TEXT_MASK)
            colorsList.add(ContentData(id = COLOR_ID_BLANK, colorHexString = "#00000000"))
        if (panelType != PanelType.STICKER_SHADOW)
            colorsList.add(ContentData(id = COLOR_ID_DROPPER, colorHexCode = Color.BLUE))
        for (i in staticColors.indices) {
            colorsList.add(
                ContentData(
                    id = i,
                    colorHexString = String.format("#%06X", (staticColors[i])),
                    colorHexCode = staticColors[i]
                )
            )
        }

        val linearLayoutManager = SnappyLinearLayoutManager(
            mActivity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        (linearLayoutManager as SnappyLayoutManager).setSnapType(SnapType.CENTER)
        (linearLayoutManager as SnappyLayoutManager).setSnapDuration(240)
        (linearLayoutManager as SnappyLayoutManager).setSnapPaddingStart(
            Utils.getScreenWidth(mActivity) - (Utils.dpToPx(32F).roundToInt() / 2)
        )
        (linearLayoutManager as SnappyLayoutManager).setSnapPaddingEnd(
            Utils.getScreenWidth(mActivity) + (Utils.dpToPx(32F).roundToInt() / 2)
        )
        (linearLayoutManager as SnappyLayoutManager).setSnapInterpolator(
            DecelerateInterpolator()
        )
        binding.panelContainerList.layoutManager = linearLayoutManager

        val animator: RecyclerView.ItemAnimator? = binding.panelContainerList.itemAnimator
        if (animator != null && animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        binding.panelContainerList.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        colorsAdapter = ElementPanelAdapter(mActivity, colorsList, panelType)
        binding.panelContainerList.adapter = colorsAdapter

        colorsAdapter!!.mItemCallback = object : ItemCallback {
            override fun onCategoryClick(position: Int, contentData: ContentData) {

            }

            override fun onHeaderContentClick(position: Int, contentData: ContentData) {

            }

            override fun onItemClick(position: Int, contentData: ContentData) {
//                binding.panelContainerList.smoothScrollToPosition(position)
//                binding.panelContainerList.scrollToPosition(position)
                if (colorPickerView != null) {
                    colorPickerView!!.visibility = View.GONE
                    if (colorsAdapter != null)
                        colorsAdapter!!.setDropperColorSelection()
                }
                if (editorView != null) {

                    var elementView: View? = null
                    editorView!!.forEach {
                        if (it.id == currentSelectedViewId) {
                            elementView = it
                            return@forEach
                        }
                    }
                    when (contentData.id) {
                        COLOR_ID_BLANK -> {
                            when (elementView) {

                                is CustomTextView -> {
                                    (elementView as CustomTextView).applyColor(
                                        contentData.colorHexString,
                                        true
                                    )
                                }
                            }
                        }

                        COLOR_ID_DROPPER -> {
                            try {
                                SetPaletteToPickerViewTask().execute()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        else -> {
                            when (elementView) {

                                is CustomTextView -> {
                                    (elementView as CustomTextView).applyColor(
                                        contentData.colorHexString,
                                        true
                                    )
                                    updateColorSelection()
                                }
                            }
                        }
                    }
                }
            }
        }
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)

        updateColorSelection()
    }

    fun updateColorSelection() {
        if (editorView != null && colorsAdapter != null) {
            val elementView =
                editorView!!.children.filter { it.id == currentSelectedViewId }.singleOrNull()

            when (elementView) {

                is CustomTextView -> {
                    val colorString = (elementView as CustomTextView).colorName
                    val itemPosition = colorsAdapter!!.setColorSelection(colorString)
                    binding.panelContainerList.postDelayed({
                        binding.panelContainerList.scrollToPosition(if (itemPosition == -1) 0 else itemPosition)
                    }, 10L)
                }

                else -> {
                    binding.panelContainerList.postDelayed({
                        binding.panelContainerList.scrollToPosition(0)
                    }, 10L)
                }
            }
        }
    }

    private inner class SetPaletteToPickerViewTask : CoroutineAsyncTask<Void, Void, Bitmap>() {
        override fun onPreExecute() {
            super.onPreExecute()
            tempColorHexCode = ""
        }

        override fun doInBackground(vararg params: Void?): Bitmap {
            try {
                Thread.sleep(250L)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ScreenshotUtils.loadBitmapFromView2(editParentWrapper!!)
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            try {
                if (result != null) {
                    colorPickerView!!.visibility = View.VISIBLE
                    val drawable: Drawable = BitmapDrawable(resources, result)
                    colorPickerView!!.setPaletteDrawable(drawable)

                    colorPickerView!!.viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            colorPickerView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            colorPickerView!!.onTouchReceivedFunction(
                                MotionEvent.ACTION_UP,
                                colorPickerView!!.selectedPoint!!.x,
                                colorPickerView!!.selectedPoint!!.y
                            )
                        }
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //TODO: Dynamic Backgrounds
    private val backgroundCategoriesList = ArrayList<ContentData>()
    private fun prepareBackgroundLayout() {
        binding.elementSubCategoryTabs.isVisible = true
        binding.panelContainerList.setPadding(Utils.dpToPx(4F).roundToInt())

        prepareColorPanel()
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)
        prepareBackgroundCategoriesTabs()
        homeViewModel.getBackgroundCategories()
        homeViewModel.backgroundCategoriesResponse.value = null
        homeViewModel.backgroundCategoriesResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.data != null) {
                        prepareBackgroundCategoriesTabs()
                    }
                }

                is Resource.Error -> {
                }

                is Resource.Loading -> {
                }
                else -> {}
            }
        }

        // updateBackgroundCategorySelection()
    }

    private fun prepareBackgroundCategoriesTabs() {
        backgroundCategoriesList.clear()
        binding.elementSubCategoryTabs.removeAllTabs()
        binding.elementSubCategoryTabs.isSmoothScrollingEnabled = true

        //TODO: Prepare Background Sub-Categories Tabs
        val jsonResponse =
            MyApplication.instance.resourcesResponseData.getString(Constants.RESPONSE_BACKGROUND_CATEGORIES)!!
        if (jsonResponse.isNotEmpty()) {
            val backgroundsItem = Utils.getGson().fromJson(jsonResponse, DataResponse::class.java)
            backgroundCategoriesList.addAll(backgroundsItem.data)
        }
        val tabItemBinding = TabItemBinding.inflate(
            LayoutInflater.from(context),
            binding.panelContainerParent,
            true
        )
        val shapeRing = GradientDrawable()
        shapeRing.shape = GradientDrawable.OVAL
        shapeRing.setColor(Color.parseColor("#ff5a5f"))
        tabItemBinding.icon.setImageDrawable(shapeRing)
        tabItemBinding.text1.text = MyApplication.instance.context!!.getString(R.string.label_color)

        binding.elementSubCategoryTabs.addTab(
            binding.elementSubCategoryTabs.newTab().setCustomView(tabItemBinding.root)
        )
        if (backgroundCategoriesList.isNotEmpty()) {
            backgroundCategoriesList.forEach {
                val tabItemBinding = TabItemBinding.inflate(
                    LayoutInflater.from(context),
                    binding.panelContainerParent,
                    true
                )

                Glide.with(mActivity)
                    .load(UrlUtils.getBackgroundCategoryPreviewImage128px(it))
//                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(
                        RequestOptions().placeholder(R.drawable.ic_graphics_ph)
                            .circleCrop()
                            .override(128)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .dontAnimate()
                    )
                    .into(tabItemBinding.icon)
                tabItemBinding.text1.text = it.name

                binding.elementSubCategoryTabs.addTab(
                    binding.elementSubCategoryTabs.newTab().setCustomView(tabItemBinding.root)
                )
            }

            binding.elementSubCategoryTabs.tabGravity =
                if (backgroundCategoriesList.size > 1) TabLayout.GRAVITY_FILL else TabLayout.GRAVITY_START
            binding.elementSubCategoryTabs.tabMode =
                if (backgroundCategoriesList.size > 1) TabLayout.MODE_SCROLLABLE else TabLayout.MODE_AUTO

            binding.elementSubCategoryTabs.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (colorPickerView != null) {
                        colorPickerView!!.visibility = View.GONE
                        if (colorsAdapter != null)
                            colorsAdapter!!.setDropperColorSelection()
                    }
                    if (tab?.position == 0) {
                        prepareColorPanel()
                    } else {
                        if (tab!!.position < binding.elementSubCategoryTabs.tabCount)
                            prepareBackgroundContents(backgroundCategoriesList[tab.position])
                    }
//                    updateBackgroundCategorySelection()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            })
            backgroundCategoriesList.add(0, ContentData())

            updateBackgroundCategorySelection()
        }
    }

    //TODO: Dynamic Fonts
    private val fontCategoriesList = ArrayList<ContentData>()
    private fun prepareFontLayout() {
        binding.elementSubCategoryTabs.isVisible = true
        binding.panelContainerList.setPadding(Utils.dpToPx(4F).roundToInt())

        prepareColorPanel()
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)
        preparefontCategoriesTabs()
        fontViewModel.getFontCategories()
        fontViewModel.fontCategoriesResponse.value = null
        fontViewModel.fontCategoriesResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.data != null) {
                        preparefontCategoriesTabs()
                    }
                }

                is Resource.Error -> {
                }

                is Resource.Loading -> {
                }
                else -> {}
            }
        }

        // updateBackgroundCategorySelection()
    }

    private fun preparefontCategoriesTabs() {
        fontCategoriesList.clear()
        binding.elementSubCategoryTabs.removeAllTabs()
        binding.elementSubCategoryTabs.isSmoothScrollingEnabled = true

        //TODO: Prepare Background Sub-Categories Tabs
        val jsonResponse =
            MyApplication.instance.resourcesResponseData.getString(Constants.RESPONSE_FONT_CATEGORIES)!!
        if (jsonResponse.isNotEmpty()) {
            val fontItem = Utils.getGson().fromJson(jsonResponse, DataResponse::class.java)
            fontCategoriesList.addAll(fontItem.data)
        }
        val tabItemBinding = TabItemBinding.inflate(
            LayoutInflater.from(context),
            binding.panelContainerParent,
            true
        )
//        val shapeRing = GradientDrawable()
//        shapeRing.shape = GradientDrawable.OVAL
//        shapeRing.setColor(Color.parseColor("#ff5a5f"))
//        tabItemBinding.icon.setImageDrawable(shapeRing)
//        tabItemBinding.text1.text = MyApplication.instance.context!!.getString(R.string.label_color)
//
//        binding.elementSubCategoryTabs.addTab(
//            binding.elementSubCategoryTabs.newTab().setCustomView(tabItemBinding.root)
//        )

        if (fontCategoriesList.isNotEmpty()) {
            fontCategoriesList.forEach {
                val tabItemBinding = TabItemBinding.inflate(
                    LayoutInflater.from(context),
                    binding.panelContainerParent,
                    true
                )

                tabItemBinding.icon.isVisible = false
                tabItemBinding.text1.text = it.name

                binding.elementSubCategoryTabs.addTab(
                    binding.elementSubCategoryTabs.newTab().setCustomView(tabItemBinding.root)
                )
            }

            binding.elementSubCategoryTabs.tabGravity =
                if (fontCategoriesList.size > 1) TabLayout.GRAVITY_FILL else TabLayout.GRAVITY_START
            binding.elementSubCategoryTabs.tabMode =
                if (fontCategoriesList.size > 1) TabLayout.MODE_SCROLLABLE else TabLayout.MODE_AUTO

            binding.elementSubCategoryTabs.addOnTabSelectedListener(object :
                TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (colorPickerView != null) {
                        colorPickerView!!.visibility = View.GONE
                        if (colorsAdapter != null)
                            colorsAdapter!!.setDropperColorSelection()
                    }
                    if (tab?.position == 0) {
                        prepareColorPanel()
                    } else {
                        if (tab!!.position < binding.elementSubCategoryTabs.tabCount)
                            prepareFontContents(fontCategoriesList[tab.position])
                    }
//                    updateBackgroundCategorySelection()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            })
            fontCategoriesList.add(0, ContentData())

            updateFontCategorySelection()
        }
    }

    //TODO: Opacity
    private fun prepareOpacityLayout() {
        binding.opacitySeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.opacitySeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) {
                    if (editorView != null) {
                        val elementView =
                            editorView!!.children.filter { it.id == currentSelectedViewId }
                                .singleOrNull()
                        when (elementView) {
                            is BackgroundImageView -> {
                                (elementView!! as BackgroundImageView).setViewAlpha(progress)
                            }

                            is CustomTextView -> {
                                (elementView!! as CustomTextView).setElementAlpha(
                                    progress, true
                                )
                            }
                        }
                    }
                    binding.opacityValueText.text = "${progress}%"
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        updateOpacity()
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.opacityLayout)
    }

    fun updateOpacity() {
        if (editorView != null) {
            /*    val elementView = editorView!!.findViewById<View>(
                    Constants.currentSelectedViewId
                )*/
            var elementView: View? = null
            editorView!!.forEach {
                if (it.id == currentSelectedViewId) {
                    elementView = it
                    return@forEach
                }
            }
            when (elementView) {
                is BackgroundImageView -> {

                }

                is CustomTextView -> {
                    binding.opacitySeekbar.progress =
                        (elementView as CustomTextView).elementAlpha
                }
            }
            binding.opacityValueText.text = "${binding.opacitySeekbar.progress}%"
        }
    }

//    //TODO: Spacing
//    private fun prepareSpacingLayout() {
//        binding.lineSSeekbar.max = 100
//        binding.lineSSeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        binding.lineSSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
//                if (fromuser) {
//                    if (editorView != null) {
//                        var elementView: View? = null
//                        editorView!!.forEach {
//                            if (it.id == currentSelectedViewId) {
//                                elementView = it
//                                return@forEach
//                            }
//                        }
//                        when (elementView) {
//                            is CustomTextView -> {
//                                (elementView as CustomTextView).latterSpacing =
//                                    ((progress - 25) / 100F).toFloat()
//                                isProjectHasChanges = true
//                            }
//                        }
//                    }
//                }
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//
//            }
//        })
//        binding.lineHSeekbar.max = 400
//        binding.lineHSeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        binding.lineHSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
//                if (fromuser) {
//                    if (editorView != null) {
//                        var elementView: View? = null
//                        editorView!!.forEach {
//                            if (it.id == currentSelectedViewId) {
//                                elementView = it
//                                return@forEach
//                            }
//                        }
//                        when (elementView) {
//                            is CustomTextView -> {
//                                (elementView as CustomTextView).lineSpacing =
//                                    (progress + 10)
//                                isProjectHasChanges = true
//                            }
//                        }
//                    }
//                }
//            }
//
//            override fun onStartTrackingTouch(p0: SeekBar?) {
//
//            }
//
//            override fun onStopTrackingTouch(p0: SeekBar?) {
//            }
//        })
//        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.spacingLayout)
//        updateTextSpacing()
//    }
//
//    fun updateTextSpacing() {
//        if (editorView != null) {
//            editorView!!.forEach {
//                if (it.id == currentSelectedViewId) {
//                    binding.lineSSeekbar.progress =
//                        (((it as CustomTextView).latterSpacing * 100F) + 25).roundToInt()
//
//                    if (it.lineSpacing == 0) {
//                        val lineValue =
//                            it.paint.fontMetrics.bottom - it.paint.fontMetrics.top
//                        binding.lineHSeekbar.progress = (lineValue - 10).roundToInt()
//                    } else {
//                        binding.lineHSeekbar.progress = (it.lineSpacing - 10)
//                    }
//                    return@forEach
//                }
//            }
//        }
//    }

    //TODO: Spacing
    private fun prepareSpacingLayout() {
        binding.elementSubCategoryTabs.visibility = View.GONE
        binding.lineSSeekbar.max = 50
        binding.lineSSeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.lineSSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) {
                    if (editorView != null) {
                        var elementView: View? = null
                        editorView!!.forEach {
                            if (it.id == currentSelectedViewId) {
                                elementView = it
                                return@forEach
                            }
                        }
                        when (elementView) {
                            is CustomTextView -> {
                                (elementView as CustomTextView).applyLetterSpacing(((progress - 8) / 20F))
                                (elementView as CustomTextView).hideWrapView()

                                isProjectHasChanges = true
                                (elementView as CustomTextView).post {
                                    (elementView as CustomTextView).updateScaleSize()
                                }
                            }
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (editorView != null) {
                    var elementView: View? = null
                    editorView!!.forEach {
                        if (it.id == currentSelectedViewId) {
                            elementView = it
                            return@forEach
                        }
                    }
                    when (elementView) {
                        is CustomTextView -> {
                            (elementView as CustomTextView).updateScaleSize()
                            (elementView as CustomTextView).visibleAll()
                        }

                    }
                }
            }
        })
        binding.lineHSeekbar.max = 400
        binding.lineHSeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.lineHSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) {
                    if (editorView != null) {
                        var elementView: View? = null
                        editorView!!.forEach {
                            if (it.id == currentSelectedViewId) {
                                elementView = it
                                return@forEach
                            }
                        }
                        when (elementView) {
                            is CustomTextView -> {
                                (elementView as CustomTextView).applyLineSpacing((progress + 10).toFloat())
                                isProjectHasChanges = true
                                (elementView as CustomTextView).hideWrapView()
                            }
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (editorView != null) {
                    var elementView: View? = null
                    editorView!!.forEach {
                        if (it.id == currentSelectedViewId) {
                            elementView = it
                            return@forEach
                        }
                    }
                    when (elementView) {
                        is CustomTextView -> {
                            (elementView as CustomTextView).updateScaleSize()
                            (elementView as CustomTextView).visibleAll()
                        }

                    }
                }
            }
        })
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.spacingLayout)
        updateTextSpacing()
    }

    fun updateTextSpacing() {
        if (editorView != null) {
            editorView!!.forEach {
                if (it.id == currentSelectedViewId) {
                    if (it is CustomTextView) {
                        binding.lineSSeekbar.progress =
                            ((((it as CustomTextView).letterSpacing * 20) + 8)).roundToInt()
                        if (it.mLineSpacing == 0F) {
                            val lineValue =
                                it.paint.fontMetrics.bottom - it.paint.fontMetrics.top
                            binding.lineHSeekbar.progress = (lineValue - 10).roundToInt()
                        } else {
                            binding.lineHSeekbar.progress = ((it.mLineSpacing - 10).roundToInt())
                        }
                        it.largeWidth()
                    }
                    return@forEach
                }
            }
        }
    }

    //TODO: Text Format
    private val textFormatList = ArrayList<ContentData>()
    private var textFormatAdapter: BottomPanelAdapter? = null
    private fun prepareTextFormatLayout() {
        textFormatList.clear()
        textFormatList.add(
            ContentData(
                ratioImage = R.drawable.none_selector,
                id = Constants.TEXT_FORMAT_NONE,
                name = MyApplication.instance.context!!.getString(R.string.label_none),
                isSelected = false
            )
        )
        textFormatList.add(
            ContentData(
                ratioImage = R.drawable.text_cap_selector,
                id = Constants.TEXT_FORMAT_CAPS,
                name = MyApplication.instance.context!!.getString(R.string.label_caps),
                isSelected = false
            )
        )
        textFormatList.add(
            ContentData(
                ratioImage = R.drawable.text_lower_selector,
                id = Constants.TEXT_FORMAT_LOWER,
                name = MyApplication.instance.context!!.getString(R.string.label_lower),
                isSelected = false
            )
        )
        textFormatList.add(
            ContentData(
                ratioImage = R.drawable.text_format_selector,
                id = Constants.TEXT_FORMAT_TITLE,
                name = MyApplication.instance.context!!.getString(R.string.label_title),
                isSelected = false
            )
        )
        if (textFormatAdapter != null) {
            binding.panelContainerList.post {
                textFormatAdapter!!.notifyItemRangeInserted(0, textFormatList.size)
            }
        } else {
            val linearLayoutManager = SnappyLinearLayoutManager(
                mActivity, LinearLayoutManager.HORIZONTAL, false
            )
            (linearLayoutManager as SnappyLayoutManager).setSnapType(SnapType.CENTER)
            (linearLayoutManager as SnappyLayoutManager).setSnapDuration(240)
            (linearLayoutManager as SnappyLayoutManager).setSnapPaddingStart(
                Utils.getScreenWidth(mActivity) - (Utils.dpToPx(32F).roundToInt() / 2)
            )
            (linearLayoutManager as SnappyLayoutManager).setSnapPaddingEnd(
                Utils.getScreenWidth(mActivity) + (Utils.dpToPx(32F).roundToInt() / 2)
            )
            (linearLayoutManager as SnappyLayoutManager).setSnapInterpolator(
                DecelerateInterpolator()
            )
            binding.panelContainerList.layoutManager = linearLayoutManager

            val animator: RecyclerView.ItemAnimator? = binding.panelContainerList.itemAnimator
            if (animator != null && animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }

            binding.panelContainerList.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            textFormatAdapter =
                BottomPanelAdapter(
                    mActivity,
                    textFormatList,
                    binding.panelContainerList.paddingStart * 2
                )
            binding.panelContainerList.adapter = textFormatAdapter
        }
        textFormatAdapter?.mItemCallback = object : ItemCallback {
            override fun onItemClick(position: Int, contentData: ContentData) {
                binding.panelContainerList.post {
                    if (editorView != null) {
                        val elementView =
                            editorView!!.children.filter { it.id == currentSelectedViewId }
                                .singleOrNull()
                        when (elementView) {
                            is CustomTextView -> {
                                when (contentData.id) {
                                    Constants.TEXT_FORMAT_NONE -> {
                                        (elementView as CustomTextView).changeCaseType(
                                            TextCaseType.NONE, true
                                        )
                                    }

                                    Constants.TEXT_FORMAT_CAPS -> {
                                        (elementView as CustomTextView).changeCaseType(
                                            TextCaseType.UPPER_CASE, true
                                        )
                                    }

                                    Constants.TEXT_FORMAT_LOWER -> {
                                        (elementView as CustomTextView).changeCaseType(
                                            TextCaseType.LOWER_CASE, true
                                        )
                                    }

                                    Constants.TEXT_FORMAT_TITLE -> {
                                        (elementView as CustomTextView).changeCaseType(
                                            TextCaseType.TITLE_CASE, true
                                        )
                                    }
                                }

                                (mActivity as EditActivity).updateTextWidthHeight(elementView as CustomTextView)
                            }
                        }
                    }
                }
            }

            override fun onCategoryClick(position: Int, contentData: ContentData) {

            }

            override fun onHeaderContentClick(position: Int, contentData: ContentData) {

            }
        }
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)

        updateTextFormat()
    }

    fun updateTextFormat() {
        if (editorView != null && textFormatAdapter != null) {
            val elementView =
                editorView!!.children.filter { it.id == currentSelectedViewId }.singleOrNull()
            when (elementView) {
                is CustomTextView -> {
                    val itemPosition =
                        when ((elementView as CustomTextView).textCaseIndex) {
                            TextCaseType.NONE -> {
                                textFormatAdapter!!.setItemSelectionById(Constants.TEXT_FORMAT_NONE)
                            }

                            TextCaseType.UPPER_CASE -> {
                                textFormatAdapter!!.setItemSelectionById(Constants.TEXT_FORMAT_CAPS)
                            }

                            TextCaseType.LOWER_CASE -> {
                                textFormatAdapter!!.setItemSelectionById(Constants.TEXT_FORMAT_LOWER)
                            }

                            TextCaseType.TITLE_CASE -> {
                                textFormatAdapter!!.setItemSelectionById(Constants.TEXT_FORMAT_TITLE)
                            }

                            else -> {
                                textFormatAdapter!!.setItemSelectionById()
                            }
                        }
                    (mActivity as EditActivity).updateTextWidthHeight(elementView as CustomTextView)
                }
            }
        }
    }

    //TODO: Order
    private val orderList = ArrayList<ContentData>()
    private var orderAdapter: BottomPanelAdapter? = null
    private fun prepareNudgeOrderLayout(panelType: PanelType) {
        orderList.clear()

        when (panelType) {
            PanelType.ORDER -> {
                orderList.add(
                    ContentData(
                        ratioImage = R.drawable.front_order_selector,
                        id = Constants.ORDER_FRONT,
                        name = MyApplication.instance.context!!.getString(R.string.label_front),
                        isSelected = false,
                        enableSelectionMode = false,
                        orderEnabled = false
                    )
                )
                orderList.add(
                    ContentData(
                        ratioImage = R.drawable.back_order_selector,
                        id = Constants.ORDER_BACK,
                        name = MyApplication.instance.context!!.getString(R.string.label_back),
                        isSelected = false,
                        enableSelectionMode = false,
                        orderEnabled = false
                    )
                )
                orderList.add(
                    ContentData(
                        ratioImage = R.drawable.down_order_selector,
                        id = Constants.ORDER_DOWN,
                        name = MyApplication.instance.context!!.getString(R.string.label_down),
                        isSelected = false,
                        enableSelectionMode = false,
                        orderEnabled = false
                    )
                )
                orderList.add(
                    ContentData(
                        ratioImage = R.drawable.up_order_selector,
                        id = Constants.ORDER_UP,
                        name = MyApplication.instance.context!!.getString(R.string.label_up),
                        isSelected = false,
                        enableSelectionMode = false,
                        orderEnabled = false
                    )
                )
            }
            PanelType.NUDGE -> {
                orderList.add(
                    ContentData(
                        ratioImage = R.drawable.front_order_selector,
                        id = Constants.NUDGE_LEFT,
                        name = MyApplication.instance.context!!.getString(R.string.label_nudge_left),
                        isSelected = false,
                        enableSelectionMode = false,
                        orderEnabled = false
                    )
                )
                orderList.add(
                    ContentData(
                        ratioImage = R.drawable.back_order_selector,
                        id = Constants.NUDGE_RIGHT,
                        name = MyApplication.instance.context!!.getString(R.string.label_nudge_right),
                        isSelected = false,
                        enableSelectionMode = false,
                        orderEnabled = false
                    )
                )
                orderList.add(
                    ContentData(
                        ratioImage = R.drawable.down_order_selector,
                        id = Constants.NUDGE_UP,
                        name = MyApplication.instance.context!!.getString(R.string.label_nudge_up),
                        isSelected = false,
                        enableSelectionMode = false,
                        orderEnabled = false
                    )
                )
                orderList.add(
                    ContentData(
                        ratioImage = R.drawable.up_order_selector,
                        id = Constants.NUDGE_DOWN,
                        name = MyApplication.instance.context!!.getString(R.string.label_nudge_down),
                        isSelected = false,
                        enableSelectionMode = false,
                        orderEnabled = false
                    )
                )
            }
            else -> {}
        }
        if (orderAdapter != null) {
            binding.panelContainerList.post {
                orderAdapter!!.notifyItemRangeInserted(0, orderList.size)
            }
        } else {
            val linearLayoutManager = SnappyLinearLayoutManager(
                mActivity, LinearLayoutManager.HORIZONTAL, false
            )
            (linearLayoutManager as SnappyLayoutManager).setSnapType(SnapType.CENTER)
            (linearLayoutManager as SnappyLayoutManager).setSnapDuration(240)
            (linearLayoutManager as SnappyLayoutManager).setSnapPaddingStart(
                Utils.getScreenWidth(mActivity) - (Utils.dpToPx(32F).roundToInt() / 2)
            )
            (linearLayoutManager as SnappyLayoutManager).setSnapPaddingEnd(
                Utils.getScreenWidth(mActivity) + (Utils.dpToPx(32F).roundToInt() / 2)
            )
            (linearLayoutManager as SnappyLayoutManager).setSnapInterpolator(
                DecelerateInterpolator()
            )
            binding.panelContainerList.layoutManager = linearLayoutManager

            val animator: RecyclerView.ItemAnimator? = binding.panelContainerList.itemAnimator
            if (animator != null && animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }

            binding.panelContainerList.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

            orderAdapter =
                BottomPanelAdapter(
                    mActivity,
                    orderList,
                    binding.panelContainerList.paddingStart * 2
                )
            binding.panelContainerList.adapter = orderAdapter
        }
        orderAdapter?.mItemCallback = object : ItemCallback {
            override fun onItemClick(position: Int, contentData: ContentData) {
                binding.panelContainerList.post {
                    if (editorView != null) {
                        val elementView =
                            editorView!!.children.filter { it.id == currentSelectedViewId }
                                .singleOrNull()
                        if (elementView != null)
                            when (contentData.id) {
                                Constants.ORDER_FRONT -> {
                                    editorView!!.removeView(elementView)
                                    editorView!!.addView(elementView)
                                    isProjectHasChanges = true
                                }

                                Constants.ORDER_BACK -> {
                                    editorView!!.removeView(elementView)
                                    editorView!!.addView(elementView, 1)
                                    isProjectHasChanges = true
                                }

                                Constants.ORDER_DOWN -> {
                                    var position: Int = editorView!!.indexOfChild(elementView)
                                    if (position > 1) {
                                        editorView!!.removeView(elementView)
                                        position--
                                        //TODO: If Below Element is EmptyImageView
//                                    val emptyView = editorView!!.getChildAt(position)
//                                    if (emptyView is EmptyImageView) {
//                                        position--
//                                    }
                                        editorView!!.addView(elementView, position)
                                        isProjectHasChanges = true
                                    }
                                }

                                Constants.ORDER_UP -> {
                                    var position: Int = editorView!!.indexOfChild(elementView)
                                    if (editorView!!.childCount > 0) {
                                        if (position < editorView!!.childCount - 1) {
                                            editorView!!.removeView(elementView)
                                            position++
                                            //TODO: If Upper Element is EmptyImageView
//                                        val emptyView = editorView!!.getChildAt(position)
//                                        if (emptyView is EmptyImageView) {
//                                            position++
//                                        }
                                            editorView!!.addView(elementView, position)
                                            isProjectHasChanges = true
                                        }
                                    }
                                }

                                Constants.NUDGE_LEFT -> {
                                    (elementView as CustomTextView).moveViewPositionBy(0)
                                    updateSelection(elementView as CustomTextView)
                                }

                                Constants.NUDGE_RIGHT -> {
                                    (elementView as CustomTextView).moveViewPositionBy(2)
                                    updateSelection(elementView as CustomTextView)
                                }

                                Constants.NUDGE_UP -> {
                                    (elementView as CustomTextView).moveViewPositionBy(1)
                                    updateSelection(elementView as CustomTextView)
                                }

                                Constants.NUDGE_DOWN -> {
                                    (elementView as CustomTextView).moveViewPositionBy(3)
                                    updateSelection(elementView as CustomTextView)
                                }
                            }
                    }
                    if (panelType == PanelType.ORDER) updateOrderLayout()
                    if (panelType == PanelType.NUDGE) updateOrderLayout()
                }
            }

            override fun onCategoryClick(position: Int, contentData: ContentData) {

            }

            override fun onHeaderContentClick(position: Int, contentData: ContentData) {

            }
        }
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)

        if (panelType == PanelType.ORDER) updateOrderLayout()
    }

    private fun updateSelection(customTextView: CustomTextView) {
        try {
            customTextView.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        customTextView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        (mActivity as EditActivity).showSelectionUIForView(
                            customTextView,
                            isVisible = true
                        )
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateOrderLayout() {
        if (editorView != null && orderAdapter != null) {
            editorView!!.forEach {
                if (it.id == currentSelectedViewId) {
                    when (currentSelectedViewType) {
                        ElementType.TEXT -> {
                            val textView = (it as CustomTextView)
                            val position: Int = editorView!!.indexOfChild(textView)

                            orderList.forEach {
                                if (it.id == Constants.ORDER_BACK || it.id == Constants.ORDER_DOWN) {
                                    it.orderEnabled = position > 1
                                } else {
                                    it.orderEnabled = position < editorView!!.childCount - 1
                                }
                            }
                            if (orderAdapter != null)
                                orderAdapter!!.notifyDataSetChanged()
                        }

                        else -> {}
                    }
                    return@forEach
                }
            }
        }
    }

    //TODO: Rotate
    private fun prepareRotateLayout() {
        binding.rotateSeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.rotateSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) {
                    if (editorView != null) {
                        val elementView =
                            editorView!!.children.filter { it.id == currentSelectedViewId }
                                .singleOrNull()

                        when (elementView) {
                            is CustomTextView -> {
                                (elementView as CustomTextView).adjustAngle(progress.toFloat())
                                updateSelection(elementView as CustomTextView)
                            }
                        }
                    }
                    binding.rotateValueText.text = "$progress"
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        updateRotation()
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.rotateLayout)
    }

    fun updateRotation() {
        try {
            if (editorView != null) {
                val elementView =
                    editorView!!.children.filter { it.id == currentSelectedViewId }.singleOrNull()
                if (elementView != null)
                    when (elementView) {
                        is CustomTextView -> {
                            binding.rotateSeekbar.progress = (elementView as CustomTextView).rotation.roundToInt()
                        }
                    }
                binding.rotateValueText.text = "${binding.rotateSeekbar.progress}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    //TODO: Prepare Background Contents
    private val backgroundContentsList = ArrayList<ContentData>()
    private var backgroundContentAdapter: ElementPanelAdapter? = null
    private fun prepareBackgroundContents(backgroundCategoryData: ContentData) {
        binding.panelContainerList.setPadding(Utils.dpToPx(4F).roundToInt())
        homeViewModel.getBackgroundContents(backgroundCategoryData.id)
        homeViewModel.backgroundsResponse.value = null
        homeViewModel.backgroundsResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.data != null) {
                        setBackgroundsAdapter(backgroundCategoryData)
                    }
                }

                is Resource.Error -> {
                }

                is Resource.Loading -> {
                }
                else -> {}
            }
        }
        setBackgroundsAdapter(backgroundCategoryData)
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)
    }

    //TODO: Prepare Background Contents
    private val fontContentsList = ArrayList<ContentData>()
    private var fontContentAdapter: ElementPanelAdapter? = null
    private fun prepareFontContents(fontCategoryData: ContentData) {
        binding.panelContainerList.setPadding(Utils.dpToPx(4F).roundToInt())
        homeViewModel.getBackgroundContents(fontCategoryData.id)
        homeViewModel.backgroundsResponse.value = null
        homeViewModel.backgroundsResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.data != null) {
                        setBackgroundsAdapter(fontCategoryData)
                    }
                }

                is Resource.Error -> {
                }

                is Resource.Loading -> {
                }
                else -> {}
            }
        }
        setBackgroundsAdapter(fontCategoryData)
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)
    }

    private fun setBackgroundsAdapter(backgroundCategoryData: ContentData) {
        if (colorsList.isNotEmpty()) {
            colorsList.clear()
            colorsList.forEach {
                it.isSelected = false
            }
            if (colorsAdapter != null)
                colorsAdapter!!.notifyItemRangeChanged(0, colorsList.size)
        }
        backgroundContentsList.clear()
        val contentJsonResponse =
            MyApplication.instance.resourcesResponseData.getString("${Constants.RESPONSE_BACKGROUND_CONTENTS_BY_}${backgroundCategoryData.id}")!!
        if (contentJsonResponse.isNotEmpty()) {
            val contentsItem =
                Utils.getGson().fromJson(contentJsonResponse, DataResponse::class.java)
            backgroundContentsList.addAll(contentsItem.data)
        }

        val linearLayoutManager = SnappyLinearLayoutManager(
            mActivity, LinearLayoutManager.HORIZONTAL, false
        )
        (linearLayoutManager as SnappyLayoutManager).setSnapType(SnapType.CENTER)
        (linearLayoutManager as SnappyLayoutManager).setSnapDuration(240)
        (linearLayoutManager as SnappyLayoutManager).setSnapPaddingStart(
            Utils.getScreenWidth(mActivity) - (Utils.dpToPx(32F).roundToInt() / 2)
        )
        (linearLayoutManager as SnappyLayoutManager).setSnapPaddingEnd(
            Utils.getScreenWidth(mActivity) + (Utils.dpToPx(32F).roundToInt() / 2)
        )
        (linearLayoutManager as SnappyLayoutManager).setSnapInterpolator(
            DecelerateInterpolator()
        )
        binding.panelContainerList.layoutManager = linearLayoutManager

        val animator: RecyclerView.ItemAnimator? = binding.panelContainerList.itemAnimator
        if (animator != null && animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        binding.panelContainerList.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        backgroundContentAdapter =
            ElementPanelAdapter(mActivity, backgroundContentsList, PanelType.BACKGROUND_ELEMENTS)
        binding.panelContainerList.adapter = backgroundContentAdapter
        backgroundContentAdapter?.mItemCallback = object : ItemCallback {
            override fun onItemClick(position: Int, contentData: ContentData) {
                if (colorPickerView != null) {
                    colorPickerView!!.visibility = View.GONE
                    if (colorsAdapter != null)
                        colorsAdapter!!.setDropperColorSelection()
                }

                binding.panelContainerList.post {
                    val isPro = if (!MyApplication.instance.isPremiumVersion()) {
                        contentData.free == 0
                    } else {
                        false
                    }
                    if (isPro) {

                    } else {
                        if (AppFileUtils.isBackgroundFileExit(mActivity, contentData)) {
                            if (editorView != null) {
                                val elementView =
                                    editorView!!.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()
                                when (elementView) {
                                    is CustomTextView -> {
                                        ApplyBackground(
                                            (elementView as CustomTextView),
                                            contentData
                                        ).execute()
                                    }
                                }
                            }
                        } else {
                            if (Utils.isNetworkAvailable(mActivity)) {
                                if (downloadFile != null) {
                                    downloadFile!!.downloadFile(contentData)
                                }
                            } else {
                                Utils.showToast(
                                    mActivity,
                                    MyApplication.instance.getString(R.string.no_internet)
                                )
                            }
                        }
                    }
                }
            }

            override fun onCategoryClick(position: Int, contentData: ContentData) {

            }

            override fun onHeaderContentClick(position: Int, contentData: ContentData) {

            }
        }
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)

        updateBackgroundContentSelection()

        if (colorPickerView != null) {
            colorPickerView!!.visibility = View.GONE
            if (colorsAdapter != null)
                colorsAdapter!!.setDropperColorSelection()
        }
    }

    private inner class ApplyBackground(
        private val elementView: View,
        private val contentData: ContentData
    ) :
        CoroutineAsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void?): Bitmap? {
            val backgroundFile = AppFileUtils.getBackgroundFile(
                mActivity,
                contentData
            )
            return if (backgroundFile.exists()) {
                Glide.with(mActivity)
                    .asBitmap()
                    .load(backgroundFile)
                    .apply(
                        RequestOptions()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .override(editorView!!.height)
                    )
                    .submit().get()
            } else {
                val bitmap = Glide.with(mActivity)
                    .asBitmap()
                    .load(
                        UrlUtils.getBackgroundsPreviewImage(
                            contentData
                        )
                    )
                    .apply(
                        RequestOptions()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .override(editorView!!.height)
                    )
                    .submit().get()
                val fileName =
                    contentData.preview_image!!.webp.ifEmpty { contentData.preview_image!!.name }
                BitmapUtils.saveFileInLocation(
                    mActivity,
                    bitmap,
                    fileName,
                    FileUtils.getInternalBackgroundDir(mActivity)
                )
                bitmap
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if (result != null) {
                when (elementView) {
                    is CustomTextView -> {
                        (elementView as CustomTextView).resourceCategoryId =
                            contentData.primarybackgroundcategory_id
                        (elementView as CustomTextView).resourceId = contentData.id
                        (elementView as CustomTextView).resourceName = contentData.name
                        (elementView as CustomTextView).resourceUrl =
                            UrlUtils.getBackgroundsPreviewImage(contentData)
                        (elementView as CustomTextView).applyMask(
                            result,
                            contentData.name,
                            byUser = true
                        )
                        (elementView as CustomTextView).colorStringName = ""
                        (elementView as CustomTextView).img = contentData.name
                        (elementView as CustomTextView).isReset = true

                        (mActivity as EditActivity).updateBackgroundBorderUI((elementView as CustomTextView).colorStringName)
                    }
                }
                updateBackgroundContentSelection()
            }
        }
    }

    fun updateBackgroundCategorySelection() {
        if (editorView != null) {
            val elementView =
                editorView!!.children.filter { it.id == currentSelectedViewId }.singleOrNull()
            when (elementView) {
                is CustomTextView -> {
                    backgroundCategoriesList.forEachIndexed { index, contentData ->
                        if (contentData.id == (elementView as CustomTextView).resourceCategoryId) {
                            binding.elementSubCategoryTabs.selectTab(
                                binding.elementSubCategoryTabs.getTabAt(
                                    index
                                ), true
                            )
                            binding.elementSubCategoryTabs.postDelayed({
                                binding.elementSubCategoryTabs.setScrollPosition(
                                    index,
                                    0f,
                                    true
                                )
                            }, 20L)
                            return@forEachIndexed
                        }
                    }
                }
            }
        }
    }

    fun updateBackgroundContentSelection() {
        if (editorView != null) {
            var isListScrolled = false
            val elementView =
                editorView!!.children.filter { it.id == currentSelectedViewId }.singleOrNull()
            when (elementView) {
                is CustomTextView -> {
                    val index =
                        backgroundContentsList.indexOf(backgroundContentsList.filter { it.id == (elementView as CustomTextView).resourceId }
                            .singleOrNull())
                    if (index != -1) {
                        backgroundContentAdapter!!.setItemSelection(index)
                        isListScrolled = true
                    }
                }
            }
            if (!isListScrolled) {
            }
        }
    }

    fun updateFontCategorySelection() {
        if (editorView != null) {
            val elementView =
                editorView!!.children.filter { it.id == currentSelectedViewId }.singleOrNull()
            when (elementView) {
                is CustomTextView -> {
                    fontCategoriesList.forEachIndexed { index, contentData ->
                        if (contentData.id == (elementView as CustomTextView).resourceCategoryId) {
                            binding.elementSubCategoryTabs.selectTab(
                                binding.elementSubCategoryTabs.getTabAt(
                                    index
                                ), true
                            )
                            binding.elementSubCategoryTabs.postDelayed({
                                binding.elementSubCategoryTabs.setScrollPosition(
                                    index,
                                    0f,
                                    true
                                )
                            }, 20L)
                            return@forEachIndexed
                        }
                    }
                }
            }
        }
    }

    fun updateFontContentSelection() {
        if (editorView != null) {
            var isListScrolled = false
            val elementView =
                editorView!!.children.filter { it.id == currentSelectedViewId }.singleOrNull()
            when (elementView) {
                is CustomTextView -> {
                    val index =
                        backgroundContentsList.indexOf(backgroundContentsList.filter { it.id == (elementView as CustomTextView).resourceId }
                            .singleOrNull())
                    if (index != -1) {
                        backgroundContentAdapter!!.setItemSelection(index)
                        isListScrolled = true
                    }
                }
            }
            if (!isListScrolled) {
            }
        }
    }

    override fun downloadProgress(
        contentData: ContentData?,
        downloadState: DownloadState,
        progressValue: Int
    ) {
        when (downloadState) {
            DownloadState.NONE -> {

            }

            DownloadState.DOWNLOAD_STARTED -> {
                prepareDownloadingProgress("")
                showDownloadProgress(false)
            }

            DownloadState.DOWNLOAD_PROGRESS -> {
                updateDownloadProgressValue(progressValue)
            }

            DownloadState.DOWNLOAD_COMPLETED -> {
                dismissDownloadProgress()
                if (contentData != null) {
                    if (editorView != null) {
                        val elementView =
                            editorView!!.children.filter { it.id == currentSelectedViewId }
                                .singleOrNull()
                        when (currentSelectedViewType) {

                            ElementType.TEXT -> {
                                ApplyBackground(
                                    (elementView as CustomTextView),
                                    contentData
                                ).execute()
                            }

                            else -> {}
                        }
                    }
                }
            }

            DownloadState.DOWNLOAD_CANCELLED -> {
                if (contentData != null) {
                    when (currentSelectedViewType) {
                        else -> {}
                    }
                }
                dismissDownloadProgress()
            }

            DownloadState.ERROR -> {
                if (contentData != null) {
                    when (currentSelectedViewType) {

                        else -> {}
                    }
                }
                downloadFile?.cancelDownload()
                dismissDownloadProgress()
            }

            else -> {}
        }
    }

    private fun prepareStickerShadowLayout() {
        binding.elementSubCategoryTabs.isVisible = true

        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.panelContainerList)

        binding.elementSubCategoryTabs.removeAllTabs()
        binding.elementSubCategoryTabs.isSmoothScrollingEnabled = true

        addTab(
            binding.elementSubCategoryTabs,
            MyApplication.instance.getString(R.string.label_none)
        )
        addTab(
            binding.elementSubCategoryTabs,
            MyApplication.instance.getString(R.string.label_color)
        )
        addTab(
            binding.elementSubCategoryTabs,
            MyApplication.instance.getString(R.string.label_opacity)
        )
        addTab(
            binding.elementSubCategoryTabs,
            MyApplication.instance.getString(R.string.label_distance)
        )
        addTab(
            binding.elementSubCategoryTabs,
            MyApplication.instance.getString(R.string.label_blur)
        )

        binding.elementSubCategoryTabs.tabGravity = TabLayout.GRAVITY_FILL
        binding.elementSubCategoryTabs.tabMode = TabLayout.MODE_AUTO

        binding.elementSubCategoryTabs.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (colorPickerView != null) {
                    colorPickerView!!.visibility = View.GONE
                    if (colorsAdapter != null)
                        colorsAdapter!!.setDropperColorSelection()
                }
                when (tab?.position) {
                    0 -> {
                        if (editorView != null) {
                            editorView!!.forEach {
                                if (it.id == currentSelectedViewId) {

                                    if (currentSelectedViewType == ElementType.STICKER_PHOTO
                                        || currentSelectedViewType == ElementType.STICKER
                                    ) {
//                                        val stickerView = it as StickerView
//                                        stickerView.removeShadow()
//                                        (mActivity as EditActivity).closeElementPanel(stickerView.elementType)
//                                        if ((mActivity as EditActivity).supportFragmentManager.backStackEntryCount > 0) {
//                                            (mActivity as EditActivity).supportFragmentManager.popBackStack()
//                                        }
                                        return@forEach
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowOpacityLayout
                        )
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowBlurLayout
                        )
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowDistanceLayout
                        )
                        prepareColorPanel()
                    }

                    2 -> {
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.panelContainerList
                        )
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowBlurLayout
                        )
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowDistanceLayout
                        )
                        prepareShadowOpacityLayout()
                    }

                    3 -> {
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.panelContainerList
                        )
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowOpacityLayout
                        )
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowBlurLayout
                        )
                        prepareShadowDistanceLayout()
                    }

                    4 -> {
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.panelContainerList
                        )
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowOpacityLayout
                        )
                        AnimUtils.toggleFade(
                            false,
                            binding.panelContainerParent,
                            binding.stickerShadowDistanceLayout
                        )
                        prepareShadowBlurLayout()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })

        binding.elementSubCategoryTabs.getTabAt(1)!!.select()
        prepareColorPanel()
        updateStickerShadowPanel()
    }

    fun updateStickerShadowPanel(selectNone: Boolean = false) {
        if (editorView != null) {
            editorView!!.forEach {
                if (it.id == currentSelectedViewId) {
                    if (currentSelectedViewType == ElementType.STICKER_PHOTO
                        || currentSelectedViewType == ElementType.STICKER
                    ) {
//                        val stickerView = it as StickerView
//                        if (stickerView.isStickerShadow == 1) {
//                            if (colorsAdapter != null) {
//                                val itemPosition =
//                                    colorsAdapter!!.setColorSelection(stickerView.shadowColor)
//                                binding.panelContainerList.postDelayed({
//                                    binding.panelContainerList.scrollToPosition(if (itemPosition == -1) 0 else itemPosition)
//                                }, 10L)
//                            }
//                        } else {
//                            if (selectNone) {
//                                (mActivity as EditActivity).closeElementPanel(stickerView.elementType)
//                                if ((mActivity as EditActivity).supportFragmentManager.backStackEntryCount > 0) {
//                                    (mActivity as EditActivity).supportFragmentManager.popBackStack()
//                                }
//                            } else {
//                                binding.elementSubCategoryTabs.getTabAt(1)!!.select()
//                                stickerView.addDefaultShadow()
//                                prepareColorPanel()
//                            }
//                        }
                    }
                }
            }
        }
    }

    private fun addTab(tabLayout: TabLayout, title: String) {
        val tabItemBinding = TabTitleItemBinding.inflate(
            LayoutInflater.from(context),
            binding.panelContainerParent,
            true
        )
        tabItemBinding.text1.text = title

        tabLayout.addTab(
            tabLayout.newTab().setCustomView(tabItemBinding.root)
        )
    }

    private fun prepareShadowOpacityLayout() {
        binding.stickerShadowOpacitySeekbar.max = 100
        binding.stickerShadowOpacitySeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.stickerShadowOpacitySeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) {
                    if (editorView != null) {
                        var elementView: View? = null
                        editorView!!.forEach {
                            if (it.id == currentSelectedViewId) {
                                elementView = it
                                return@forEach
                            }
                        }
//                        if (elementView is StickerView && (elementView as StickerView).elementType != ElementType.BACKGROUND) {
//                            (elementView as StickerView).shadowOpacity = progress
//                            (elementView as StickerView).invalidate()
//                        }
                    }
                    binding.stickerShadowOpacityValueText.text = "${progress}%"
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        updateShadowOpacity()
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.stickerShadowOpacityLayout)
    }

    fun updateShadowOpacity() {
        if (editorView != null) {
            var elementView: View? = null
            editorView!!.forEach {
                if (it.id == currentSelectedViewId) {
                    elementView = it
                    return@forEach
                }
            }
            when (elementView) {
                is CustomTextView -> {
//                    binding.stickerShadowOpacitySeekbar.progress =
//                        (elementView as CustomTextView).shadowOpacity
                    binding.stickerShadowOpacityValueText.text =
                        "${binding.stickerShadowOpacitySeekbar.progress}%"
                }
            }
        }
    }

    private fun prepareShadowDistanceLayout() {
        var maxLeftRight = 1080
        var maxTopBottom = 1080
        if (editorView != null) {
            var elementView: View? = null
            editorView!!.forEach {
                if (it.id == currentSelectedViewId) {
                    elementView = it
                    return@forEach
                }
            }
            if (elementView != null) {
//                maxLeftRight = (elementView as StickerView).mBitmap!!.width
//                maxTopBottom = (elementView as StickerView).mBitmap!!.height
            }
        }
        binding.stickerShadowDistanceLeftRightSeekbar.max = maxLeftRight * 2
        binding.stickerShadowDistanceLeftRightSeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.stickerShadowDistanceLeftRightSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) {
                    if (editorView != null) {
                        var elementView: View? = null
                        editorView!!.forEach {
                            if (it.id == currentSelectedViewId) {
                                elementView = it
                                return@forEach
                            }
                        }
//                        if (elementView is StickerView && (elementView as StickerView).elementType != ElementType.BACKGROUND) {
//                            (elementView as StickerView).updateShadowDX((progress - (maxLeftRight)).toFloat())
//                        }
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
        binding.stickerShadowDistanceUpDownSeekbar.max = maxTopBottom * 2
        binding.stickerShadowDistanceUpDownSeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.stickerShadowDistanceUpDownSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) {
                    if (editorView != null) {
                        var elementView: View? = null
                        editorView!!.forEach {
                            if (it.id == currentSelectedViewId) {
                                elementView = it
                                return@forEach
                            }
                        }
//                        if (elementView is StickerView && (elementView as StickerView).elementType != ElementType.BACKGROUND) {
//                            (elementView as StickerView).updateShadowDY((progress - (maxTopBottom)).toFloat())
//                        }
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        updateShadowDistance()
        AnimUtils.toggleFade(
            true,
            binding.panelContainerParent,
            binding.stickerShadowDistanceLayout
        )
    }

    fun updateShadowDistance() {
        if (editorView != null) {
            var elementView: View? = null
            editorView!!.forEach {
                if (it.id == currentSelectedViewId) {
                    elementView = it
                    return@forEach
                }
            }
            when (elementView) {
                is CustomTextView -> {
//                    val maxLeftRight = (elementView as StickerView).mBitmap!!.width
//                    val maxTopBottom = (elementView as StickerView).mBitmap!!.height
//
//                    binding.stickerShadowDistanceLeftRightSeekbar.max = maxLeftRight * 2
//                    binding.stickerShadowDistanceUpDownSeekbar.max = maxTopBottom * 2
//
//                    binding.stickerShadowDistanceLeftRightSeekbar.progress =
//                        ((elementView as StickerView).shadowDistanceX + maxLeftRight).toInt()
//                    binding.stickerShadowDistanceUpDownSeekbar.progress =
//                        ((elementView as StickerView).shadowDistanceY + maxTopBottom).toInt()
                }
            }
        }
    }

    private fun prepareShadowBlurLayout() {
        binding.stickerShadowBlurSeekbar.max = 200
        binding.stickerShadowBlurSeekbar.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        binding.stickerShadowBlurSeekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromuser: Boolean) {
                if (fromuser) {
                    if (editorView != null) {
                        var elementView: View? = null
                        editorView!!.forEach {
                            if (it.id == currentSelectedViewId) {
                                elementView = it
                                return@forEach
                            }
                        }
//                        if (elementView is StickerView && (elementView as StickerView).elementType != ElementType.BACKGROUND) {
//                            if (progress > 1) {
//                                (elementView as StickerView).shadowBlurRadius = progress.toFloat()
//                                (elementView as StickerView).updateShadow(update = true)
//                            }
//                        }
                    }
                    binding.stickerShadowBlurValueText.text = "${progress / 2}%"
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    if (editorView != null) {
                        var elementView = editorView!!.children.filter { it.id == currentSelectedViewId }.singleOrNull()
//                        if (elementView is StickerView && (elementView as StickerView).elementType != ElementType.BACKGROUND) {
//                            if (seekBar.progress > 1) {
//                                (elementView as StickerView).shadowBlurRadius =
//                                    seekBar.progress.toFloat()
//                                (elementView as StickerView).updateShadow(update = true)
//                            }
//                        }
                    }
                    binding.stickerShadowBlurValueText.text = "${seekBar.progress / 2}%"
                }
            }
        })
        AnimUtils.toggleFade(true, binding.panelContainerParent, binding.stickerShadowBlurLayout)
    }

    override fun onDestroyView() {
        binding.elementSubCategoryTabs.isVisible = false
        super.onDestroyView()
    }
}