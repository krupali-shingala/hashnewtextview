package com.hashone.module.textview.retrofit

import android.content.Context
import com.hashone.commonutils.enums.ContentType
import com.hashone.commonutils.enums.DownloadState
import com.hashone.module.textview.base.CoroutineAsyncTask
import com.hashone.module.textview.model.ContentData
import com.hashone.module.textview.utils.AppFileUtils
import com.hashone.module.textview.utils.UrlUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DownloadFile {
    private var mDownloadListener: DownloadListener? = null
    private var mDirectory: String? = null
    private var mZipFile: String? = null

    private var contentData: ContentData? = null

    private var context: Context
    private var contentType: ContentType

    var mIsCancelled = false
    var isDownloading = false

    constructor(context: Context, contentType: ContentType, dirPath: String) {
        this.context = context
        mDirectory = dirPath
        this.contentType = contentType
    }

    fun setDownloadListener(mDownloadListener: DownloadListener) {
        this.mDownloadListener = mDownloadListener
    }

    fun downloadFile(contentData: ContentData) {
        try {
            this.contentData = contentData
            val zipUrl = when (contentType) {
                ContentType.GRAPHIC -> {
                    UrlUtils.getGraphicsPreviewImage(this.contentData!!)
                }
                ContentType.FONT -> {
                    UrlUtils.getFontsFontFile(this.contentData!!)
                }
                ContentType.BACKGROUND, ContentType.TEXT_MASK -> {
                    UrlUtils.getBackgroundsPreviewImage(this.contentData!!)
                }
                else -> {
                    ""
                }
            }
            val mimeType = when (contentType) {
                ContentType.GRAPHIC -> {
                    contentData.preview_image!!.name
                }
                ContentType.FONT -> {
                    contentData.font_file!!.name
                }
                ContentType.FILTER -> {
                    contentData.filter_file!!.name
                }
                ContentType.BACKGROUND, ContentType.TEXT_MASK -> {
                    contentData.preview_image!!.webp.ifEmpty { contentData.preview_image!!.name }
                }
                else -> {
                    ""
                }
            }
            val extension = mimeType.substring(mimeType.indexOf(".") + 1)
            val fileName = "${contentData.name}.$extension"
            mZipFile = "$mDirectory/$fileName"
            DownloadTask().execute(zipUrl, mZipFile, mDirectory)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelDownload() {
        try {
            isDownloading = false
            mIsCancelled = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class DownloadTask : CoroutineAsyncTask<String, String, String>() {

        internal var result = ""

        override fun onPreExecute() {
            super.onPreExecute()

            isDownloading = true
            mIsCancelled = false

            mDownloadListener!!.downloadProgress(contentData, DownloadState.DOWNLOAD_STARTED)
        }

        override fun doInBackground(vararg params: String?): String {
            var count: Int
            try {
                val isFileExist = when (contentType) {
                     ContentType.FONT -> {
                        AppFileUtils.isFontFileExit(context, contentData!!)
                    }
                    ContentType.TEXT_MASK -> {
                        AppFileUtils.isBackgroundFileExit(context, contentData!!)
                    }
                    else -> {
                        false
                    }
                }
                if (isFileExist) {
                    publishProgress("" + 100)
                }
                else {
                    val retrofitHelper = RetrofitHelper()
                    val responseBody = retrofitHelper.api().downloadFileByUrl(params[0].toString())
                        .execute().body()!!
                    val lengthOfFile = responseBody.contentLength()
                    val input = BufferedInputStream(responseBody.byteStream(), 1024 * 4)
                    val output = FileOutputStream(params[1])
                    val data = ByteArray(1024)
                    var total: Long = 0
                    while (input.read(data).also { count = it } != -1) {
                        total += count.toLong()

                        if (mIsCancelled) {
                            output.flush()
                            output.close()
                            input.close()

                            isDownloading = false
                            mIsCancelled = true

                            if (File(params[1]).exists()) {
                                File(params[1]).delete()
                            }
                            return "false1"
                        }

                        publishProgress("" + (total * 100 / lengthOfFile).toInt())
                        output.write(data, 0, count)
                    }
                    output.close()
                    input.close()
                }
                result = "true"
            } catch (e: Exception) {
                e.printStackTrace()
                result = "false"
            }
            return result
        }

        override fun onProgressUpdate(vararg progress: String?) {
            super.onProgressUpdate(*progress)
            mDownloadListener!!.downloadProgress(
                contentData,
                DownloadState.DOWNLOAD_PROGRESS,
                progress[0]!!.toInt()
            )
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            isDownloading = false
            mIsCancelled = false
            if (result.equals("true", ignoreCase = true)) {
                try {
                    val isFileExist = when (contentType) {
                         ContentType.FONT -> {
                             AppFileUtils.isFontFileExit(context, contentData!!)
                        }
                        ContentType.TEXT_MASK -> {
                            AppFileUtils.isBackgroundFileExit(context, contentData!!)
                        }
                        else -> {
                            false
                        }
                    }
                    if (isFileExist) {
                        isDownloading = false
                        mIsCancelled = false
                        contentData!!.path = AppFileUtils.getStickerPath(context, contentData!!)

                        mDownloadListener!!.downloadProgress(
                            contentData,
                            DownloadState.DOWNLOAD_COMPLETED
                        )
                    } else {
                        mDownloadListener!!.downloadProgress(contentData, DownloadState.ERROR)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (result.equals("false1", ignoreCase = true)) {
                mDownloadListener!!.downloadProgress(contentData, DownloadState.DOWNLOAD_CANCELLED)
            } else {
                mDownloadListener!!.downloadProgress(contentData, DownloadState.ERROR)
            }
        }
    }

    interface DownloadListener {
        fun downloadProgress(
            contentData: ContentData?,
            downloadState: DownloadState,
            progressValue: Int = -1
        )
    }
}