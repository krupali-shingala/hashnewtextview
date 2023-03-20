package com.hashone.module.textview.utils

import com.hashone.module.textview.model.ContentData


object UrlUtils {

    //TODO: Contents
    fun getCategoryPreviewImage128px(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "128px/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getCategoryPreviewImage(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getContentPreviewImage50pc(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "50pc/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getContentPreviewImage128px(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "128px/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
//                "128px/" + dataBean.preview_image!!.name
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getContentPreviewImage(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
//                "" + dataBean.preview_image!!.name
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    //TODO: Fonts
    fun getFontPreviewImage50pc(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "50pc/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getFontsPreviewImage128px(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "128px/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    //TODO: Backgrounds
    fun getBackgroundCategoryPreviewImage50pc(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "50pc/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getBackgroundCategoryPreviewImage128px(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "128px/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getBackgroundPreviewImage128px(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "128px/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    //TODO: Filters
    fun getFiltersPreviewImage128px(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "128px/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    //TODO: Graphics
    fun getGraphicsPreviewImage128px(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path//.replace("cdn1", "test")
            val zipName =
                "128px/" + (if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp)
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getGraphicsPreviewImage(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path/*.replace("http:", "https:")*/
            val zipName =
                if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getFontsFontFile(dataBean: ContentData): String {
        return if (dataBean.font_file != null) {
            val folderPath = dataBean.font_file!!.folder_path/*.replace("http:", "https:")*/
            val zipName = dataBean.font_file!!.name
            (folderPath + "" + zipName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getBackgroundsPreviewImage(dataBean: ContentData): String {
        return if (dataBean.preview_image != null) {
            val folderPath = dataBean.preview_image!!.folder_path/*.replace("http:", "https:")*/
            val filterName =
                if (dataBean.preview_image!!.webp.isNullOrEmpty()) dataBean.preview_image!!.name else dataBean.preview_image!!.webp
            (folderPath + "" + filterName).replace("http:", "https:")
        } else {
            ""
        }
    }

    fun getContentZipFile(contentData: ContentData): String {
        return if (contentData.zip_file != null) {
            val folderPath = contentData.zip_file!!.folder_path.replace("http:", "https:")
            val zipName = contentData.zip_file!!.name
            folderPath + "" + zipName
        } else {
            ""
        }
    }

}