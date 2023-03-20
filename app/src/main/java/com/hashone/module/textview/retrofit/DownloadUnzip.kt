package com.hashone.module.textview.retrofit

import android.content.Context
import com.hashone.commonutils.enums.DownloadState
import com.hashone.commonutils.utils.FileUtils
import com.hashone.module.textview.base.CoroutineAsyncTask
import com.hashone.module.textview.model.ContentData
import com.hashone.module.textview.utils.AppFileUtils
import com.hashone.module.textview.utils.UrlUtils
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class DownloadUnzip {
    private var mDownloadListener: DownloadListener? = null
    private var mDirectory: String? = null
    private var mZipFile: String? = null

    private var contentData: ContentData? = null

    private var context: Context

    var mIsCancelled = false
    var isDownloading = false

    var totalFileSize: Long = 0L
    var currentFileSize: Long = 0L

    constructor(context: Context) {
        this.context = context
        mDirectory = FileUtils.getInternalContentDir(context).absolutePath
    }

    fun setDownloadListener(mDownloadListener: DownloadListener) {
        this.mDownloadListener = mDownloadListener
    }

    fun downloadFile(contentData: ContentData) {
        try {
            this.contentData = contentData
            val zipUrl = UrlUtils.getContentZipFile(this.contentData!!)
            val fileName = zipUrl.substring(zipUrl.lastIndexOf('/') + 1)
            mZipFile = "$mDirectory/$fileName"
            totalFileSize = contentData.zip_file!!.size
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

            mDownloadListener!!.downloadZipProgress(contentData, DownloadState.DOWNLOAD_STARTED)
        }

        override fun doInBackground(vararg params: String?): String {
            var count: Int
            try {
                val fileExist = AppFileUtils.isDirHasFiles(context, contentData!!)
                if (fileExist) {
                    publishProgress("" + 100)
                } else {
                    val retrofitHelper = RetrofitHelper()
                    val input = BufferedInputStream(
                        retrofitHelper.api().downloadFileByUrl(params[0].toString())
                            .execute().body()!!.byteStream(), 1024 * 4
                    )
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

                        publishProgress("" + (total * 100 / totalFileSize).toInt())
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
            mDownloadListener!!.downloadZipProgress(
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
                    val fileExist = AppFileUtils.isDirHasFiles(context, contentData!!)
                    if (fileExist) {
                        isDownloading = false
                        mIsCancelled = false

                        mDownloadListener!!.downloadZipProgress(
                            contentData,
                            DownloadState.DOWNLOAD_COMPLETED
                        )
                    } else {
                        UnZipTak().execute(mZipFile, mDirectory)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else if (result.equals("false1", ignoreCase = true)) {
                mDownloadListener!!.downloadZipProgress(contentData, DownloadState.DOWNLOAD_CANCELLED)
            } else {
                mDownloadListener!!.downloadZipProgress(contentData, DownloadState.ERROR)
            }
        }
    }

    interface DownloadListener {
        fun downloadZipProgress(
            contentData: ContentData?,
            downloadState: DownloadState,
            progressValue: Int = -1
        )
    }

    private inner class UnZipTak : CoroutineAsyncTask<String, Void, Boolean>() {

        override fun onPreExecute() {
            super.onPreExecute()
            mDownloadListener!!.downloadZipProgress(contentData, DownloadState.EXTRACT_STARTED)
        }

        override fun doInBackground(vararg params: String?): Boolean {
            val filePath = params[0]
            val destinationPath = params[1]
            val archive = File(filePath)
            try {
                val zipfile = ZipFile(archive)
                val e = zipfile.entries()

                if (mIsCancelled) {
                    isDownloading = false
                    mIsCancelled = true

                    if (File(filePath).exists()) {
                        File(filePath).delete()
                    }
                    if (File(destinationPath).exists()) {
                        File(destinationPath).delete()
                    }
                    return false
                }

                while (e.hasMoreElements()) {
                    val entry = e.nextElement() as ZipEntry
                    unzipEntry(zipfile, entry, destinationPath!!)
                }

                unZipFile(filePath!!, destinationPath!!)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            isDownloading = false
            mIsCancelled = false

            try {
                if (File(mZipFile!!).exists()) {
                    File(mZipFile!!).delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (AppFileUtils.isDirHasFiles(context, contentData!!)) {
                mDownloadListener!!.downloadZipProgress(contentData, DownloadState.EXTRACT_COMPLETED)
            } else {
                mDownloadListener!!.downloadZipProgress(contentData, DownloadState.ERROR)
            }
            super.onPostExecute(result)
        }
    }

    @Throws(IOException::class)
    private fun unzipEntry(
        zipfile: ZipFile, entry: ZipEntry,
        outputDir: String
    ) {
        if (!entry.isDirectory && !entry.name.contains("_")) {
            if (entry.isDirectory) {
                createDir(File(outputDir, entry.name))
                return
            }

            val outputFile = File(outputDir, entry.name)
            if (!outputFile.parentFile.exists()) {
                createDir(outputFile.parentFile)
            }

            val inputStream = BufferedInputStream(zipfile.getInputStream(entry))
            val outputStream = BufferedOutputStream(FileOutputStream(outputFile))
            try {
            } finally {
                outputStream.flush()
                outputStream.close()
                inputStream.close()
            }
        }
    }

    private fun createDir(dir: File) {
        if (dir.exists()) {
            return
        }
        if (!dir.mkdirs()) {
            throw RuntimeException("Can not create dir $dir")
        }
    }

    private fun unZipFile(zipFile: String, destLocation: String) {
        try {
            val f = File(destLocation)
            if (!f.isDirectory) {
                f.mkdirs()
                f.mkdir()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val zin = ZipInputStream(FileInputStream(zipFile))
        try {
            var ze: ZipEntry? = null

            while (run {
                    ze = zin.nextEntry
                    ze
                } != null) {
                val dir = File(destLocation)
                val path = dir.absolutePath + File.separator + ze!!.name

                val unzipFile = File(path)
                if (ze!!.isDirectory) {
                    if (!unzipFile.isDirectory) {
                        unzipFile.mkdirs()
                    }
                } else {
                    val canonicalPath = unzipFile.canonicalPath
                    if (canonicalPath.startsWith(dir.absolutePath)) {
                        val fout = FileOutputStream(path, false)

                        val bufout = BufferedOutputStream(fout)
                        val buffer = ByteArray(8096)
                        var read = 0
                        while (zin.read(buffer).also { read = it } != -1) {
                            bufout.write(buffer, 0, read)
                        }

                        zin.closeEntry()
                        bufout.close()
                        fout.close()

                        try {
                            var c = zin.read()
                            while (c != -1) {
                                fout.write(c)
                                c = zin.read()
                            }
                            zin.closeEntry()
                        } finally {
                            fout.close()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            zin.close()
        }
    }

}