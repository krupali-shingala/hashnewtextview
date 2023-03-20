package com.hashone.module.textview.screenshot

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.hashone.commonutils.utils.BitmapUtils
import com.hashone.commonutils.utils.Constants
import com.hashone.module.textview.R
import com.hashone.module.textview.utils.AppUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ScreenshotUtils {

    fun loadBitmapFromView(
        view: View,
        projectWidth: Int,
        projectHeight: Int,
        fileType: String
    ): Bitmap? {
        // width measure spec
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            view.measuredWidth, View.MeasureSpec.EXACTLY
        )
        // height measure spec
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            view.measuredHeight, View.MeasureSpec.EXACTLY
        )
        //        // measure the view
        view.measure(widthSpec, heightSpec)
        // set the layout sizes
        view.layout(
            view.left,
            view.top,
            view.measuredWidth + view.left,
            view.measuredHeight + view.top
        )

        val widthRatio = projectWidth / view.width.toFloat()
        val heightRatio = projectHeight / view.height.toFloat()

        val bitmap = Bitmap.createBitmap(
            projectWidth,
            projectHeight,
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )

        return if (bitmap != null) {
            val canvas = Canvas(bitmap)
            canvas.translate(0f, 0f)
            if (fileType == Constants.EXTENSION_JPG) {
                canvas.drawColor(Color.WHITE, PorterDuff.Mode.ADD)
            } else {
                canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
            }
            canvas.scale(
                widthRatio,
                heightRatio
            )
            view.draw(canvas)
            bitmap
        } else {
            null
        }
    }

    fun loadBitmapFromView2(view: View): Bitmap {
        // width measure spec
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            view.measuredWidth, View.MeasureSpec.EXACTLY
        )
        // height measure spec
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            view.measuredHeight, View.MeasureSpec.EXACTLY
        )
        //        // measure the view
        view.measure(widthSpec, heightSpec)
        // set the layout sizes
        view.layout(
            view.left,
            view.top,
            view.measuredWidth + view.left,
            view.measuredHeight + view.top
        )

        val localWidth = view.width//(Constants.DARFT_GRID_PIXEL * gridColumn)
        val localHeight = view.height//(Constants.DARFT_GRID_PIXEL * gridRow)

        val widthRatio = 1F//localWidth / view.width.toFloat()
        val heightRatio = 1F//localHeight / view.height.toFloat()

        var bitmap = Bitmap.createBitmap(
            localWidth,
            localHeight,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)
        canvas.translate(0f, 0f)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
        canvas.scale(
            widthRatio,
            heightRatio
        )
        view.draw(canvas)

        return bitmap
    }

    /*  Create Directory where screenshot will save for sharing screenshot  */
    fun getMainDirectoryName(context: Context): File {
        //Here we will use getExternalFilesDir and inside that we will make our Demo folder
        //benefit of getExternalFilesDir is that whenever the app uninstalls the images will get deleted automatically.
        val mainDir = File(
            Environment.getExternalStorageDirectory(),
            "${Environment.DIRECTORY_PICTURES}/${context.getString(R.string.app_folder_name)}"
        )
        mainDir.setReadable(true)
        mainDir.setExecutable(true)
        mainDir.setWritable(true, false)
        //If File is not present create directory
        if (!mainDir.exists()) {
            mainDir.mkdirs()
            mainDir.mkdir()
        }
        return mainDir
    }

    /*  Store taken screenshot into above created path  */
    fun storeInInternal(
        context: Context,
        bm: Bitmap,
        fileName: String,
        saveFilePath: File,
        fileFormat: String,
        recycle: Boolean = false
    ): File {
        val dir = File(saveFilePath.absolutePath)
        dir.setReadable(true)
        dir.setExecutable(true)
        dir.setWritable(true, false)
        if (!dir.exists()) {
            dir.mkdirs()
            dir.mkdir()
        }
        val file = File(dir, fileName)
        if (file.exists()) {
            file.delete()
        }
        try {
            file.createNewFile()
            val fOut = FileOutputStream(file)
            bm.compress(
                if (fileFormat == Constants.EXTENSION_JPG) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG,
                BitmapUtils.getBitmapQuality(),
                fOut
            )
            fOut.flush()
            fOut.close()
            if (recycle) {
                if (!bm.isRecycled)
                    bm.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }

    fun saveProject(
        context: Context,
        bm: Bitmap,
        fileName: String,
        saveFilePath: File,
        fileFormat: String
    ): File {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.applicationContext.contentResolver

            val now = System.currentTimeMillis() / 1000

            val fileNameWithoutExtension = fileName.substring(0, fileName.indexOf("."))
            val fileExtension = fileName.substring(fileName.indexOf(".") + 1)

            val values = ContentValues().apply {
                put(MediaStore.Images.ImageColumns.DATE_ADDED, now)
                put(MediaStore.Images.ImageColumns.DATE_MODIFIED, now)
                put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName)
                put(MediaStore.Images.ImageColumns.TITLE, fileNameWithoutExtension)
                put(MediaStore.Images.ImageColumns.DESCRIPTION, "")
                put(
                    MediaStore.Images.ImageColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/${context.getString(R.string.app_folder_name)}"
                )
                put(
                    MediaStore.Images.ImageColumns.MIME_TYPE,
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
                )
                put(MediaStore.Images.ImageColumns.IS_PENDING, 1)
            }

            val pictureCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val alreadyExist = File(saveFilePath, fileName).exists()

            val pictureContentUri = if (!alreadyExist)
                resolver.insert(pictureCollection, values)!!
            else
                FileProvider.getUriForFile(
                    context,
                    "${AppUtils.getAppPackageName()}.provider",
                    File(saveFilePath, fileName)
                )

            resolver.openFileDescriptor(pictureContentUri, "rwt", null).use { it ->
                try {
                    it?.let {
                        val fos = FileOutputStream(it.fileDescriptor)
                        bm.compress(
                            if (fileFormat == Constants.EXTENSION_JPG) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG,
                            100,
                            fos
                        )
                        fos.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            if (!alreadyExist) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(pictureContentUri, values, null, null)
            }
            return File(saveFilePath, fileName)
        } else {
            val dir = File(saveFilePath.absolutePath)
            dir.setReadable(true)
            dir.setExecutable(true)
            dir.setWritable(true, false)
            if (!dir.exists()) {
                dir.mkdirs()
                dir.mkdir()
            }
            val file = File(dir, fileName)
            if (file.exists()) {
                file.delete()
            }
            try {
                file.setReadable(true)
                dir.setExecutable(true)
                file.setWritable(true, false)
                file.createNewFile()
                val fOut = FileOutputStream(file)
                bm.compress(
                    if (fileFormat == Constants.EXTENSION_JPG) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG,
                    100,
                    fOut
                )
                fOut.flush()
                fOut.close()

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.toString()), null
                ) { path, uri ->
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return file
        }
    }
}