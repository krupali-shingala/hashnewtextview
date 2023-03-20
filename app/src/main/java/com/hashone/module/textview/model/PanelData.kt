package com.hashone.module.textview.model

import com.hashone.commonutils.enums.ContentType
import java.io.File
import java.io.Serializable


data class DataResponse(
    var server_time: Long = 0L,
    var count: Int = 0,
    var status: Boolean = false,
    var home_header: ArrayList<ContentData> = ArrayList(),
    var categories: ArrayList<ContentData> = ArrayList(),
    var data: ArrayList<ContentData> = ArrayList()
) : Serializable

data class ContentData(
    var id: Int = 0,
    var name: String = "",
    var text_color: String = "",
    var bg_color: String = "",
    var color: Int = 0,
    var type_id: String = "",
    var tag: String = "",
    var subcategory: String = "",
    var type: String = "",
    var display_name: String = "",
    var scheduled: Int = 0,
    var status: Int = 0,
    var featured: Int = 0,
    var category_id: Int = 0,
    var subcategory_id: Int = 0,
    var contenttag_id: Int = 0,
    var primarygraphicscategory_id: Int = -1,
    var primarysubcategory_id: Int = 0,
    var secondarysubcategory_id: Int = 0,
    var graphicscategoryId: Int = 0,
    var graphicssubcategoryId: Int = 0,
    var paid: Int = 0,
    var pro: Int = 0,
    var lock: Int = 0,
    var size_id: Int = 0,
    var ratio: String = "",
    var appversion_id: Int = 0,
    var preview_image: FileData? = null,
    var featured_image: FileData? = null,
    var subcategories: ArrayList<ContentData> = ArrayList(),
    var zip_file: ZipFileData? = null,
    var font_file: ZipFileData? = null,
    var contentType: ContentType = ContentType.CONTENTS,
    var contentName: String = "",
    var contents: ArrayList<ContentData> = ArrayList(),

    //TODO: SizeData
    var ratioImage: Int = -1,
    var width: Int = 0,
    var height: Int = 0,
    var ratioWidth: Int = 0,
    var ratioHeight: Int = 0,
    var isSelected: Boolean = false,
    var enableSelectionMode: Boolean = true,
    var flipEnabled: Boolean = false,
    var orderEnabled: Boolean = false,

    //TODO: Settings
    var path: String = "",
    var link: String = "",
    var text: String = "",
    var scheduled_on: String = "",
    var featured_at: String = "",
    var sort: Int = -1,
    var app_ver: String = "",
    var force: Int = 0,
    var created_by: Int = -1,
    var image: Image? = null,
    var free: Int = 0,

    //TODO: Colors
    var colorHexString: String = "",
    var colorHexCode: Int = -1,
    //TODO: Fonts
    var fontcategory_id: Int = -1,
    var fontcategories: ContentData? = null,
    //TODO: Filters
    var filter_file: FileData? = null,
    var filtercategory_id: Int = -1,
    var filtercategories: ContentData? = null,
    //TODO: Content Fonts
    var fonts: ArrayList<ContentData> = arrayListOf(),
    //TODO: Backgrounds
    var backgroundcategories: ContentData? = null,
    var savedTemplateFile: File? = null,
    var primarybackgroundcategory_id: Int = -1,
    //TODO: Content Fonts
    var colors: ArrayList<String> = arrayListOf(),
    //TODO: Content Fonts
    var graphicscategories: ContentData? = null
) : Serializable

data class FileData(
    val name: String,
    val webp: String,
    val mimetype: String,
    val files: Files,
    val folder_path: String
) : Serializable

data class Files(
    val `50pc`: Px, val `128px`: Px, val original: Px
) : Serializable

data class Px(
    val height: Int, val size: Int, val width: Int
) : Serializable

data class ZipFileData(
    val name: String, val mimetype: String, val size: Long, val folder_path: String
) : Serializable

data class Image(
    val files: Files, val folder_path: String, val mimetype: String, val name: String
) : Serializable
