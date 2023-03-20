package com.hashone.module.textview.base

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.hashone.commonutils.checkinternet.config.NetworkConfig
import com.hashone.commonutils.checkinternet.listener.NetworkStateListener
import com.hashone.module.textview.retrofit.DownloadFile
import com.hashone.module.textview.retrofit.DownloadUnzip
import com.hashone.module.textview.retrofit.RetrofitHelper
import com.hashone.module.textview.R
import com.hashone.module.textview.databinding.DialogProgressLoadingBinding
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.ResponseBody
import retrofit2.Call

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity(), NetworkStateListener {

    lateinit var mActivity: Activity
    var networkConfig: NetworkConfig? = null
    var isInForeground = true
    var isAvailable = true
    var SubscribedSKU: String = ""
    var isSubscribed: Boolean = false
    var isNetworkAvailable = true
    var previousSate = true
    var fontDownloadNotCancel = true
    val retrofitHelper = RetrofitHelper()
    val apiHashMap = retrofitHelper.getFieldMap()
    var apiCall: Call<ResponseBody>? = null

    var downloadProgressDialog: Dialog? = null
    var downloadUnzip: DownloadUnzip? = null
    var downloadFont: DownloadFile? = null
    var dialogBinding: DialogProgressLoadingBinding? = null
    var alertDialog: AlertDialog? = null
    var isProjectRenderInProgress: Boolean = false

    val activityLauncher: BetterActivityResult<Intent, ActivityResult> =
        BetterActivityResult.registerActivityForResult(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this

//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        networkConfig = NetworkConfig.getInstance()
        networkConfig!!.addNetworkConnectivityListener(this)

        setNavigationBarButtonsColor(mActivity, Color.WHITE)

        MyApplication.instance.setMContext(mActivity)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }

    open fun setNavigationBarButtonsColor(activity: Activity, navigationBarColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView = activity.window.decorView
            var flags = decorView.systemUiVisibility
            flags = if (isColorLight(navigationBarColor)) {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
            decorView.systemUiVisibility = flags
        }
    }

    open fun isColorLight(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }


    var progressDialog: Dialog? = null
    fun prepareProgressDialog(message: String, isCancelable: Boolean = false) {
        try {
            progressDialog = Dialog(mActivity, R.style.TransparentDialog)
            progressDialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            progressDialog!!.setContentView(R.layout.dialog_progress_loading)
            progressDialog!!.setCancelable(isCancelable)
            progressDialog!!.setCanceledOnTouchOutside(isCancelable)
            (progressDialog!!.findViewById(R.id.textViewProgress) as AppCompatTextView).isVisible =
                message.isNotEmpty()
            (progressDialog!!.findViewById(R.id.textViewProgress) as AppCompatTextView).text =
                message

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
                                fontDownloadNotCancel = false
                                downloadUnzip!!.cancelDownload()
                                progressDialog!!.dismiss()
                            }
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
                if (!mActivity.isDestroyed) {
                    if (progressDialog != null && !progressDialog!!.isShowing) progressDialog!!.show()
                }


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

    fun prepareDownloadingProgress(text: String) {
        try {
            downloadProgressDialog = Dialog(mActivity)
            downloadProgressDialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            downloadProgressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogBinding = DialogProgressLoadingBinding.inflate(LayoutInflater.from(mActivity))
            downloadProgressDialog!!.setContentView(dialogBinding!!.root)
            downloadProgressDialog!!.setCancelable(true)

            dialogBinding!!.textViewProgress.visibility =
                if (text == "") ViewGroup.GONE else ViewGroup.VISIBLE
            dialogBinding!!.textViewProgress.text = text

            downloadProgressDialog!!.setOnKeyListener { dialogInterface, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (downloadUnzip != null) {
                        downloadUnzip!!.cancelDownload()
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
            }
            downloadProgressDialog!!.setOnDismissListener {
                if (downloadUnzip != null) {
                    downloadUnzip!!.cancelDownload()
                    downloadProgressDialog!!.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showDownloadProgress(showText: Boolean) {
        try {
            if (downloadProgressDialog != null && !downloadProgressDialog!!.isShowing) {
                downloadProgressDialog!!.show()
                (downloadProgressDialog!!.findViewById(R.id.textViewProgress) as AppCompatTextView).visibility =
                    if (showText) ViewGroup.VISIBLE else ViewGroup.GONE
                /* downloadProgressDialog!!.window!!.setFlags(
                     WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                     WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                 )
                 window!!.setFlags(
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
                    }
                    true
                }
                downloadProgressDialog!!.setOnCancelListener {
                    if (downloadUnzip != null) {
                        downloadUnzip!!.cancelDownload()
                        downloadProgressDialog!!.dismiss()
                    }
                }
                downloadProgressDialog!!.setOnDismissListener {
                    if (downloadUnzip != null) {
                        downloadUnzip!!.cancelDownload()
                        downloadProgressDialog!!.dismiss()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateProgressValue(progressValue: Int) {
        if (downloadProgressDialog != null && downloadProgressDialog!!.isShowing) {
        }
    }

    fun dismissProgress() {
        try {
            if (downloadProgressDialog != null && downloadProgressDialog!!.isShowing) {
                downloadProgressDialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                downloadProgressDialog!!.window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                window!!.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                downloadProgressDialog!!.dismiss()
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

    fun loadFragment(
        containerView: View, fragment: Fragment, bundle: Bundle? = null, isAdd: Boolean = false
    ) {
        try {
            if (bundle != null) fragment.arguments = bundle

            val beginTransaction = supportFragmentManager.beginTransaction()
            beginTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            beginTransaction.add(containerView.id, fragment)
            if (isAdd) {
                beginTransaction.addToBackStack(fragment.tag)
            }
            beginTransaction.commitNow()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}