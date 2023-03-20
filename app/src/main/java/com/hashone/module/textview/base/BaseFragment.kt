package com.hashone.module.textview.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.hashone.commonutils.checkinternet.config.NetworkConfig
import com.hashone.commonutils.checkinternet.listener.NetworkStateListener
import com.hashone.module.textview.retrofit.DownloadFile
import com.hashone.module.textview.retrofit.DownloadUnzip
import com.hashone.module.textview.retrofit.RetrofitHelper
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.R
import com.hashone.module.textview.databinding.DialogProgressLoadingBinding
import okhttp3.ResponseBody
import retrofit2.Call

open class BaseFragment : Fragment(), NetworkStateListener {

    var alertDialog: AlertDialog? = null
    lateinit var mActivity: Activity

    var previousSate = true

    var mLastClickTime = 0L

    var currentPage = 1
    var totalCount = 0
    var isLoadMoreEnabled = true
    var isLoading = false
    val retrofitHelper = RetrofitHelper()
    val apiHashMap = retrofitHelper.getFieldMap()
    var apiCall: Call<ResponseBody>? = null

    var downloadingDialogBinding: DialogProgressLoadingBinding? = null
    var downloadUnzip: DownloadUnzip? = null
    var downloadFile: DownloadFile? = null
    var fontDownloadNotCancel = true

    var isNetworkAvailable = true

    var loadingDialog: ProgressDialog? = null

    //    var progressDialog: ProgressDialog? = null
    var downloadProgressDialog: Dialog? = null
    var networkConfig: NetworkConfig? = null

    var isRegistered = false

    protected val activityLauncher: BetterActivityResult<Intent, ActivityResult> =
        BetterActivityResult.registerActivityForResult(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mActivity = getActivity() as Activity

        networkConfig = NetworkConfig.getInstance()
        networkConfig!!.addNetworkConnectivityListener(this)

        trackScreen()
//        LocaleHelper.setLocale(activity, storeUserData.getString(Constants.DEFAULT_LANGUAGE))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        MyApplication.instance.setMContext(context)
    }

    private fun trackScreen() {
        try {
            var trackString = "Templates screen"

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun wrapTabIndicatorToTitle(
        tabLayout: TabLayout,
        externalMargin: Int,
        internalMargin: Int
    ) {
        val tabStrip = tabLayout.getChildAt(0)
        if (tabStrip is ViewGroup) {
            val childCount = tabStrip.childCount
            for (i in 0 until childCount) {
                val tabView = tabStrip.getChildAt(i)

                tabView.minimumWidth = 0

                tabView.setPadding(0, tabView.paddingTop, 0, tabView.paddingBottom)

                if (tabView.layoutParams is MarginLayoutParams) {
                    val layoutParams =
                        tabView.layoutParams as MarginLayoutParams
                    if (i == 0) {
                        settingMargin(layoutParams, externalMargin, internalMargin)
                    } else if (i == childCount - 1) {
                        settingMargin(layoutParams, internalMargin, externalMargin)
                    } else {
                        settingMargin(layoutParams, internalMargin, internalMargin)
                    }
                }
            }
            tabLayout.requestLayout()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun settingMargin(
        layoutParams: MarginLayoutParams,
        start: Int,
        end: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layoutParams.marginStart = start
            layoutParams.marginEnd = end
        } else {
            layoutParams.leftMargin = start
            layoutParams.rightMargin = end
        }
    }

    fun changeTabsFont(tabLayout: TabLayout) {
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabsCount = vg.childCount
        for (j in 0 until tabsCount) {
            val vgTab = vg.getChildAt(j) as ViewGroup
            val tabChildsCount = vgTab.childCount
            for (i in 0 until tabChildsCount) {
                val tabViewChild = vgTab.getChildAt(i)
                if (tabViewChild is TextView
                ) {
                    tabViewChild.isAllCaps = false
                    tabViewChild.setTypeface(
                        ResourcesCompat.getFont(requireActivity(), R.font.roboto_medium),
                        Typeface.NORMAL
                    )
                    tabViewChild.textSize = Utils.dpToPx(16f)
                }
            }
        }
    }

    override fun onDestroyView() {
        if (mActivity != null) {
            if (networkConfig != null) {
                networkConfig!!.removeNetworkConnectivityListener(this)
            }
        }
        super.onDestroyView()
    }

    fun showDownloadProgress(showText: Boolean) {
        try {
            requireActivity().runOnUiThread {
                if (downloadProgressDialog != null && !downloadProgressDialog!!.isShowing) {
                    downloadProgressDialog!!.show()
                    (downloadProgressDialog!!.findViewById(R.id.textViewProgress) as AppCompatTextView).visibility =
                        if (showText) VISIBLE else GONE
                    /* downloadProgressDialog!!.window!!.setFlags(
                         WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                         WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                     )
                     requireActivity().window!!.setFlags(
                         WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                         WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                     )*/
//                downloadProgressDialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                    downloadProgressDialog!!.setOnKeyListener { dialogInterface, keyCode, keyEvent ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (downloadUnzip != null) {
                                downloadUnzip!!.cancelDownload()
                                downloadProgressDialog!!.dismiss()
                            }
                            if (downloadFile != null) {
                                downloadFile!!.cancelDownload()
                                downloadProgressDialog!!.dismiss()
                            }
                        }
                        true
                    }
                    downloadProgressDialog!!.setOnCancelListener {
                        if (downloadUnzip != null) {
                            downloadUnzip!!.cancelDownload()
                            downloadProgressDialog!!.dismiss()
                        }
                        if (downloadFile != null) {
                            downloadFile!!.cancelDownload()
                            downloadProgressDialog!!.dismiss()
                        }
                    }
                    downloadProgressDialog!!.setOnDismissListener {
                        if (downloadUnzip != null) {
                            downloadUnzip!!.cancelDownload()
                            downloadProgressDialog!!.dismiss()
                        }
                        if (downloadFile != null) {
                            downloadFile!!.cancelDownload()
                            downloadProgressDialog!!.dismiss()
                        }
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateDownloadProgressValue(progressValue: Int) {
        if (downloadProgressDialog != null && downloadProgressDialog!!.isShowing) {
        }
    }

    fun dismissDownloadProgress() {
        try {
            if (downloadProgressDialog != null && downloadProgressDialog!!.isShowing) {
                downloadProgressDialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                downloadProgressDialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                requireActivity().window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                requireActivity().window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                downloadProgressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun prepareDownloadingProgress(text: String) {
        try {
            downloadProgressDialog = Dialog(requireActivity())
            downloadProgressDialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            downloadProgressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            downloadingDialogBinding =
                DialogProgressLoadingBinding.inflate(LayoutInflater.from(mActivity))
            downloadProgressDialog!!.setContentView(downloadingDialogBinding!!.root)
            downloadProgressDialog!!.setCancelable(true)

            downloadingDialogBinding!!.textViewProgress.isVisible = text.isNotEmpty()
//            downloadingDialogBinding!!.textViewProgress.visibility = if (text == "") GONE else VISIBLE
            downloadingDialogBinding!!.textViewProgress.text = text

            downloadProgressDialog!!.setOnKeyListener { dialogInterface, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (downloadUnzip != null) {
                        downloadUnzip!!.cancelDownload()
                        downloadProgressDialog!!.dismiss()
                    }
                    if (downloadFile != null) {
                        downloadFile!!.cancelDownload()
                        downloadProgressDialog!!.dismiss()
                    }
                }
                true
            }
            downloadProgressDialog!!.setOnCancelListener {
                if (downloadUnzip != null) {
                    downloadUnzip!!.cancelDownload()
                    downloadProgressDialog!!.dismiss()
                }
                if (downloadFile != null) {
                    downloadFile!!.cancelDownload()
                    downloadProgressDialog!!.dismiss()
                }
            }
            downloadProgressDialog!!.setOnDismissListener {
                if (downloadUnzip != null) {
                    downloadUnzip!!.cancelDownload()
                    downloadProgressDialog!!.dismiss()
                }
                if (downloadFile != null) {
                    downloadFile!!.cancelDownload()
                    downloadProgressDialog!!.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    var progressDialog: Dialog? = null
    fun prepareProgressDialog(message: String, isCancelable: Boolean = false) {
        try {
            progressDialog = Dialog(mActivity, R.style.TransparentDialog)
            progressDialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            downloadingDialogBinding =
                DialogProgressLoadingBinding.inflate(LayoutInflater.from(mActivity))
            progressDialog!!.setContentView(downloadingDialogBinding!!.root)
            progressDialog!!.setCancelable(isCancelable)
            progressDialog!!.setCanceledOnTouchOutside(isCancelable)
            downloadingDialogBinding!!.textViewProgress.isVisible = message.isNotEmpty()
            downloadingDialogBinding!!.textViewProgress.text = message

            progressDialog!!.setOnKeyListener { dialogInterface, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (downloadUnzip != null) {
                        downloadUnzip!!.cancelDownload()
                    }
                    if (downloadFile != null) {
                        downloadFile!!.cancelDownload()
                    }
                }
                true
            }
            progressDialog!!.setOnCancelListener {
                if (downloadUnzip != null) {
                    downloadUnzip!!.cancelDownload()
                }
                if (downloadFile != null) {
                    downloadFile!!.cancelDownload()
                }
            }
            progressDialog!!.setOnDismissListener {
                if (downloadUnzip != null) {
                    downloadUnzip!!.cancelDownload()
                }
                if (downloadFile != null) {
                    downloadFile!!.cancelDownload()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showProgress(hasCallback: Boolean) {
        try {
            if (progressDialog != null && !progressDialog!!.isShowing) {
                if (hasCallback) {
                    progressDialog!!.setOnKeyListener { dialogInterface, keyCode, keyEvent ->
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (downloadUnzip != null) {
                                downloadUnzip!!.cancelDownload()
                            }
                            if (downloadFile != null) {
                                downloadFile!!.cancelDownload()
                            }
                            progressDialog!!.dismiss()
                            //activity.onBackPressed()
                        }
                        true
                    }
                    progressDialog!!.setOnCancelListener {
                        if (downloadUnzip != null && progressDialog != null) {
                            fontDownloadNotCancel = false
                            downloadUnzip!!.cancelDownload()
                            progressDialog!!.dismiss()

                        }
                    }
                    progressDialog!!.setOnDismissListener {
                        if (downloadUnzip != null && progressDialog != null) {
                            fontDownloadNotCancel = false
                            downloadUnzip!!.cancelDownload()
                            progressDialog!!.dismiss()
                        }
                    }
                }
                if (!mActivity.isDestroyed)
                    progressDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isProgressShown(): Boolean {
        return (progressDialog != null && progressDialog!!.isShowing)
    }

    fun isDownloadProgressShown(): Boolean {
        return (downloadProgressDialog != null && downloadProgressDialog!!.isShowing)
    }

    fun dismissProgress(isCancel: Boolean) {
        try {
            if (progressDialog != null && progressDialog!!.isShowing) {
                if (isCancel) {

                }
                progressDialog!!.dismiss()
                progressDialog = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onNetworkStatusChanged(isConnected: Boolean) {
        isNetworkAvailable = isConnected
    }

    override fun onNetworkSpeedChanged(speedType: Int) {
    }

    open fun clickEvents() {

    }
}