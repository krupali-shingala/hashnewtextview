package com.hashone.module.textview.utils

import android.content.Context
import com.hashone.commonutils.utils.FileUtils
import com.hashone.commonutils.utils.FileUtils.getInternalBackgroundDir
import com.hashone.commonutils.utils.FileUtils.getInternalContentDir
import com.hashone.commonutils.utils.FileUtils.getInternalFontDir
import com.hashone.commonutils.utils.FileUtils.getInternalSavedDir
import com.hashone.commonutils.utils.FileUtils.getStickerDir
import com.hashone.module.textview.R
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.model.ContentData
import java.io.File

object AppFileUtils {

    fun createSavedProjectDir(
            context: Context, projectName: String, projectFileName: String
                             ): File {
        val rootDir = getInternalSavedDir(context)
        val imageDir = File(
                rootDir.absolutePath,
                "${MyApplication.instance.context!!.getString(R.string.app_folder_name)}_${projectFileName}_${projectName}_${System.currentTimeMillis()}"
                           )
        imageDir.setReadable(true)
        imageDir.setWritable(true, false)
        if (!imageDir.exists()) {
            imageDir.mkdirs()
            imageDir.mkdir()
        }
        return imageDir.canonicalFile
    }

    fun isBackgroundFileExit(context: Context, contentData: ContentData): Boolean {
        try {
            if (contentData.preview_image != null) {
                val mimeType =
                    contentData.preview_image!!.webp.ifEmpty { contentData.preview_image!!.name }
                val extension = mimeType.substring(mimeType.indexOf(".") + 1)
                val fileName = "${contentData.name}.$extension"
                val filePath = File(getInternalBackgroundDir(context), fileName)
                return filePath.exists()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getBackgroundFile(context: Context, contentData: ContentData): File {
        val mimeType = contentData.preview_image!!.webp.ifEmpty { contentData.preview_image!!.name }
        val extension = mimeType.substring(mimeType.indexOf(".") + 1)
        val fileName = "${contentData.name}.$extension"
        return File(getInternalBackgroundDir(context), fileName)
    }

    fun isFontFileExit(context: Context, contentData: ContentData): Boolean {
        try {
            val mimeType = contentData.font_file!!.name
            val extension = mimeType.substring(mimeType.indexOf(".") + 1)
            val fileName = "${contentData.name}.$extension"
            val filePath = File(getInternalFontDir(context), fileName)
            return filePath.exists()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getStickerPath(context: Context, dataBean: ContentData): String {
        try {
            val filePath1 = File(getStickerDir(context), "${dataBean.name}.webp")
            val filePath = File(getStickerDir(context), "${dataBean.name}.png")
            val filePath2 = File(getStickerDir(context), "${dataBean.name}.jpg")
            val filePath3 = File(getStickerDir(context), "${dataBean.name}.jpeg")
            val filePath4 = File(getStickerDir(context), "${dataBean.name}.gif")
            return when {
                filePath1.exists() -> filePath1.absolutePath
                filePath.exists() -> filePath.absolutePath
                filePath2.exists() -> filePath2.absolutePath
                filePath3.exists() -> filePath3.absolutePath
                filePath4.exists() -> filePath4.absolutePath
                else -> ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    fun isDirHasFiles(context: Context, contentData: ContentData): Boolean {
        try {
            val filePath = File(getInternalContentDir(context), contentData.name)
            return filePath.exists() && !filePath.listFiles().isNullOrEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun getFileFromProjectDir(
        context: Context,
        templateName: String,
        fileName: String,
        projectFolderName: String = "",
        fromMyProjects: Boolean = false
    ): String {
        if (fromMyProjects) {
            val filePath1 =
                File(FileUtils.getInternalTempDir(context), "$fileName.png")
            val filePath2 =
                File(FileUtils.getInternalTempDir(context), "$fileName.webp")
            val filePath3 =
                File(FileUtils.getInternalTempDir(context), "$fileName.jpg")
            val filePath4 =
                File(FileUtils.getInternalTempDir(context), "$fileName.jpeg")
            val filePath = when {
                filePath2.exists() -> filePath2
                filePath1.exists() -> filePath1
                filePath3.exists() -> filePath3
                filePath4.exists() -> filePath4
                else -> null
            }
            return if (filePath != null) filePath.absolutePath else ""
        } else {
            val filePath1 =
                File(FileUtils.getInternalContentDir(context), "$templateName/$fileName.png")
            val filePath2 =
                File(FileUtils.getInternalContentDir(context), "$templateName/$fileName.webp")
            val filePath3 =
                File(FileUtils.getInternalContentDir(context), "$templateName/$fileName.jpg")
            val filePath4 =
                File(FileUtils.getInternalContentDir(context), "$templateName/$fileName.jpeg")
            val filePath = when {
                filePath2.exists() -> filePath2
                filePath1.exists() -> filePath1
                filePath4.exists() -> filePath4
                else -> null
            }
            return if (filePath != null) filePath.absolutePath else ""
        }
    }

}