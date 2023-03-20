package com.hashone.module.textview.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.hashone.commonutils.enums.ContentType
import com.hashone.commonutils.enums.DownloadState
import com.hashone.commonutils.utils.AnimUtils
import com.hashone.commonutils.utils.Constants
import com.hashone.commonutils.utils.FileUtils
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.R
import com.hashone.module.textview.adapters.GoogleRoutesAdapter
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.databinding.FragmentFontsBinding
import com.hashone.module.textview.interfaces.ItemCallback
import com.hashone.module.textview.model.ContentData
import com.hashone.module.textview.model.DataResponse
import com.hashone.module.textview.retrofit.DownloadFile
import com.hashone.module.textview.utils.AppConstants
import com.hashone.module.textview.utils.AppFileUtils
import com.hashone.module.textview.utils.GoogleBottomSheet
import com.hashone.module.textview.views.snappysmoothscroller.SnapType
import com.hashone.module.textview.views.snappysmoothscroller.SnappyLayoutManager
import com.hashone.module.textview.views.snappysmoothscroller.SnappyLinearLayoutManager

class FontsFragment : BaseViewStubFragment(), DownloadFile.DownloadListener {

    lateinit var binding: FragmentFontsBinding

    private var fontCategoryData: ContentData? = null
    private var fontsAdapter: GoogleRoutesAdapter? = null
    private var allFontList: ArrayList<ContentData> = ArrayList()
    private var fontsList = ArrayList<ContentData>()
    private var pageIndex = 0
    private var fontSelectedPosition = -1

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == AppConstants.ACTION_PREMIUM_PURCHASED) {
                if (fontsAdapter != null) {
                    if (fontsList.size > 0) {
                        fontsAdapter!!.notifyItemRangeChanged(0, fontsList.size)
                    }
                }
            } else if (intent != null && intent.action == AppConstants.ACTION_UPDATE_FONT_SELECTION) {
                val fontData = intent.extras!!.getSerializable("data") as ContentData?
                val fontName = intent.extras!!.getString("fontName", "")
                selectTextFont(fontData, fontName)
            } else if (intent != null && intent.action == "UPDATE_VIEW_SIZE") {
                val paddingValue = intent.extras!!.getInt("padding", 0)
               // binding.fontListRecyclerView.setPadding(0, 0, 0, paddingValue)
            }
        }
    }

    private var fontUIView: GoogleBottomSheet? = null
    fun setFontUIView(fontUIView: GoogleBottomSheet) {
        this.fontUIView = fontUIView
    }

    companion object {
        fun newInstance(index: Int, data: ContentData?): FontsFragment {
            val args = Bundle()
            args.putInt("index", index)
            if (data != null) args.putSerializable("data", data)
            val fragment = FontsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateViewAfterViewStubInflated(
        inflatedView: View, savedInstanceState: Bundle?
    ) {
        binding = FragmentFontsBinding.bind(inflatedView)

        pageIndex = requireArguments().getInt("index")
        if (pageIndex != 0) fontCategoryData =
            requireArguments().getSerializable("data") as ContentData

        val fontData: DataResponse? =
            MyApplication.instance.resourcesResponseData.getDataResponse(Constants.RESPONSE_FONTS)
        if (fontData != null) {
            if (pageIndex == 0) {
                allFontList = ArrayList()
                allFontList = fontData.data

                fontsList.clear()
                fontsList.addAll(allFontList)
            } else {
                val localFonts = fontData.data
                fontsList.clear()
                localFonts.forEach {
                    if (it.fontcategory_id == fontCategoryData!!.id) {
                        fontsList.add(it)
                    }
                }
                fontsList.sortBy {
                    return@sortBy if (it.free == 1) 0 else 1
                }
            }
        }

        setAdapter()

        if (fontsAdapter != null) {

            binding.fontListRecyclerView!!.post {
                if (fontUIView != null) {
                    selectTextFont(fontUIView!!.selectedFontData, fontUIView!!.selectedFontName)
                }
            }
        }

        if (fontUIView != null) {
            filterProjectFonts(fontUIView!!.projectsFonts)

            selectTextFont(fontUIView!!.selectedFontData, fontUIView!!.selectedFontName)
        }

        mActivity.runOnUiThread {
            val intentFilter = IntentFilter()
            intentFilter.addAction(AppConstants.ACTION_PREMIUM_PURCHASED)
            intentFilter.addAction(AppConstants.ACTION_UPDATE_FONT_SELECTION)
            intentFilter.addAction("UPDATE_VIEW_SIZE")
            mActivity.registerReceiver(broadcastReceiver, intentFilter)
            isRegistered = true
            downloadFile = DownloadFile(
                mActivity,
                ContentType.FONT,
                FileUtils.getInternalFontDir(mActivity).absolutePath
            )
            downloadFile!!.setDownloadListener(this)
        }
    }

    override fun getViewStubLayoutResource(): Int {
        return R.layout.fragment_fonts
    }

    private fun setAdapter() {
        if (binding.fontListRecyclerView != null) {

            val linearLayoutManager =
                SnappyLinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false)
            (linearLayoutManager as SnappyLayoutManager).setSnapType(SnapType.CENTER)
            (linearLayoutManager as SnappyLayoutManager).setSnapDuration(240)
            (linearLayoutManager as SnappyLayoutManager).setSnapInterpolator(
                DecelerateInterpolator()
            )
            binding.fontListRecyclerView!!.layoutManager = linearLayoutManager

            val animator: RecyclerView.ItemAnimator? = binding.fontListRecyclerView!!.itemAnimator
            if (animator != null && animator is SimpleItemAnimator) {
                animator.supportsChangeAnimations = false
            }
            binding.fontListRecyclerView!!.setHasFixedSize(true)

//            // Save state
//            val recyclerViewState =
//                binding.fontListRecyclerView!!.layoutManager!!.onSaveInstanceState()
//
//            // Restore state
//            binding.fontListRecyclerView!!.layoutManager!!.onRestoreInstanceState(recyclerViewState)

            fontsAdapter = GoogleRoutesAdapter(
                mActivity, fontsList
            )
            binding.fontListRecyclerView!!.adapter = fontsAdapter

            fontsAdapter!!.mItemCallback = object : ItemCallback {
                override fun onCategoryClick(position: Int, contentData: ContentData) {

                }

                override fun onHeaderContentClick(position: Int, contentData: ContentData) {

                }

                override fun onItemClick(position: Int, contentData: ContentData) {
                    val isPro = if (!MyApplication.instance.isPremiumVersion()) {
                        contentData.free == 0
                    } else {
                        false
                    }
                    if (isPro) {
//                        (context as EditActivity).activityLauncher.launch(
//                            Intent(context, ProActivity::class.java),
//                            onActivityResult = object :
//                                BetterActivityResult.OnActivityResult<ActivityResult> {
//                                override fun onActivityResult(result: ActivityResult) {
//                                    if (MyApplication.instance.isPremiumVersion())
//                                        (context as EditActivity).showPurchaseSuccessSnackBar()
//                                }
//                            }
//                        )
                    } else {
                        if (AppFileUtils.isFontFileExit(mActivity, contentData)) {
                            fontsAdapter!!.notifyItemChanged(position)
                            if (fontUIView != null) {
                                fontUIView!!.mItemCallback?.onItemClick(position, contentData)
                                fontUIView!!.selectedFontData = contentData
                                fontUIView!!.selectedFontName = contentData.name
                            }
                        } else {
                            if (Utils.isNetworkAvailable(mActivity)) {
                                if (fontUIView != null) {
                                    downloadFont(contentData, position)
                                }
                            } else {
                                Utils.showToast(mActivity, MyApplication.instance.getString(R.string.no_internet))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNetworkStatusChanged(isConnected: Boolean) {
        if (previousSate != isConnected) {
            previousSate = isConnected
            when (isConnected) {
                true -> netIsOn()
                false -> netIsOff()
            }
        }
    }

    private fun netIsOn() {

    }

    private fun netIsOff() {

    }

    override fun onDestroyView() {
        if (isRegistered)
            mActivity.unregisterReceiver(broadcastReceiver)
        super.onDestroyView()
    }

    fun downloadFont(contentData: ContentData, position: Int) {
        if (downloadFile != null) {
            downloadFile!!.cancelDownload()
        }
        fontSelectedPosition = position
        downloadFile = DownloadFile(
            mActivity,
            ContentType.FONT,
            FileUtils.getInternalFontDir(mActivity).absolutePath
        )
        downloadFile!!.setDownloadListener(this)
        downloadFile!!.downloadFile(contentData)
    }

    override fun downloadProgress(
        contentData: ContentData?,
        downloadState: DownloadState,
        progressValue: Int
    ) {
        val viewHolder = binding.fontListRecyclerView.findViewHolderForAdapterPosition(
            fontSelectedPosition
        )

        if (viewHolder != null)
            (viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).downloadState = downloadState

        when (downloadState) {
            DownloadState.NONE -> {

            }

            DownloadState.DOWNLOAD_STARTED -> {
                if (fontSelectedPosition == fontsAdapter!!.selectedIndex) {

                    fontsAdapter!!.downloadProgress = 0
                    val viewHolder =
                        binding.fontListRecyclerView.findViewHolderForAdapterPosition(
                            fontSelectedPosition
                        )
                    AnimUtils.toggleFade(
                        true,
                        (viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).binding.fontItemParentLayout,
                        (viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).binding.progressBarFont
                    )
                }
            }

            DownloadState.DOWNLOAD_PROGRESS -> {
                if (fontSelectedPosition == fontsAdapter!!.selectedIndex) {
                    fontsAdapter!!.downloadProgress = progressValue
                    if (viewHolder != null) {
                        (viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).binding.progressBarFont.isVisible =
                            true
                    }
                }
            }

            DownloadState.DOWNLOAD_COMPLETED -> {
                if (fontSelectedPosition == fontsAdapter!!.selectedIndex) {

                    val viewHolder = binding.fontListRecyclerView.findViewHolderForAdapterPosition(
                        fontSelectedPosition
                    )
                    if (viewHolder != null) {
                        AnimUtils.toggleFade(
                            false,
                            (viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).binding.fontItemParentLayout,
                            (viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).binding.progressBarFont
                        )

                        Handler().postDelayed({
                            if (!(viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).binding.imageViewSelectFont.isVisible) {
                                viewHolder.binding.imageViewSelectFont.setImageResource(R.drawable.ic_check)
                                AnimUtils.toggleFade(
                                    true,
                                    (viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).binding.fontItemParentLayout,
                                    (viewHolder as GoogleRoutesAdapter.GoogleRouteViewHolder).binding.imageViewSelectFont
                                )
                            }
                        }, 20L)

                    } else {
                        if (fontsAdapter != null) {
                            fontsAdapter!!.notifyItemRangeChanged(0, fontsList.size)
                        }
                    }
                    if (contentData != null && fontUIView != null) {
                        fontUIView!!.mItemCallback?.onItemClick(fontSelectedPosition, contentData)
                        fontUIView!!.selectedFontData = contentData
                        fontUIView!!.selectedFontName = contentData.name
                    }
                }
            }

            DownloadState.ERROR -> {
                downloadFile?.cancelDownload()
            }

            else -> {}
        }
    }

    fun selectTextFont(fontData: ContentData?, fontName: String) {
        fontSelectedPosition = -1
        fontsAdapter!!.setItemSelection()
        fontsList.forEachIndexed { index, contentData ->
            if (fontData != null) {
                if (contentData.id == fontData.id) {
                    contentData.isSelected = true
                    if (fontUIView != null) {
                        fontUIView!!.selectedFontData = contentData
                        fontUIView!!.selectedFontName = contentData.name
                    }
                    fontSelectedPosition = index
                    fontsAdapter!!.selectedIndex = index
                    fontsAdapter!!.notifyItemChanged(index)
                    //     fontListRecyclerView!!.scrollToPosition(index)
                    binding.fontListRecyclerView!!.postDelayed({
                        binding.fontListRecyclerView!!.scrollToPosition(index)
                    }, 20L)
                    return@forEachIndexed
                }
            } else if (contentData.name == fontName) {
                contentData.isSelected = true
                if (fontUIView != null) {
                    fontUIView!!.selectedFontData = contentData
                    fontUIView!!.selectedFontName = contentData.name
                }
                fontSelectedPosition = index
                fontsAdapter!!.selectedIndex = index
                fontsAdapter!!.notifyItemChanged(index)
                //     fontListRecyclerView!!.scrollToPosition(index)
                binding.fontListRecyclerView!!.postDelayed({
                    //  fontListRecyclerView!!.smoothScrollToPosition(index)
                }, 20L)
                return@forEachIndexed
            }
        }
    }

    var isFilteredList: Boolean = false
    fun filterProjectFonts(projectFonts: ArrayList<String>) {
        if (pageIndex == 0) {
            if (!isFilteredList && projectFonts.isNotEmpty()) {
                val filterFonts = ArrayList<ContentData>()
                if (allFontList.isNotEmpty()) {
                    allFontList.forEachIndexed { index, contentData ->
                        if (projectFonts.contains(contentData.name)) {
                            filterFonts.add(contentData)
                        }
                    }
                }

//                allFontList.sortBy {
//                    return@sortBy if (it.free == 1) 0 else 1
//                }

                filterFonts.forEachIndexed { index, contentData ->
                    allFontList.remove(contentData)
                    allFontList.add(index, contentData)
                }
                allFontList.add(
                    filterFonts.size,
                    ContentData(id = -111, name = "divider", contentType = ContentType.DIVIDER)
                )
                isFilteredList = filterFonts.size > 0
            }
            fontsList.clear()
            fontsList.addAll(allFontList)
            if (fontsAdapter != null) {
                fontsAdapter!!.notifyDataSetChanged()
                binding.fontListRecyclerView!!.post {
                    binding.fontListRecyclerView!!.scrollToPosition(0)
                    if (fontUIView != null) {
                        selectTextFont(fontUIView!!.selectedFontData, fontUIView!!.selectedFontName)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
}