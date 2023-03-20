package com.hashone.module.textview.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.core.view.*
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hashone.commonutils.enums.ElementPositionData
import com.hashone.commonutils.enums.ElementType
import com.hashone.commonutils.enums.PanelType
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_ADD
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_ALIGN
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_DELETE
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_DUPLICATE
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_EDIT
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_FONT
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_FORMAT
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_OPACITY
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_ORDER
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_SPACING
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_COPY_STYLE
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_FILL_MASK
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_FLIPH
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_FLIPV
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_NUDGE
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_PASTE_STYLE
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_ROTATE
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_SHADOW
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_SIZE_MINUS
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_SIZE_PLUS
import com.hashone.commonutils.utils.Constants.currentSelectedViewId
import com.hashone.commonutils.utils.Constants.currentSelectedViewType
import com.hashone.commonutils.utils.Constants.elementPositionList
import com.hashone.commonutils.utils.Constants.isProjectHasChanges
import com.hashone.module.textview.R
import com.hashone.module.textview.databinding.ActivityEditBinding
import com.hashone.module.textview.adapters.BottomPanelAdapter
import com.hashone.module.textview.base.BaseActivity
import com.hashone.module.textview.base.BetterActivityResult
import com.hashone.module.textview.base.CoroutineAsyncTask
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.databinding.MergeToolbarIconBinding
import com.hashone.commonutils.extensions.doOnGlobalLayout
import com.hashone.commonutils.utils.*
import com.hashone.commonutils.utils.Constants.EXTENSION_JPG
import com.hashone.commonutils.utils.Constants.EXTENSION_PNG
import com.hashone.commonutils.utils.Constants.MAIN_ID_TEXT
import com.hashone.commonutils.utils.Constants.PREVIEW_NAME
import com.hashone.commonutils.views.BackgroundFrameLayout
import com.hashone.module.textview.databinding.GoogleBottomSheetFontsBinding
import com.hashone.module.textview.fragment.PanelContainerFragment
import com.hashone.module.textview.interfaces.FontItemCallback
import com.hashone.module.textview.interfaces.ItemCallback
import com.hashone.module.textview.model.ElementData
import com.hashone.module.textview.model.ContentData
import com.hashone.module.textview.model.ResourceData
import com.hashone.module.textview.model.TemplateData
import com.hashone.module.textview.screenshot.ScreenshotUtils
import com.hashone.module.textview.utils.AppConstants
import com.hashone.module.textview.utils.AppFileUtils
import com.hashone.module.textview.utils.UrlUtils
import com.hashone.module.textview.viewmodel.FontViewModel
import com.hashone.module.textview.views.BackgroundImageView
import com.hashone.module.textview.views.pickerview.ColorEnvelope
import com.hashone.module.textview.views.pickerview.flag.BubbleFlag
import com.hashone.module.textview.views.pickerview.flag.FlagMode
import com.hashone.module.textview.views.pickerview.listeners.ColorEnvelopeListener
import com.hashone.module.textview.views.snappysmoothscroller.SnapType
import com.hashone.module.textview.views.snappysmoothscroller.SnappyLayoutManager
import com.hashone.module.textview.views.snappysmoothscroller.SnappyLinearLayoutManager
import com.hashone.textview.textviewnew.CustomTextView
import com.hashone.textview.textviewnew.IStickerOperation
import com.hashone.textview.textviewnew.TextCaseType
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

class EditActivity : BaseActivity(), CoroutineScope {
    private lateinit var binding: ActivityEditBinding
    private lateinit var toolBarBinding: MergeToolbarIconBinding
    private lateinit var fontsBinding: GoogleBottomSheetFontsBinding

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val mainBottomPanelList = ArrayList<ContentData>()
    private var mainBottomPanelAdapter: BottomPanelAdapter? = null
    private val elementBottomPanelList = ArrayList<ContentData>()
    private var elementBottomPanelAdapter: BottomPanelAdapter? = null

    private var atomicInteger: AtomicInteger = AtomicInteger(0)
    private var fromMyProjects: Boolean = false// open template in re-edit

    private var projectId: Int = -1
    private var projectName: String = ""
    private var projectFileName: String = ""
    private var prevProjectSaveDir: File? = null
    private var projectSaveDir: File? = null
    private var tempProjectDir: File? = null
    private var isBlankCanvas: Int = 0

    private var editorWidth: Int = 0
    private var editorHeight: Int = 0
    private var templateWidth: Int = 0
    private var templateHeight: Int = 0

    private var templateData: TemplateData? = null

    lateinit var fontViewModel: FontViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()
        mActivity = this

        toolBarBinding = MergeToolbarIconBinding.bind(binding.root)
        setSupportActionBar(toolBarBinding.toolBar)
        toolBarBinding.toolBar.navigationContentDescription =
            MyApplication.instance.context!!.getString(R.string.label_back)
        supportActionBar!!.title = ""
        supportActionBar!!.subtitle = ""
        toolBarBinding.textViewToolBarTitle.text = ""

        isProjectRenderInProgress = true

        fontsBinding = GoogleBottomSheetFontsBinding.bind(binding.root)

        doSeparateTask()

        tempProjectDir = FileUtils.getInternalTempDir(mActivity)
        FileUtils.deleteDirectory(tempProjectDir!!, isParentDelete = false)

        clickEvents()

        fontViewModel = ViewModelProvider(mActivity as EditActivity)[FontViewModel::class.java]

        binding.layoutParentEdit.post {
            setupTemplateUI()
        }
        prepareMainBottomPanel()
    }

    private fun doSeparateTask() {
        launch(Dispatchers.IO) {
            FileUtils.deleteDirectory(FileUtils.getInternalTempDir(mActivity),
                    isParentDelete = false)
            FileUtils.deleteDirectory(FileUtils.getInternalDeletedDir(mActivity),
                    isParentDelete = false)
            FileUtils.copyFilesToInternal(mActivity)

            if (Utils.isNetworkAvailable(mActivity)) {
                fontViewModel.getFontCategories()
                fontViewModel.getFonts()
            }
        }
    }

    override fun clickEvents() {
        super.clickEvents()
        binding.templatePreview.setOnClickListener {
            if (Utils.checkClickTime600()) {
            }
        }
        binding.closeElementPanelImage.setOnClickListener {
            if (Utils.checkClickTime600()) {
                //TODO: De-Select View and Close related Panel
                disableSelectedElements()
                closeElementPanel()

                //TODO: Update Selection View
                currentSelectedViewType = ElementType.NONE
                currentSelectedViewId = -1
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        toolBarBinding.toolBar.inflateMenu(R.menu.menu_edit)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (!isProjectRenderInProgress) onBackPressed()
            }

            R.id.action_save -> {
                if (!isProjectRenderInProgress) {
                    if (Utils.checkClickTime400()) {
                        if (fontsBinding.myGoogleBottomSheet.isVisible) {
                            AnimUtils.toggleSlide(false,
                                    binding.layoutParentEdit,
                                    fontsBinding.myGoogleBottomSheet)
                            AnimUtils.toggleSlide(false,
                                    binding.layoutParentEdit,
                                    binding.fontBackground)
                        } else {
                            SaveProject(EXTENSION_PNG,
                                    isReplace = false,
                                    externalPreview = true).execute()
                        }
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class SaveProject(private val selectedSavedType: String,
                                    private val isReplace: Boolean,
                                    private val externalPreview: Boolean = true) :
            CoroutineAsyncTask<Void, Void, File>() {

        override fun onPreExecute() {
            super.onPreExecute()
            prepareProgressDialog(MyApplication.instance.context!!.getString(R.string.label_saving))
            showProgress(false)

            //TODO: Disable All Elements and Panel
            disableSelectedElements()
            closeElementPanel()

            //TODO: Update Element selection
            currentSelectedViewType = ElementType.NONE
            currentSelectedViewId = -1
        }

        override fun doInBackground(vararg params: Void?): File? {
            try {
                Thread.sleep(550L)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val extension = selectedSavedType

            val projectBitmap = ScreenshotUtils.loadBitmapFromView(binding.editParentLayer,
                    templateWidth,
                    templateHeight,
                    extension)

            projectSaveDir = if (isReplace) {
                FileUtils.deleteDirectory(prevProjectSaveDir!!, isParentDelete = false)
                prevProjectSaveDir
            } else {
                AppFileUtils.createSavedProjectDir(mActivity, projectName, projectFileName)
            }

            val jpgFile = File(projectSaveDir!!, "$PREVIEW_NAME.$EXTENSION_PNG")
            val pngFile = File(projectSaveDir!!, "$PREVIEW_NAME.$EXTENSION_JPG")

            if (jpgFile.exists()) jpgFile.delete()

            if (pngFile.exists()) pngFile.delete()

            if (externalPreview) {
                ScreenshotUtils.storeInInternal(mActivity,
                        projectBitmap!!,
                        "$PREVIEW_NAME.$extension",
                        projectSaveDir!!,
                        extension)
                return ScreenshotUtils.saveProject(mActivity,
                        projectBitmap,
                        "${projectSaveDir!!.nameWithoutExtension}.$extension",
                        ScreenshotUtils.getMainDirectoryName(mActivity),
                        extension)
            } else {
                return ScreenshotUtils.storeInInternal(mActivity,
                        projectBitmap!!,
                        "$PREVIEW_NAME.$extension",
                        projectSaveDir!!,
                        extension)
            }
        }

        override fun onPostExecute(result: File?) {
            super.onPostExecute(result)
            if (result != null) {
                prepareProjectJson(projectSaveDir)

                val externalPreviewPath =
                    "${ScreenshotUtils.getMainDirectoryName(mActivity)}/${projectSaveDir!!.nameWithoutExtension}.$selectedSavedType"

                MediaScannerConnection.scanFile(mActivity,
                        arrayOf(externalPreviewPath),
                        null) { path, uri ->
                }

                prevProjectSaveDir = projectSaveDir
                FileUtils.deleteDirectory(tempProjectDir!!, isParentDelete = false)
                projectSaveDir!!.listFiles()?.forEach {
                    FileUtils.moveFile(it.absolutePath, tempProjectDir!!.absolutePath)
                }

                val intent3 = Intent()
                intent3.action = AppConstants.ACTION_UPDATE_SAVED
                sendBroadcast(intent3)

                fromMyProjects = true
                isProjectHasChanges = false
                Toast.makeText(mActivity, "Project Saved", Toast.LENGTH_LONG).show()
                dismissProgress(false)
            } else {
                dismissProgress(false)
            }
        }
    }

    private fun prepareProjectJson(savedProjectDir: File?) {
        val projectJsonObject = JSONObject()

        //TODO: Project Data
        projectJsonObject.put("w", templateWidth)
        projectJsonObject.put("h", templateHeight)
        projectJsonObject.put("projectId", projectId)
        projectJsonObject.put("projectName", projectName)
        projectJsonObject.put("projectFileName", projectFileName)
        projectJsonObject.put("isBlank", isBlankCanvas)

        val elementDataJsonArray = JSONArray()

        //TODO: Element Data
        binding.editParentLayer.forEach {
            when (it) {
                is CustomTextView -> {
                    //TODO: Text Element
                    val elementView = it

                    val elementJsonObject = JSONObject()
                    elementJsonObject.put("ele", "txt")
                    elementJsonObject.put("x",
                            (templateWidth * elementView.x) / editorWidth.toDouble())
                    elementJsonObject.put("y",
                            (templateHeight * elementView.y) / editorHeight.toDouble())
                    elementJsonObject.put("w",
                            (templateWidth * (elementView.width)) / editorWidth.toDouble())
                    elementJsonObject.put("h",
                            (templateHeight * (elementView.height)) / editorHeight.toDouble())

                    //TODO: Background
                    if (elementView.resourceId != -1) {
                        val filterJsonObject = JSONObject()
                        filterJsonObject.put("categoryId", elementView.resourceCategoryId)
                        filterJsonObject.put("id", elementView.resourceId)
                        filterJsonObject.put("name", elementView.resourceName)
                        filterJsonObject.put("imgUrl", elementView.resourceUrl)
                        elementJsonObject.put("graphic", filterJsonObject)

                        val backgroundFile =
                            FileUtils.getBackgroundFile(mActivity, elementView.maskImage)
                        if (backgroundFile != null && backgroundFile.exists()) FileUtils.moveFile(
                                backgroundFile.absolutePath,
                                savedProjectDir!!.absolutePath)
                    }

                    elementJsonObject.put("img", elementView.maskImage)
                    elementJsonObject.put("clr", elementView.colorName)
                    elementJsonObject.put("agl", elementView.rotation)
                    elementJsonObject.put("lck", elementView.isLock)
                    elementJsonObject.put("opa", elementView.elementAlpha)
                    elementJsonObject.put("txt", elementView.text)
                    elementJsonObject.put("fnt", elementView.fontName)
                    elementJsonObject.put("lh",
                            (templateWidth * (elementView.mLineSpacing)) / editorWidth.toDouble())
                    elementJsonObject.put("ls",
                            (templateWidth * (elementView.letterSpacing)) / editorWidth.toDouble())
                    elementJsonObject.put("fs",
                            (templateWidth * elementView.textSize) / editorWidth.toDouble())
                    elementJsonObject.put("aln", elementView.textGravityIndex)

                    //TODO: Local Data
                    elementJsonObject.put("scale", elementView.scaleX)

                    elementJsonObject.put("flipH", elementView.flipX)
                    elementJsonObject.put("flipV", elementView.flipY)
                    elementJsonObject.put("agl", elementView.rotation)

                    //TODO: Font
                    if (elementView.fontId != -1) {
                        val filterJsonObject = JSONObject()
                        filterJsonObject.put("categoryId", elementView.fontCategoryId)
                        filterJsonObject.put("id", elementView.fontId)
                        filterJsonObject.put("name", elementView.fontName)
                        filterJsonObject.put("imgUrl", elementView.fontServerUrl)
                        elementJsonObject.put("font", filterJsonObject)
                    }
                    val fontFile = File(FileUtils.getFontFile(mActivity, elementView.fontName))
                    if (fontFile.exists()) FileUtils.moveFile(fontFile.absolutePath,
                            savedProjectDir!!.absolutePath)

                    elementJsonObject.put("jsonV2", 2)

                    elementDataJsonArray.put(elementJsonObject)
                }

                else -> {}
            }
        }
        projectJsonObject.put("data", elementDataJsonArray)

        if (savedProjectDir != null) {
            val jsonFile = savedProjectDir.absolutePath + "/" + projectName + ".json"
            FileUtils.storeProjectJson(jsonFile, projectJsonObject.toString())
        }
    }

    private fun setupTemplateUI() {
        try {
            isBlankCanvas = 1
            if (isBlankCanvas == 1) {
                projectId = -1
                projectName = Constants.NAME_BLANK_CANVAS
                projectFileName = Utils.getRandomString(10)
                templateData = TemplateData()
                templateData!!.w = /*intent!!.extras!!.getInt(KEY_WIDTH)*/2048
                templateData!!.h = /*intent!!.extras!!.getInt(KEY_HEIGHT)*/2048
            }

            templateWidth = templateData!!.w
            templateHeight = templateData!!.h
            val templateRatio = templateHeight / templateWidth.toDouble()

            editorWidth = Utils.getScreenWidth(mActivity)
            editorHeight = (Utils.getScreenWidth(mActivity) * templateRatio).roundToInt()

            val elementData = ElementData()
            elementData.ele = "blnk"
            elementData.idx = 0
            elementData.lck = 0
            templateData!!.data.add(0, elementData)

            binding.editParentContainer.doOnGlobalLayout { it ->
                val mLayoutParams =
                    binding.editParentWrapper.layoutParams as FrameLayout.LayoutParams
                val width = it.measuredWidth - (binding.editParentWrapper.marginStart * 2)
                val height = ((editorHeight * width) / editorWidth.toDouble()).roundToInt()

                val refWidth = (it.measuredWidth - (binding.editParentWrapper.marginStart * 2))
                val refHeight = (it.measuredHeight - (binding.editParentWrapper.marginTop * 2))

                if (height > refHeight) {
                    mLayoutParams.height = refHeight
                    mLayoutParams.width =
                        ((refHeight * editorWidth) / editorHeight.toDouble()).roundToInt()
                } else {
                    mLayoutParams.width = refWidth
                    mLayoutParams.height =
                        ((refWidth * editorHeight) / editorWidth.toDouble()).roundToInt()
                }
                binding.editParentWrapper.layoutParams = mLayoutParams
                binding.editParentWrapper.doOnGlobalLayout {
                    it.isVisible = true
                    editorWidth = it.measuredWidth
                    editorHeight = it.measuredHeight

                    addBlankView(elementData, 0)

                    //TODO: After add all element from json false thi variable
                    isProjectRenderInProgress = false
                }
            }
            setupUITouch()
            setupPickerView()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun prepareMainBottomPanel() {
        mainBottomPanelList.clear()
        mainBottomPanelList.add(ContentData(id = MAIN_ID_TEXT,
                ratioImage = R.drawable.add_text_selector,
                name = MyApplication.instance.context!!.getString(R.string.label_text),
                isSelected = false,
                enableSelectionMode = false))

        binding.recyclerViewParentPanel.layoutManager =
            LinearLayoutManager(mActivity, RecyclerView.HORIZONTAL, false)

        binding.recyclerViewParentPanel.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        mainBottomPanelAdapter = BottomPanelAdapter(mActivity,
                mainBottomPanelList,
                binding.recyclerViewParentPanel.paddingStart * 2,
                true)
        binding.recyclerViewParentPanel.adapter = mainBottomPanelAdapter

        mainBottomPanelAdapter!!.mItemCallback = object : ItemCallback {
            override fun onItemClick(position: Int, contentData: ContentData) {
                GlobalScope.launch(Dispatchers.Main) {
                    binding.recyclerViewParentPanel.post {
                        when (contentData.id) {
                            MAIN_ID_TEXT -> {
                                //TODO: Update Selection View
                                closeElementPanel()
                                if (currentSelectedViewId != -1) {
                                    currentSelectedViewId = -1
                                    currentSelectedViewType = ElementType.NONE
                                    disableSelectedElements()
                                    showSelectionUIForView(isVisible = false)
                                }
                                openTextEditScreen()
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
    }

    private fun addBlankView(elementData: ElementData, elementIndex: Int = -1): Int {
        var elementId: Int = -1
        try {
            val backgroundImageView = BackgroundImageView(mActivity)
            backgroundImageView.id = getNextUniqueValue()
            elementId = backgroundImageView.id
            backgroundImageView.elementType = ElementType.BLANK
            backgroundImageView.isLock = elementData.lck
            //TODO: Dimen.
            val mLayoutParams = FrameLayout.LayoutParams(binding.editParentLayer.width,
                    binding.editParentLayer.height)
            backgroundImageView.layoutParams = mLayoutParams

            binding.editParentLayer.addView(backgroundImageView)

            backgroundImageView.mCallbackListener = object : BackgroundImageView.CallbackListener {
                override fun onViewClick(motionEvent: MotionEvent) {
                    try {
                        //TODO: De-Select View and Close related Panel
                        disableSelectedElements()
                        closeElementPanel(backgroundImageView.elementType)

                        //TODO: Update Selection View
                        currentSelectedViewType = backgroundImageView.elementType
                        currentSelectedViewId = backgroundImageView.id

                        //TODO: Show Selection Border UI
                        showSelectionUIForView(backgroundImageView, true)

                        //TODO: Panel
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            backgroundImageView.mEventCallbackListener =
                object : BackgroundImageView.EventCallbackListener {
                    override fun onEventDetected(motionEvent: MotionEvent) {
                        try {
                            updateTouchEvent(motionEvent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return elementId
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupUITouch() {
        binding.layoutEditorFrame.mCallbackListener =
            object : BackgroundFrameLayout.CallbackListener {
                override fun onViewClick(motionEvent: MotionEvent) {
                    try {
                        //TODO: Update Selection View
                        currentSelectedViewId = binding.editParentContainer.id
                        currentSelectedViewType = ElementType.NONE

                        //TODO: De-Select View and Close related Panel
                        closeElementPanel()
                        disableSelectedElements()

                        //TODO: Show Selection Border UI
                        showSelectionUIForView(isVisible = false)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        binding.layoutEditorFrame.mEventCallbackListener =
            object : BackgroundFrameLayout.EventCallbackListener {
                override fun onEventDetected(motionEvent: MotionEvent) {
                    updateTouchEvent(motionEvent)
                }
            }

        binding.editParentContainer.mCallbackListener =
            object : BackgroundFrameLayout.CallbackListener {
                override fun onViewClick(motionEvent: MotionEvent) {
                    try {
                        //TODO: Update Selection View
                        currentSelectedViewId = binding.editParentContainer.id
                        currentSelectedViewType = ElementType.NONE

                        //TODO: De-Select View and Close related Panel
                        closeElementPanel()
                        disableSelectedElements()

                        //TODO: Show Selection Border UI
                        showSelectionUIForView(isVisible = false)
//                        currentSelectedViewType = ElementType.NONE

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        binding.editParentContainer.mEventCallbackListener =
            object : BackgroundFrameLayout.EventCallbackListener {
                override fun onEventDetected(motionEvent: MotionEvent) {
                    try {
                        updateTouchEvent(motionEvent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
    }

    private fun setupPickerView() {
        try {
            binding.colorPickerView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            val bubbleFlag = BubbleFlag(mActivity)
            bubbleFlag.flagMode = FlagMode.ALWAYS
            bubbleFlag.isFlipAble = true
            binding.colorPickerView.flagView = bubbleFlag
            binding.colorPickerView.setColorListener(object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    try {
                        if (envelope != null) {
                            val colorCode =
                                ColorUtils.removeAlphaValue(ColorUtils.correctColorName(binding.colorPickerView.colorEnvelope!!.hexCode))
                            setColorSelection(colorCode)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
            binding.colorPickerView.setLifecycleOwner(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setColorSelection(hexCode: String) {
        try {
            val elementView =
                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                        .singleOrNull()
            if (elementView != null) when (elementView) {
                is CustomTextView -> {
                    (elementView as CustomTextView).applyColor(hexCode, true)
                    currentPanelFragment!!.setDropperColor(hexCode)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var currentPanelFragment: PanelContainerFragment? = null
    private fun prepareElementBottomPanel() {
//        if (elementBottomPanelList.size > 0 && elementBottomPanelAdapter != null) {
//            elementBottomPanelList.clear()
//            elementBottomPanelAdapter!!.notifyItemRangeRemoved(0, elementBottomPanelList.size)
//        }

        val localList = ArrayList<ContentData>()

        if (currentSelectedViewType == ElementType.TEXT) {
//            localList.add(
//                ContentData(
//                    ratioImage = R.drawable.text_edit_selector,
//                    id = ELEMENT_ID_ADD,
//                    name = getString(R.string.label_add),
//                    isSelected = false,
//                    enableSelectionMode = false
//                )
//            )
            localList.add(ContentData(ratioImage = R.drawable.text_edit_selector,
                    id = ELEMENT_ID_EDIT,
                    name = getString(R.string.label_edit),
                    isSelected = false,
                    enableSelectionMode = false))

            localList.add(ContentData(id = ELEMENT_ID_DELETE,
                    ratioImage = R.drawable.delete_selector,
                    name = getString(R.string.label_delete),
                    isSelected = false,
                    enableSelectionMode = false))
            localList.add(ContentData(ratioImage = R.drawable.duplicate_selector,
                    id = ELEMENT_ID_DUPLICATE,
                    name = getString(R.string.label_duplicate),
                    isSelected = false,
                    enableSelectionMode = false))

            localList.add(ContentData(ratioImage = R.drawable.font_selector,
                    id = ELEMENT_ID_FONT,
                    name = getString(R.string.label_fonts),
                    isSelected = false,
                    enableSelectionMode = false))
            localList.add(ContentData(ratioImage = R.drawable.opacity_selector,
                    id = ELEMENT_ID_OPACITY,
                    name = getString(R.string.label_opacity),
                    isSelected = false))
            localList.add(ContentData(ratioImage = R.drawable.spacing_selector,
                    id = ELEMENT_ID_SPACING,
                    name = getString(R.string.label_spacing),
                    isSelected = false))
            val textView =
                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                        .singleOrNull()
            val gravityResource = if (textView != null) {
                when ((textView!! as CustomTextView).textGravityIndex) {
                    0 -> {
                        R.drawable.align_left_selector
                    }

                    1 -> {
                        R.drawable.align_center_selector
                    }

                    2 -> {
                        R.drawable.align_right_selector
                    }

                    else -> {
                        R.drawable.align_center_selector
                    }
                }
            } else {
                R.drawable.align_center_selector
            }
            localList.add(ContentData(ratioImage = gravityResource,
                    id = ELEMENT_ID_ALIGN,
                    name = getString(R.string.label_align),
                    isSelected = false))
            localList.add(ContentData(ratioImage = R.drawable.text_format_selector,
                    id = ELEMENT_ID_FORMAT,
                    name = getString(R.string.label_format),
                    isSelected = false))
            localList.add(ContentData(ratioImage = R.drawable.order_selector,
                    id = ELEMENT_ID_ORDER,
                    name = getString(R.string.label_order),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_FILL_MASK,
                    ratioImage = R.drawable.ic_fill_bg_selector,
                    name = getString(R.string.label_text_fill),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_FLIPH,
                    ratioImage = R.drawable.flip_h_selector,
                    name = getString(R.string.label_flip_h),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_FLIPV,
                    ratioImage = R.drawable.flip_v_selector,
                    name = getString(R.string.label_flip_v),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_SHADOW,
                    ratioImage = R.drawable.ic_shadow_selector,
                    name = getString(R.string.label_shadow),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_COPY_STYLE,
                    ratioImage = R.drawable.ic_copy_selector,
                    name = getString(R.string.label_copy_style),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_PASTE_STYLE,
                    ratioImage = R.drawable.ic_paste_selector,
                    name = getString(R.string.label_paste_style),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_SIZE_PLUS,
                    ratioImage = R.drawable.ic_shadow_selector,
                    name = getString(R.string.label_size_plus),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_SIZE_MINUS,
                    ratioImage = R.drawable.ic_shadow_selector,
                    name = getString(R.string.label_size_minus),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_NUDGE,
                    ratioImage = R.drawable.ic_shadow_selector,
                    name = getString(R.string.label_nudge),
                    isSelected = false))
            localList.add(ContentData(id = ELEMENT_ID_TEXT_ROTATE,
                    ratioImage = R.drawable.ic_shadow_selector,
                    name = getString(R.string.label_rotate),
                    isSelected = false))
        }

        elementBottomPanelList.clear()
        elementBottomPanelList.addAll(localList)

        if (elementBottomPanelAdapter != null) {
            binding.recyclerViewElementPanel.post {
                elementBottomPanelAdapter!!.notifyDataSetChanged()
            }
        } else {
            binding.recyclerViewElementPanel.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            val linearLayoutManager =
                SnappyLinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
            (linearLayoutManager as SnappyLayoutManager).setSnapType(SnapType.CENTER)
            (linearLayoutManager as SnappyLayoutManager).setSnapDuration(240)
            (linearLayoutManager as SnappyLayoutManager).setSnapPaddingStart(Utils.getScreenWidth(
                    mActivity) - (Utils.dpToPx(32F).roundToInt() / 2))
            (linearLayoutManager as SnappyLayoutManager).setSnapPaddingEnd(Utils.getScreenWidth(
                    mActivity) + (Utils.dpToPx(32F).roundToInt() / 2))
            (linearLayoutManager as SnappyLayoutManager).setSnapInterpolator(DecelerateInterpolator())
            binding.recyclerViewElementPanel.layoutManager = linearLayoutManager

            val animator: RecyclerView.ItemAnimator? = binding.recyclerViewElementPanel.itemAnimator
            if ((animator != null) && (animator is SimpleItemAnimator)) {
                animator.changeDuration = 0
                animator.supportsChangeAnimations = false
            }
            binding.recyclerViewElementPanel.itemAnimator = null
            binding.recyclerViewElementPanel.setHasFixedSize(true)
            binding.recyclerViewElementPanel.setItemViewCacheSize(elementBottomPanelList.size)

            elementBottomPanelAdapter = BottomPanelAdapter(mActivity, elementBottomPanelList)
            binding.recyclerViewElementPanel.adapter = elementBottomPanelAdapter
        }

        elementBottomPanelAdapter?.mItemCallback = object : ItemCallback {
            override fun onItemClick(position: Int, contentData: ContentData) {
                binding.recyclerViewElementPanel.post {
                    when (contentData.id) {
                        ELEMENT_ID_ADD -> {
                            openTextEditScreen()
                        }
                        ELEMENT_ID_EDIT -> {
                            removeFragment()
                            val elementView =
                                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()
                            if (elementView != null) openTextEditScreen(forEdit = true,
                                    (elementView as CustomTextView))
                        }
                        ELEMENT_ID_DELETE -> {
                            removeFragment()
                            if (currentSelectedViewId != -1) {
                                disableSelectedElements()
                                isProjectHasChanges = true
                                closeElementPanel()
                                val elementView =
                                    binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                            .singleOrNull()
                                binding.editParentLayer.removeView(elementView)
                                currentSelectedViewId = -1
                                currentSelectedViewType = ElementType.NONE
                            }
                        }
                        ELEMENT_ID_DUPLICATE -> {
                            val elementView =
                                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()
                            if (elementView != null) {
                                when (elementView) {
                                    is CustomTextView -> {
                                        removeFragment()
                                        //TODO: De-Select View and Close related Panel
                                        disableSelectedElements()

                                        //TODO: Update Selection View
                                        duplicateTextView((elementView as CustomTextView))
                                    }
                                    else -> {}
                                }
                            }
                        }
                        ELEMENT_ID_FONT -> {
                            val selectedView =
                                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()

                            removeFragment()
                            elementBottomPanelAdapter!!.setItemSelection()

                            val showGoogleBottomSheet = true

                            if (showGoogleBottomSheet) {
                                //TODO: Show Font bottom Sheet
                                fontsBinding.myGoogleBottomSheet.elementFontCategoryTabs!!.getTabAt(
                                        0)?.select()

                                fontsBinding.myGoogleBottomSheet.filterProjectFonts(projectFontsList)
                                fontsBinding.myGoogleBottomSheet.setFontBackground(binding.fontBackground)

                                if (selectedView != null) fontsBinding.myGoogleBottomSheet.selectTextFont(
                                        (selectedView as CustomTextView).fontId,
                                        selectedView!!.fontName)
                                AnimUtils.toggleSlide(true,
                                        binding.layoutParentEdit,
                                        fontsBinding.myGoogleBottomSheet)
                                AnimUtils.toggleSlide(true,
                                        binding.layoutParentEdit,
                                        binding.fontBackground)

                                //TODO: Apply font on TextView
                                fontsBinding.myGoogleBottomSheet.mItemCallback =
                                    object : FontItemCallback {

                                        override fun onItemClick(position: Int,
                                                                 contentData: ContentData) {
                                            if (AppFileUtils.isFontFileExit(mActivity,
                                                        contentData)) {
                                                val selectedView =
                                                    binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                                            .singleOrNull()
                                                if (selectedView != null) {
                                                    if (AppFileUtils.isFontFileExit(mActivity,
                                                                contentData)) {
                                                        val mimeType = contentData.font_file!!.name
                                                        val extension =
                                                            mimeType.substring(mimeType.indexOf(".") + 1)
                                                        val fileName =
                                                            "${contentData.name}.$extension"
                                                        val filePath =
                                                            File(FileUtils.getInternalFontDir(
                                                                    mActivity), fileName)
                                                        if (selectedView != null && filePath.exists()) {
                                                            (selectedView as CustomTextView).setFont(
                                                                    Typeface.createFromFile(filePath),
                                                                    true)
                                                            (selectedView as CustomTextView).fontName =
                                                                contentData.name
                                                            (selectedView as CustomTextView).fontId =
                                                                contentData.id
                                                            (selectedView as CustomTextView).fontCategoryId =
                                                                contentData.fontcategory_id
                                                            (selectedView as CustomTextView).fontServerUrl =
                                                                UrlUtils.getFontsFontFile(
                                                                        contentData)

                                                            var oldWidth =
                                                                (selectedView as CustomTextView).layoutParams.width
                                                            if (oldWidth == -2 || oldWidth == -1) oldWidth =
                                                                ((selectedView as CustomTextView).minWidth * (selectedView as CustomTextView).scaleX).roundToInt()

                                                            val maxWidth =
                                                                (selectedView as CustomTextView).minMaxLengthWords(
                                                                        (selectedView as CustomTextView).text.toString())
                                                            if (maxWidth < oldWidth) {
                                                                (selectedView as CustomTextView).layoutParams.width =
                                                                    oldWidth// maxWidth.roundToInt()
                                                                (selectedView as CustomTextView).updateScaleSize()
                                                            }

                                                            (selectedView as CustomTextView).viewTreeObserver.addOnGlobalLayoutListener(
                                                                    object :
                                                                            ViewTreeObserver.OnGlobalLayoutListener {
                                                                        override fun onGlobalLayout() {
                                                                            (selectedView as CustomTextView).viewTreeObserver.removeOnGlobalLayoutListener(
                                                                                    this)
                                                                            showSelectionUIForView((selectedView as CustomTextView),
                                                                                    true)
                                                                        }
                                                                    })
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                            } else {
                                currentPanelFragment =
                                    PanelContainerFragment.newInstance(PanelType.FONTS)
                                currentPanelFragment!!.setEditorView(binding.editParentLayer,
                                        binding.editParentWrapper,
                                        binding.colorPickerView)
                                currentPanelFragment!!.setProjectDetails(projectName,
                                        "",
                                        fromMyProjects)
                                loadFragment(binding.elementSubPanelContainer,
                                        currentPanelFragment!!)
                            }
                        }
                        ELEMENT_ID_OPACITY -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.OPACITY
                            if (!isSamePanel) {
                                removeFragment()
                                currentPanelFragment =
                                    PanelContainerFragment.newInstance(PanelType.OPACITY)
                                currentPanelFragment!!.setEditorView(binding.editParentLayer,
                                        binding.editParentWrapper,
                                        binding.colorPickerView)
                                currentPanelFragment!!.setProjectDetails(projectName,
                                        "",
                                        fromMyProjects)
                                loadFragment(binding.elementSubPanelContainer,
                                        currentPanelFragment!!)
                            }
                        }
                        ELEMENT_ID_SPACING -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.SPACING
                            if (!isSamePanel) {
                                removeFragment()
                                currentPanelFragment =
                                    PanelContainerFragment.newInstance(PanelType.SPACING)
                                currentPanelFragment!!.setEditorView(binding.editParentLayer,
                                        binding.editParentWrapper,
                                        binding.colorPickerView)
                                currentPanelFragment!!.setProjectDetails(projectName,
                                        "",
                                        fromMyProjects)
                                loadFragment(binding.elementSubPanelContainer,
                                        currentPanelFragment!!)
                            }
                        }
                        ELEMENT_ID_ALIGN -> {
                            removeFragment()
                            val elementView =
                                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()
                            if (elementView != null) {
                                (elementView as CustomTextView).textGravityIndex =
                                    (when ((elementView as CustomTextView).textGravityIndex) {
                                        0 -> {
                                            1
                                        }
                                        1 -> {
                                            2
                                        }
                                        2 -> {
                                            0
                                        }
                                        else -> {
                                            1
                                        }
                                    })
                                val contentData =
                                    elementBottomPanelList.filter { it.id == ELEMENT_ID_ALIGN }
                                            .singleOrNull()
                                contentData!!.ratioImage =
                                    when ((elementView as CustomTextView).textGravityIndex) {
                                        0 -> {
                                            R.drawable.align_left_selector
                                        }
                                        1 -> {
                                            R.drawable.align_center_selector
                                        }
                                        2 -> {
                                            R.drawable.align_right_selector
                                        }
                                        else -> {
                                            R.drawable.align_center_selector
                                        }
                                    }
                                elementBottomPanelAdapter!!.notifyItemChanged(elementBottomPanelList.indexOf(
                                        contentData))
                            }
                        }
                        ELEMENT_ID_FORMAT -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.FORMAT
                            if (!isSamePanel) {
                                removeFragment()
                                currentPanelFragment =
                                    PanelContainerFragment.newInstance(PanelType.FORMAT)
                                currentPanelFragment!!.setEditorView(binding.editParentLayer,
                                        binding.editParentWrapper,
                                        binding.colorPickerView)
                                currentPanelFragment!!.setProjectDetails(projectName,
                                        "",
                                        fromMyProjects)
                                loadFragment(binding.elementSubPanelContainer,
                                        currentPanelFragment!!)
                            }
                        }
                        ELEMENT_ID_TEXT_FILL_MASK -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.TEXT_MASK
                            if (!isSamePanel) {
                                removeFragment()
                                currentPanelFragment =
                                    PanelContainerFragment.newInstance(PanelType.TEXT_MASK)
                                currentPanelFragment!!.setEditorView(binding.editParentLayer,
                                        binding.editParentWrapper,
                                        binding.colorPickerView)
                                currentPanelFragment!!.setProjectDetails(projectName,
                                        "",
                                        fromMyProjects)
                                loadFragment(binding.elementSubPanelContainer,
                                        currentPanelFragment!!)
                            }
                        }
                        ELEMENT_ID_ORDER -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.ORDER
                            if (!isSamePanel) {
                                removeFragment()
                                currentPanelFragment =
                                    PanelContainerFragment.newInstance(PanelType.ORDER)
                                currentPanelFragment!!.setEditorView(binding.editParentLayer,
                                        binding.editParentWrapper,
                                        binding.colorPickerView)
                                currentPanelFragment!!.setProjectDetails(projectName,
                                        "",
                                        fromMyProjects)
                                loadFragment(binding.elementSubPanelContainer,
                                        currentPanelFragment!!)
                            }
                        }
                        ELEMENT_ID_TEXT_FLIPH -> {
                            removeFragment()
                            val elementView =
                                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()
                            if (elementView != null) when (currentSelectedViewType) {
                                ElementType.TEXT -> {
                                    (elementView as CustomTextView).flipText(isHorizontal = true)
                                    elementBottomPanelList.filter { it.id == ELEMENT_ID_TEXT_FLIPH }
                                            .singleOrNull()?.flipEnabled =
                                        (elementView as CustomTextView).flipX == 1
                                }
                                else -> {}
                            }
                        }
                        ELEMENT_ID_TEXT_FLIPV -> {
                            removeFragment()
                            val elementView =
                                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()
                            if (elementView != null) when (currentSelectedViewType) {
                                ElementType.TEXT -> {
                                    (elementView as CustomTextView).flipText(isVertical = true)
                                    elementBottomPanelList.filter { it.id == ELEMENT_ID_TEXT_FLIPV }
                                            .singleOrNull()?.flipEnabled =
                                        (elementView as CustomTextView).flipY == 1
                                }
                                else -> {}
                            }
                        }
                        ELEMENT_ID_TEXT_COPY_STYLE -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.COPY_STYLE
                            if (!isSamePanel) {
                                removeFragment()
                            }
                            copyPasteSelectedElement(isCopy = true)
                        }
                        ELEMENT_ID_TEXT_PASTE_STYLE -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.PASTE_STYLE
                            if (!isSamePanel) {
                                removeFragment()
                            }
                            copyPasteSelectedElement(isCopy = false)
                        }
                        ELEMENT_ID_TEXT_SIZE_PLUS -> {
                            removeFragment()
                            val elementView =
                                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()
                            if (elementView != null) when (currentSelectedViewType) {
                                ElementType.TEXT -> {
                                    (elementView as CustomTextView).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            (elementView as CustomTextView).textSize + 6)
                                    (elementView as CustomTextView).updateScaleSize()
                                    updateTextWidthHeight(elementView as CustomTextView)
                                }
                                else -> {}
                            }
                        }
                        ELEMENT_ID_TEXT_SIZE_MINUS -> {
                            removeFragment()
                            val elementView =
                                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                        .singleOrNull()
                            if (elementView != null) when (currentSelectedViewType) {
                                ElementType.TEXT -> {
                                    (elementView as CustomTextView).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                            (elementView as CustomTextView).textSize - 6)
                                    (elementView as CustomTextView).updateScaleSize()
                                    updateTextWidthHeight(elementView as CustomTextView)
                                }
                                else -> {}
                            }
                        }
                        ELEMENT_ID_TEXT_NUDGE -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.NUDGE
                            if (!isSamePanel) {
                                removeFragment()
                                currentPanelFragment =
                                    PanelContainerFragment.newInstance(PanelType.NUDGE)
                                currentPanelFragment!!.setEditorView(binding.editParentLayer,
                                        binding.editParentWrapper,
                                        binding.colorPickerView)
                                currentPanelFragment!!.setProjectDetails(projectName,
                                        "",
                                        fromMyProjects)
                                loadFragment(binding.elementSubPanelContainer,
                                        currentPanelFragment!!)
                            }
                        }
                        ELEMENT_ID_TEXT_ROTATE -> {
                            val isSamePanel = if (currentPanelFragment == null) {
                                false
                            } else currentPanelFragment!!.panelType == PanelType.ROTATE
                            if (!isSamePanel) {
                                removeFragment()
                                currentPanelFragment =
                                    PanelContainerFragment.newInstance(PanelType.ROTATE)
                                currentPanelFragment!!.setEditorView(binding.editParentLayer,
                                        binding.editParentWrapper,
                                        binding.colorPickerView)
                                currentPanelFragment!!.setProjectDetails(projectName,
                                        "",
                                        fromMyProjects)
                                loadFragment(binding.elementSubPanelContainer,
                                        currentPanelFragment!!)
                            }
                        }
                    }
                }
            }

            override fun onCategoryClick(position: Int, ContentData: ContentData) {

            }

            override fun onHeaderContentClick(position: Int, contentData: ContentData) {

            }
        }

        binding.recyclerViewElementPanel.post {
            binding.recyclerViewElementPanel.scrollToPosition(0)
        }

        binding.recyclerViewElementPanel.postDelayed({
            AnimUtils.toggleSlide(true, binding.layoutParentEdit, binding.elementPanelLayout)
        }, 20L)
    }

    //TODO: Project Fonts collector
    var projectFontsList = ArrayList<String>()
    private fun addToProjectFonts(fontName: String) {
        if (!projectFontsList.contains(fontName)) projectFontsList.add(fontName)
    }

    var copyElementData: ElementData? = null
    fun copyPasteSelectedElement(isCopy: Boolean = false) {
        try {
            val elementView =
                binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                        .singleOrNull()
            if (elementView != null) when (elementView) {
                is CustomTextView -> {
                    if (isCopy) {
                        //TODO: Common data
                        copyElementData = ElementData()
                        copyElementData!!.ele = "txt"
                        copyElementData!!.img = elementView.maskImage
                        copyElementData!!.clr = elementView.colorName
                        copyElementData!!.agl = elementView.rotation
                        copyElementData!!.opa = elementView.elementAlpha
                        copyElementData!!.fnt = elementView.fontName
                        copyElementData!!.lh =
                            (templateWidth * (elementView.mLineSpacing)) / editorWidth.toDouble()
                        copyElementData!!.ls =
                            (templateWidth * (elementView.letterSpacing)) / editorWidth.toDouble()
                        copyElementData!!.fs =
                            (templateWidth * elementView.textSize) / editorWidth.toDouble()
                        copyElementData!!.lck = elementView.isLock
                        copyElementData!!.scale = elementView.scaleX
                        copyElementData!!.flipH = elementView.flipX
                        copyElementData!!.flipV = elementView.flipY
                        copyElementData!!.aln = elementView.textGravityIndex
                        copyElementData!!.textCaseIndex = elementView.textCaseIndex

                        copyElementData!!.x =
                            (templateWidth * elementView.x) / editorWidth.toDouble()
                        copyElementData!!.y =
                            (templateHeight * elementView.y) / editorHeight.toDouble()
                        copyElementData!!.w =
                            (templateWidth * (elementView.width)) / editorWidth.toDouble()
                        copyElementData!!.h =
                            (templateHeight * (elementView.height)) / editorHeight.toDouble()

                        //TODO: Applied font data
                        val font = ResourceData()
                        font.id = elementView.fontId
                        font.categoryId = elementView.fontCategoryId
                        font.name = elementView.fontName
                        font.imgUrl = elementView.fontServerUrl
                        copyElementData!!.font = font

                        //TODO: Applied resource data
                        val resource = ResourceData()
                        resource.id = elementView.resourceId
                        resource.categoryId = elementView.resourceCategoryId
                        resource.name = elementView.resourceName
                        resource.imgUrl = elementView.resourceUrl
                        copyElementData!!.graphic = resource

                    } else {
                        if (copyElementData != null) {

                            val textView = elementView as CustomTextView
                            //TODO: Paste Rotation, scale, width, height of text
//                            val mViewWidth = ((editorWidth * (copyElementData!!.w)) / templateWidth).roundToInt()
//
//                            textView.minWidth = mViewWidth
//                            val layoutParams1 = FrameLayout.LayoutParams(
//                                ViewGroup.LayoutParams.WRAP_CONTENT,
//                                ViewGroup.LayoutParams.WRAP_CONTENT
//                            )
//
//                            layoutParams1.width = mViewWidth
//                            layoutParams1.gravity = Gravity.CENTER
//                            textView.layoutParams = layoutParams1
//
//                            textView.adjustAngle(copyElementData!!.agl)
//                            textView.updateViewScale(copyElementData!!.scale)

                            if (copyElementData!!.img.isNotEmpty()) {
                                textView.applyColor(ColorUtils.correctColorName("#FFFFFF"),
                                        byUser = true)
                                ApplyTextMaskFromProject(mActivity,
                                        textView,
                                        projectName,
                                        copyElementData!!.img,
                                        binding.editParentLayer.height,
                                        copyElementData).execute()
                            } else {
                                textView.applyColor(ColorUtils.correctColorName(copyElementData!!.clr),
                                        byUser = true)
                            }
                            textView.setElementAlpha(copyElementData!!.opa, byUser = true)
                            val newTextSize =
                                ((editorWidth * copyElementData!!.fs) / templateWidth.toFloat())
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize.toFloat())

                            textView.flipX = copyElementData!!.flipH
                            textView.flipY = copyElementData!!.flipV

                            textView.textGravityIndex = copyElementData!!.aln
                            textView.fontName = copyElementData!!.fnt
                            textView.isLock = copyElementData!!.lck
                            textView.maskImage = copyElementData!!.img

                            var fontFile = FileUtils.getFontFile(mActivity, copyElementData!!.fnt)
                            textView.setFont(if (fontFile.isNotEmpty()) {
                                Typeface.createFromFile((fontFile))
                            } else {
                                textView.fontName = "LibreBaskerville-Regular"
                                fontFile =
                                    FileUtils.getFontFile(mActivity, "LibreBaskerville-Regular")
                                Typeface.createFromFile((fontFile))
                            }, byUser = true)
                            if (copyElementData!!.lh != 0.0) {
                                val newLineHeight: Int =
                                    (((editorWidth * copyElementData!!.lh) / templateWidth).roundToInt())
                                if (newLineHeight > 0) textView.applyLineSpacing(newLineHeight.toFloat())
                            }
                            if (copyElementData!!.ls != 0.0) {
                                textView.applyLetterSpacing(if (copyElementData!!.jsonV2 < 2) {
                                    (((editorWidth * (copyElementData!!.ls + (copyElementData!!.ls * 0.02F))) / templateWidth)).toFloat()
                                } else {
                                    (((editorWidth * copyElementData!!.ls) / templateWidth)).toFloat()
                                })
                            }
                            textView.changeCaseType(copyElementData!!.textCaseIndex, true)

                            //TODO: Applied font data
                            if (copyElementData!!.font != null) {
                                textView.fontId = copyElementData!!.font!!.id
                                textView.fontCategoryId = copyElementData!!.font!!.categoryId
                                textView.fontName = copyElementData!!.font!!.name
                                textView.fontServerUrl = copyElementData!!.font!!.imgUrl
                            }

                            //TODO: Applied resource data
                            if (copyElementData!!.graphic != null) {
                                textView.resourceId = copyElementData!!.graphic!!.id
                                textView.resourceCategoryId = copyElementData!!.graphic!!.categoryId
                                textView.resourceName = copyElementData!!.graphic!!.name
                                textView.resourceUrl = copyElementData!!.graphic!!.imgUrl
                            }

                            updateTextWidthHeight(textView)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun addTextView(elementData: ElementData, fromUser: Boolean) {
        try {
            var elementId: Int = -1
            var mViewWidth = if (fromUser) editorWidth - Utils.dpToPx(48F).roundToInt()
            else ((editorWidth * (elementData.w)) / templateWidth).roundToInt()
            val mViewHeight = if (fromUser) editorHeight / 2
            else ((editorHeight * (elementData.h)) / templateHeight).roundToInt()

            val newTextSize = ((editorWidth * elementData.fs) / templateWidth.toFloat())
            val newLatterSpace = ((elementData.ls) / (elementData.fs)).toFloat()

            val mViewLeft = ((editorWidth * (elementData.x)) / templateWidth).roundToInt()
            val mViewTop = ((editorHeight * (elementData.y)) / templateHeight).roundToInt()

            val textView = CustomTextView(mActivity,
                    binding.editParentLayer,
                    binding.viewBaseVertical,
                    binding.viewBaseHorizontal)

            textView.visibility = View.INVISIBLE
            textView.setWrapImages(binding.leftWrap, binding.rightWrap)

            textView.id = getNextUniqueValue()
            elementId = textView.id
            textView.tag = textView.id.toString()
            textView.mTag = textView.id.toString()
            textView.isSoundEffectsEnabled = false
            textView.text = elementData.txt
            isProjectHasChanges = fromUser
            textView.storedString = elementData.txt
            textView.textGravityIndex = elementData.aln
            textView.fontName = elementData.fnt
            textView.isLock = elementData.lck
            textView.maskImage = elementData.img
            if (fromMyProjects) textView.flipText(elementData.flipH == 1, elementData.flipV == 1)

            if (elementData.agl != 0F) {
                textView.rotation = elementData.agl
            }

            if (fromUser) textView.updatedByUser = true
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize.toFloat())

            textView.minWidth = mViewWidth + if (fromUser) Utils.dpToPx(12F).roundToInt() else 0
            val layoutParams1 = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            if (fromMyProjects) layoutParams1.width = mViewWidth
            layoutParams1.gravity = Gravity.CENTER
            if (!fromUser) {
                textView.x = ((mViewWidth / 2 - editorWidth / 2) + mViewLeft).toFloat()
                textView.y = ((mViewHeight / 2 - editorHeight / 2) + mViewTop).toFloat()
            }
            textView.layoutParams = layoutParams1

            var fontFile = FileUtils.getFontFile(mActivity, elementData.fnt)
            textView.setFont(if (fontFile.isNotEmpty()) {
                Typeface.createFromFile((fontFile))
            } else {
                textView.fontName = "LibreBaskerville-Regular"
                fontFile = FileUtils.getFontFile(mActivity, "LibreBaskerville-Regular")
                Typeface.createFromFile((fontFile))
            }, fromUser)
            if (elementData.lh != 0.0) {
                val newLineHeight: Int =
                    (((editorWidth * elementData.lh) / templateWidth).roundToInt())
                if (newLineHeight > 0) textView.applyLineSpacing(newLineHeight.toFloat())
            }
            if (elementData.ls != 0.0) {
                if (fromMyProjects) {
                    textView.applyLetterSpacing(if (elementData.jsonV2 < 2) {
                        (((editorWidth * (elementData.ls + (elementData.ls * 0.02F))) / templateWidth)).toFloat()
                    } else {
                        (((editorWidth * elementData.ls) / templateWidth)).toFloat()
                    })
                } else {
                    textView.applyLetterSpacing(newLatterSpace)
                }
            }
            textView.changeCaseType(when (elementData.format) {
                TextCaseType.UPPER_CASE.name -> {
                    TextCaseType.UPPER_CASE
                }

                TextCaseType.LOWER_CASE.name -> {
                    TextCaseType.LOWER_CASE
                }

                TextCaseType.TITLE_CASE.name -> {
                    TextCaseType.TITLE_CASE
                }

                else -> {
                    TextCaseType.DEFAULT
                }
            }, fromUser)

            if (elementData.agl != 0F) {
                textView.rotation = elementData.agl
            }

            //TODO: Opacity
            textView.setElementAlpha(elementData.opa, fromUser)

            if (elementData.img.isNotEmpty()) {
                textView.applyColor(ColorUtils.correctColorName("#FFFFFF"), fromUser)
                ApplyTextMaskFromProject(mActivity,
                        textView,
                        projectName,
                        elementData.img,
                        binding.editParentLayer.height,
                        elementData).execute()
            } else {
                textView.applyColor(ColorUtils.correctColorName(elementData.clr), fromUser)
            }

            textView.updateViewScale(elementData.scale)

            binding.editParentLayer.addView(textView)
            commonTextViewFunctions(textView)

            textView.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    textView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    if (fromUser) {
                        val maxWidth = textView.minMaxLengthWords(elementData.txt)
                        val textViewDefaultWidth = editorWidth - Utils.dpToPx(48F).roundToInt()
                        mViewWidth =
                            (if (maxWidth < textViewDefaultWidth) maxWidth else textViewDefaultWidth).toInt()
                        textView.layoutParams.width = mViewWidth + Utils.dpToPx(12F).roundToInt()

                        textView.updateScaleSize()

                        textView.viewTreeObserver.addOnGlobalLayoutListener(object :
                                ViewTreeObserver.OnGlobalLayoutListener {
                            override fun onGlobalLayout() {
                                textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                                showSelectionUIForView(textView, true)
                                updateAllElementPosition()
                            }
                        })
                    } else if (!fromMyProjects && (mViewWidth > editorWidth)) {
                        showSelectionUIForView(textView, false)
                        val maxWidth = textView.minMaxLengthWords(elementData.txt)
                        textView.layoutParams.width = mViewWidth + Utils.dpToPx(12F).roundToInt()
                        textView.updateScaleSize()
                        textView.x = textView.x + Utils.dpToPx(6F).roundToInt()
                        textView.y = textView.y + Utils.dpToPx(3F).roundToInt()
                    }

                    textView.visibility = View.VISIBLE

                    if (fromUser) {
                        var isSamePanel = true
                        if (currentSelectedViewType != textView.elementType) {
                            //TODO: De-Select View and Close related Panel
                            disableSelectedElements()
                            closeElementPanel(textView.elementType)
                            isSamePanel = false
                        }

                        //TODO: Update Selection View
                        currentSelectedViewType = textView.elementType
                        currentSelectedViewId = textView.id

                        //TODO: Show Selection Border UI
                        textView.visibleAll()
                        textView.select(true)

                        textView.setWrapImages(binding.leftWrap, binding.rightWrap)

                        showSelectionUIForView(textView, true)

                        if (isSamePanel) {
                            if (currentPanelFragment != null) {
                                currentPanelFragment!!.updateColorSelection()
                                currentPanelFragment!!.updateTextSpacing()
                                currentPanelFragment!!.updateTextFormat()
                                currentPanelFragment!!.updateOpacity()
                                currentPanelFragment!!.updateOrderLayout()
                            }
                        } else {
                            //TODO: Panel
                            prepareElementBottomPanel()
                        }
                    }
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class ApplyTextMaskFromProject(private val context: Context,
                                         private val textView: CustomTextView,
                                         private val projectName: String?,
                                         private val resourceImageName: String,
                                         private val height: Int,
                                         private val elementData: ElementData? = null) :
            CoroutineAsyncTask<Void, Void, Bitmap>() {
        var resourcePath = ""
        override fun doInBackground(vararg params: Void?): Bitmap? {
            resourcePath = AppFileUtils.getFileFromProjectDir(context,
                    projectName!!,
                    resourceImageName!!,
                    fromMyProjects = true)

            if (resourcePath.isEmpty()) resourcePath =
                FileUtils.getBackgroundFile(context, resourceImageName)!!.absolutePath

            val bitmap = Glide.with(context).asBitmap().load(resourcePath)
                    .apply(RequestOptions().skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE).override(height)).submit()
                    .get()
            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if (result != null) {

                if (elementData != null) {
                    textView.resourceCategoryId = elementData.graphic!!.categoryId
                    textView.resourceId = elementData.graphic!!.id
                    textView.resourceName = elementData.graphic!!.name
                    textView.resourceUrl = elementData.graphic!!.imgUrl
                    textView.colorStringName = ""
                    textView.img = elementData.graphic!!.name
                    textView.isReset = true
                }
                textView.applyMask(result, textView.resourceName, byUser = true)
            }
        }
    }

    fun updateTextWidthHeight(elementView: View?) {
        when (elementView) {
            is CustomTextView -> {
                var oldWidth = (elementView as CustomTextView).layoutParams.width
                if (oldWidth == -2 || oldWidth == -1) oldWidth =
                    ((elementView as CustomTextView).minWidth * (elementView as CustomTextView).scaleX).roundToInt()

                val maxWidth =
                    (elementView as CustomTextView).minMaxLengthWords((elementView as CustomTextView).text.toString())
                if (maxWidth < oldWidth) {
                    (elementView as CustomTextView).layoutParams.width = maxWidth.roundToInt()
                    (elementView as CustomTextView).updateScaleSize()
                } else (elementView as CustomTextView).updateScaleSize()
                (elementView as CustomTextView).viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        (elementView as CustomTextView).viewTreeObserver.removeOnGlobalLayoutListener(
                                this)
                        showSelectionUIForView((elementView as CustomTextView), true)
                    }
                })
            }
        }
    }

    fun updateBackgroundBorderUI(colorName: String = "") {
        binding.viewWhiteBorder.isVisible = colorName.isNotEmpty() && (colorName.equals("#FFFFFF",
                ignoreCase = true) || colorName.equals("#FFFFFFFF",
                ignoreCase = true) || colorName.equals("#00000000", ignoreCase = true))
    }

    private fun commonTextViewFunctions(textView: CustomTextView) {
        textView.setOnSelectListener(object : IStickerOperation {
            override fun onEdit(tag: String, isVisible: Boolean) {
                if (tag == textView.id.toString()) {
                    showSelectionUIForView(textView, isVisible)
                }
            }

            override fun onSelect(tag: String) {
                if (tag == textView.id.toString()) {
                    var isSamePanel = true
                    if (currentSelectedViewType != textView.elementType) {
                        //TODO: De-Select View and Close related Panel
                        closeElementPanel(textView.elementType)
                        isSamePanel = false
                    }
                    disableSelectedElements()
                    updateAllElementPosition()
                    //TODO: Update Selection View
                    currentSelectedViewType = textView.elementType
                    currentSelectedViewId = textView.id

                    textView.setWrapImages(binding.leftWrap, binding.rightWrap)

                    //TODO: Show Selection Border UI
                    textView.select(true)

                    showSelectionUIForView(childView = textView, isVisible = true)
//                    checkUndoRedoView()
                    if (isSamePanel) {
                        if (currentPanelFragment != null) {
                            currentPanelFragment!!.updateColorSelection()
                            currentPanelFragment!!.updateTextSpacing()
                            currentPanelFragment!!.updateTextFormat()
                            currentPanelFragment!!.updateOpacity()
                            currentPanelFragment!!.updateOrderLayout()
                            currentPanelFragment!!.updateRotation()
                            currentPanelFragment!!.updateBackgroundCategorySelection()
                            currentPanelFragment!!.updateBackgroundContentSelection()
                        }

                        elementBottomPanelList.forEachIndexed { index, contentData ->
                            if (contentData.id == ELEMENT_ID_ALIGN) {
                                contentData.ratioImage = when (textView.textGravityIndex) {
                                    0 -> {
                                        R.drawable.align_left_selector
                                    }

                                    1 -> {
                                        R.drawable.align_center_selector
                                    }

                                    2 -> {
                                        R.drawable.align_right_selector
                                    }

                                    else -> {
                                        R.drawable.align_center_selector
                                    }
                                }
                                elementBottomPanelAdapter!!.notifyItemChanged(index)
                                isProjectHasChanges = true
                                return@forEachIndexed
                            }
                        }
                    } else {
                        //TODO: Panel
                        prepareElementBottomPanel()
                        binding.recyclerViewElementPanel.postDelayed({
                            AnimUtils.toggleSlide(true,
                                    binding.layoutParentEdit,
                                    binding.elementPanelLayout)
                        }, 20L)
                    }
                }
            }

            override fun onDelete(tag: String) {}

            override fun onDuplicate(tag: String) {}

            override fun onDrag(tag: String) {}

            override fun onDragEnd(tag: String) {}

            override fun onRotate(tag: String) {}

            override fun onScale(tag: String) {}

            var isTextEditScreenOpened: Boolean = false

            override fun onDoubleClick(tag: String) {
                if (currentSelectedViewId != -1) {
                    if (currentSelectedViewType == ElementType.TEXT) {
                        val elementView =
                            binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                    .singleOrNull()
                        openTextEditScreen(forEdit = true, elementView as CustomTextView)
                        elementView.postDelayed({ isTextEditScreenOpened = false }, 1200L)
                    }
                }
            }
        })

        textView.setEventCallbackListener(object : CustomTextView.EventCallbackListener {
            override fun onEventDetected(motionEvent: MotionEvent?) {
                if (motionEvent != null) {
                    updateTouchEvent(motionEvent)
                }
            }

            override fun onNewElementDetected(motionEvent: MotionEvent?) {
                if (motionEvent != null) updateElementSelectionFromOuterTouch(motionEvent)
            }
        })
    }

    private fun updateElementSelectionFromOuterTouch(motionEvent: MotionEvent) {
        var isElementDetected = false
        val rect = Rect()
        binding.editParentLayer.getGlobalVisibleRect(rect)
        if (rect.contains(motionEvent.rawX.roundToInt(), motionEvent.rawY.roundToInt())) {
            for (i in (binding.editParentLayer.childCount - 1) downTo 0) {
                val elementView = binding.editParentLayer.getChildAt(i)
                when (elementView) {

                    is CustomTextView -> {
                        if ((elementView.isLock == 0) && elementView.isMotionEventInsideView(
                                    motionEvent)) {
                            isElementDetected = true
                            if (elementView.id != currentSelectedViewId) {
                                var isSamePanel = true
                                disableSelectedElements()
                                if (currentSelectedViewType != elementView.elementType) {
                                    //TODO: De-Select View and Close related Panel
                                    closeElementPanel(elementView.elementType)
                                    isSamePanel = false
                                }
                                //TODO: Update Selection View
                                val pastViewSelectedId = currentSelectedViewId

                                currentSelectedViewType = elementView.elementType
                                currentSelectedViewId = elementView.id

                                //TODO: De-Select View and Close related Panel
                                if (pastViewSelectedId != -1) {
                                    val selectedView =
                                        binding.editParentLayer.children.filter { it.id == pastViewSelectedId }
                                                .singleOrNull()
                                    if (selectedView != null) selectedView!!.invalidate()
                                }

                                //TODO: Show Selection Border UI
                                elementView.select(true)

                                elementView.onTouchEvent(motionEvent)
                                elementView.invalidate()

                                if (isSamePanel) {
                                    if (currentPanelFragment != null) {
                                        currentPanelFragment!!.updateColorSelection()
                                        currentPanelFragment!!.updateTextSpacing()
                                        currentPanelFragment!!.updateTextFormat()
                                        currentPanelFragment!!.updateOpacity()
                                        currentPanelFragment!!.updateOrderLayout()
                                    }

                                    val bototmElementAlign =
                                        elementBottomPanelList.filter { it.id == ELEMENT_ID_ALIGN }
                                                .singleOrNull()
                                    bototmElementAlign!!.ratioImage =
                                        when (elementView.textGravityIndex) {
                                            0 -> {
                                                R.drawable.align_left_selector
                                            }

                                            1 -> {
                                                R.drawable.align_center_selector
                                            }

                                            2 -> {
                                                R.drawable.align_right_selector
                                            }

                                            else -> {
                                                R.drawable.align_center_selector
                                            }
                                        }
                                    elementBottomPanelAdapter!!.notifyItemChanged(
                                            elementBottomPanelList.indexOf(bototmElementAlign))
                                    isProjectHasChanges = true
                                } else {
                                    //TODO: Panel
                                    prepareElementBottomPanel()
                                    binding.recyclerViewElementPanel.postDelayed({
                                        AnimUtils.toggleSlide(true,
                                                binding.layoutParentEdit,
                                                binding.elementPanelLayout)
                                    }, 20L)
                                }
                            }
                            updateAllElementPosition()
                            break
                        }
                    }
                }
            }
        }
        if (!isElementDetected) {
            //TODO: De-Select View and Close related Panel
            currentSelectedViewId = -1
            currentSelectedViewType = ElementType.NONE
            disableSelectedElements()
            showSelectionUIForView(isVisible = false)
        }
    }

    private fun updateTouchEvent(motionEvent: MotionEvent) {
        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                when (currentSelectedViewType) {
                    ElementType.TEXT -> {
                        val elementView =
                            binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                    .singleOrNull()
                        if (elementView != null && motionEvent.pointerCount <= 2) (elementView as CustomTextView).isParentTouch =
                            true
                    }
                    else -> {}
                }
            }

            MotionEvent.ACTION_MOVE -> {}

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                when (currentSelectedViewType) {
                    ElementType.TEXT -> {
                        val elementView =
                            binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                                    .singleOrNull()
                        if (elementView != null) (elementView as CustomTextView).isParentTouch =
                            false
                    }

                    else -> {}
                }
            }
        }
        when (currentSelectedViewType) {
            ElementType.TEXT -> {
                val elementView =
                    binding.editParentLayer.children.filter { it.id == currentSelectedViewId }
                            .singleOrNull()
                if (elementView != null) (elementView as CustomTextView).onTouchEvent(motionEvent)
            }

            else -> {}
        }
    }

    private fun updateAllElementPosition() {
        elementPositionList = HashMap()
        binding.editParentLayer.forEach {
            when (it) {
                is CustomTextView -> {
                    //TODO: Text Element
                    val elementView = it
                    val hitRect = Rect()
                    elementView.getHitRect(hitRect)
                    elementPositionList[elementView.id] =
                        ElementPositionData(hitRect.left.toFloat(),
                                hitRect.top.toFloat(),
                                hitRect.right.toFloat(),
                                hitRect.bottom.toFloat(),
                                elementView.elementType,
                                elementView)
                }

                else -> {}
            }
        }
    }

    private fun getNextUniqueValue(): Int {
        return (System.currentTimeMillis() + atomicInteger.incrementAndGet()).toInt()
    }

    private fun duplicateTextView(oldTextView: CustomTextView) {
        try {
            val mViewWidth = oldTextView.width
            val mViewHeight = oldTextView.height
            val mViewLeft = oldTextView.x
            val mViewTop = oldTextView.y

            val textView = CustomTextView(mActivity,
                    binding.editParentLayer,
                    binding.viewBaseVertical,
                    binding.viewBaseHorizontal)
            textView.visibility = View.INVISIBLE
            textView.setWrapImages(binding.leftWrap, binding.rightWrap)

            textView.id = getNextUniqueValue()
            textView.tag = textView.id.toString()
            textView.mTag = textView.id.toString()
            textView.isSoundEffectsEnabled = false
            textView.text = oldTextView.text.toString()
            isProjectHasChanges = true
            textView.storedString = textView.text.toString()
            textView.textGravityIndex = oldTextView.textGravityIndex
            textView.fontName = oldTextView.fontName
            textView.isLock = oldTextView.isLock
            textView.updatedByUser = oldTextView.updatedByUser
            textView.resourceCategoryId = oldTextView.resourceCategoryId
            textView.resourceId = oldTextView.resourceId
            textView.resourceName = oldTextView.resourceName
            textView.resourceUrl = oldTextView.resourceUrl
            textView.colorStringName = oldTextView.colorStringName
            textView.img = oldTextView.img
            textView.isReset = oldTextView.isReset
            textView.flipX = oldTextView.flipX
            textView.flipY = oldTextView.flipY
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, oldTextView.textSize)

            textView.minWidth = mViewWidth
            val layoutParams1 = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams1.width = mViewWidth
            layoutParams1.gravity = Gravity.CENTER
            textView.x =
                ((mViewWidth / 2 - editorWidth / 2) + mViewLeft + Utils.dpToPx(15F)).toFloat()
            textView.y =
                ((mViewHeight / 2 - editorHeight / 2) + mViewTop + Utils.dpToPx(15F)).toFloat()
            textView.layoutParams = layoutParams1

            var fontFile = FileUtils.getFontFile(mActivity, oldTextView.fontName)
            textView.setFont(if (fontFile.isNotEmpty()) {
                Typeface.createFromFile((fontFile))
            } else {
                textView.fontName = "LibreBaskerville-Regular"
                fontFile = FileUtils.getFontFile(mActivity, "LibreBaskerville-Regular")
                Typeface.createFromFile((fontFile))
            }, true)
            if (oldTextView.mLineSpacing != 0F) {
                textView.applyLineSpacing(oldTextView.mLineSpacing)
            }
            if (oldTextView.letterSpacing != 0F) {
                textView.applyLetterSpacing(oldTextView.letterSpacing)
            }
            textView.changeCaseType(oldTextView.textCaseIndex, true)

            if (oldTextView.rotation != 0F) {
                textView.rotation = oldTextView.rotation
            }

            //TODO: Opacity
            textView.setElementAlpha(oldTextView.elementAlpha, true)

            if (oldTextView.maskImage.isNotEmpty()) {
                textView.applyColor(ColorUtils.correctColorName("#FFFFFF"), true)
                ApplyTextMaskFromProject(mActivity,
                        textView,
                        projectName,
                        oldTextView.maskImage,
                        binding.editParentLayer.height).execute()
            } else {
                textView.applyColor(ColorUtils.correctColorName(oldTextView.colorName), true)
            }

            textView.updateViewScale(oldTextView.scaleX)

            AnimUtils.viewFadeAnimation(binding.editParentLayer, textView)
            commonTextViewFunctions(textView)

            currentSelectedViewType = textView.elementType
            currentSelectedViewId = textView.id
            textView.setWrapImages(binding.leftWrap, binding.rightWrap)

            textView.viewTreeObserver.addOnGlobalLayoutListener(object :
                    ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    textView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    textView.visibility = View.VISIBLE
                    textView.select(true)
                    textView.visibleAll()
                    textView.visibleAllView()
                    showSelectionUIForView(textView, true)
                    updateAllElementPosition()

                    textView.postDelayed({
                        AnimUtils.toggleFade(true, binding.layoutParentEdit, textView)
                    }, 20L)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeFragment() {
        if (currentPanelFragment != null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            transaction.remove(currentPanelFragment!!)
            transaction.commit()
            currentPanelFragment = null
        }

        if (binding.colorPickerView.visibility == View.VISIBLE) {
            removeColorPickerView()
        }
    }

    private fun removeColorPickerView() {
        try {
            binding.colorPickerView.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openTextEditScreen(forEdit: Boolean = false, textView: CustomTextView? = null) {
        val dataIntent = Intent(mActivity, TextEditActivity::class.java)
        if (forEdit && textView != null) {
            dataIntent.putExtra("textString", textView.text.toString())
            dataIntent.putExtra("textGravity", textView.textGravityIndex)
            dataIntent.putExtra("textFontName", textView.fontName)
        }

        activityLauncher.launch(dataIntent,
                onActivityResult = object : BetterActivityResult.OnActivityResult<ActivityResult> {
                    override fun onActivityResult(result: ActivityResult) {
                        textView?.showHideBaseLine(View.GONE, View.GONE)
                        if (result.resultCode == Activity.RESULT_OK) {
                            val intentData: Intent? = result.data
                            if (intentData != null) {
                                //TODO: De-Select View and Close related Panel
                                disableSelectedElements()

                                binding.layoutParentEdit.postDelayed({
                                    val textString = intentData.extras!!.getString("textString", "")

                                    if (forEdit) {
                                        if (textView != null) {
                                            val oldString = textView.text.toString()
                                            var oldWidth = textView.layoutParams.width
                                            if (oldWidth == -2 || oldWidth == -1) oldWidth =
                                                (textView.minWidth * textView.scaleX).roundToInt()
                                            textView.text = textString
                                            isProjectHasChanges = true
                                            textView.storedString = textView.text!!.toString()
                                            textView.updatedByUser = true

                                            textView.visibleAll()
                                            textView.select(true)

                                            showSelectionUIForView(isVisible = false)
                                            val maxWidth = textView.minMaxLengthWords(textString)
                                            if ((maxWidth < oldWidth) || (oldString.length != textString.length)) {
                                                textView.layoutParams.width = maxWidth.roundToInt()
                                                textView.updateScaleSize()
                                            }
                                            textView.viewTreeObserver.addOnGlobalLayoutListener(
                                                    object :
                                                            ViewTreeObserver.OnGlobalLayoutListener {
                                                        override fun onGlobalLayout() {
                                                            textView.viewTreeObserver.removeOnGlobalLayoutListener(
                                                                    this)
                                                            showSelectionUIForView(textView, true)
                                                        }
                                                    })
                                        }
                                    } else {
                                        //TODO: Add TextG
                                        val elementData = ElementData()
                                        elementData.x = 0.0
                                        elementData.y = 0.0
                                        elementData.w = 0.0
                                        elementData.h = 0.0
                                        elementData.txt = textString
                                        elementData.clr = "#000000"
                                        elementData.img = ""
                                        elementData.agl = 0F
                                        elementData.ls = 0.0
                                        elementData.lh = 0.0
                                        elementData.fs = 100.0
                                        elementData.aln = 1
                                        elementData.lck = 0
                                        addTextView(elementData, fromUser = true)
                                    }
                                }, 240L)
                            }
                        }
                    }
                })
    }

    private fun disableSelectedElements() {
        binding.editParentLayer.children.toList().forEach {
            when (it) {
                is CustomTextView -> {
                    (it as CustomTextView).select(false)
                    (it as CustomTextView).changeTextFocus(false)
                }
            }
        }
        showSelectionUIForView(isVisible = false)
    }

    fun showSelectionUIForView(childView: View? = null, isVisible: Boolean = false) {
        try {
            binding.selectedViewBorderUI.isVisible = isVisible
            if (childView != null) {

                if (childView is CustomTextView) {
//                    binding.leftWrap.isVisible = true
//                    binding.rightWrap.isVisible = true
                    binding.imageViewLeftTopIcon.isVisible = false
                    binding.imageViewRightTopIcon.isVisible = false
                    binding.imageViewLeftBottomIcon.isVisible = false
                    binding.imageViewRightBottomIcon.isVisible = false
                    binding.imageViewLeftTopIconWhite.isVisible = false
                    binding.imageViewRightTopIconWhite.isVisible = false
                    binding.imageViewLeftBottomIconWhite.isVisible = false
                    binding.imageViewRightBottomIconWhite.isVisible = false

                    val viewHeight =
                        if ((childView.measuredHeight * childView.scaleY).roundToInt() > 65) (childView.measuredHeight * childView.scaleY).roundToInt()
                        else 65

                    val mLayoutParams =
                        FrameLayout.LayoutParams((childView.measuredWidth * childView.scaleX).roundToInt() + 16,
                                viewHeight + 16)
                    binding.selectedViewBorderUI.layoutParams = mLayoutParams
                    binding.selectedViewBorderUI.x =
                        binding.editParentWrapper.x + childView.x + (childView.width / 2) - (mLayoutParams.width / 2)
                    binding.selectedViewBorderUI.y =
                        binding.editParentWrapper.y + childView.y + (childView.height / 2) - (mLayoutParams.height / 2)
                    binding.selectedViewBorderUI.rotation = childView.rotation

                    //childView.iconSizeManage(childView.scaleX)
                } else {
                    binding.leftWrap.isVisible = false
                    binding.rightWrap.isVisible = false
                    binding.imageViewLeftTopIcon.isVisible = false
                    binding.imageViewRightTopIcon.isVisible = false
                    binding.imageViewLeftBottomIcon.isVisible = false
                    binding.imageViewRightBottomIcon.isVisible = false
                    binding.imageViewLeftTopIconWhite.isVisible = false
                    binding.imageViewRightTopIconWhite.isVisible = false
                    binding.imageViewLeftBottomIconWhite.isVisible = false
                    binding.imageViewRightBottomIconWhite.isVisible = false
                    val mLayoutParams =
                        FrameLayout.LayoutParams(childView.width + 16, childView.height + 16)
                    binding.selectedViewBorderUI.layoutParams = mLayoutParams
                    binding.selectedViewBorderUI.x = binding.editParentWrapper.x + childView.x - 8
                    binding.selectedViewBorderUI.y = binding.editParentWrapper.y + childView.y - 8
                    binding.selectedViewBorderUI.rotation = childView.rotation
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeElementPanel(newElementType: ElementType = ElementType.NONE) {
        removeFragment()
        if (elementBottomPanelAdapter != null) elementBottomPanelAdapter!!.setItemSelectionById()
        if (newElementType == ElementType.NONE) {
            AnimUtils.toggleSlide(false, binding.layoutParentEdit, binding.elementPanelLayout)
        }
        binding.layoutParentEdit.postDelayed({
            binding.layoutEditRootPanel.isVisible = true
        }, 80L)
    }

    override fun onDestroy() {
        try {
            FileUtils.deleteDirectory(FileUtils.getInternalTempDir(mActivity),
                    isParentDelete = false)
            FileUtils.deleteDirectory(FileUtils.getInternalDeletedDir(mActivity),
                    isParentDelete = false)
            binding.editParentLayer.removeAllViews()
            MyApplication.instance.freeMemory()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        job.cancel()
        super.onDestroy()
    }
}