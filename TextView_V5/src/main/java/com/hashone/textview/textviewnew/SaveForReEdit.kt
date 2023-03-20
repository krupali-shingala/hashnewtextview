package com.hashone.textview.textviewnew

import androidx.core.view.forEach
import com.hashone.commonutils.utils.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SaveForReEdit {
//    private fun prepareProjectJson(savedProjectDir: File?) {
//        val projectJsonObject = JSONObject()
//
//        //TODO: Project Data
//        projectJsonObject.put("w", templateWidth)
//        projectJsonObject.put("h", templateHeight)
//        projectJsonObject.put("projectId", projectId)
//        projectJsonObject.put("projectName", projectName)
//        projectJsonObject.put("projectFileName", projectFileName)
//        projectJsonObject.put("isBlank", isBlankCanvas)
//
//        val elementDataJsonArray = JSONArray()
//
//        //TODO: Element Data
//        binding.editParentLayer.forEach {
//            when (it) {
//                is CustomTextView -> {
//                    //TODO: Text Element
//                    val elementView = it
//
//                    val elementJsonObject = JSONObject()
//                    elementJsonObject.put("ele", "txt")
//                    elementJsonObject.put("x",
//                            (templateWidth * elementView.x) / editorWidth.toDouble())
//                    elementJsonObject.put("y",
//                            (templateHeight * elementView.y) / editorHeight.toDouble())
//                    elementJsonObject.put("w",
//                            (templateWidth * (elementView.width)) / editorWidth.toDouble())
//                    elementJsonObject.put("h",
//                            (templateHeight * (elementView.height)) / editorHeight.toDouble())
//
//                    //TODO: Background
//                    if (elementView.resourceId != -1) {
//                        val filterJsonObject = JSONObject()
//                        filterJsonObject.put("categoryId", elementView.resourceCategoryId)
//                        filterJsonObject.put("id", elementView.resourceId)
//                        filterJsonObject.put("name", elementView.resourceName)
//                        filterJsonObject.put("imgUrl", elementView.resourceUrl)
//                        elementJsonObject.put("graphic", filterJsonObject)
//
//                        val backgroundFile =
//                            FileUtils.getBackgroundFile(mActivity, elementView.maskImage)
//                        if (backgroundFile != null && backgroundFile.exists()) FileUtils.moveFile(
//                                backgroundFile.absolutePath,
//                                savedProjectDir!!.absolutePath)
//                    }
//
//                    elementJsonObject.put("img", elementView.maskImage)
//                    elementJsonObject.put("clr", elementView.colorName)
//                    elementJsonObject.put("agl", elementView.rotation)
//                    elementJsonObject.put("lck", elementView.isLock)
//                    elementJsonObject.put("opa", elementView.elementAlpha)
//                    elementJsonObject.put("txt", elementView.text)
//                    elementJsonObject.put("fnt", elementView.fontName)
//                    elementJsonObject.put("lh",
//                            (templateWidth * (elementView.mLineSpacing)) / editorWidth.toDouble())
//                    elementJsonObject.put("ls",
//                            (templateWidth * (elementView.letterSpacing)) / editorWidth.toDouble())
//                    elementJsonObject.put("fs",
//                            (templateWidth * elementView.textSize) / editorWidth.toDouble())
//                    elementJsonObject.put("aln", elementView.textGravityIndex)
//
//                    //TODO: Local Data
//                    elementJsonObject.put("scale", elementView.scaleX)
//
//                    elementJsonObject.put("flipH", elementView.flipX)
//                    elementJsonObject.put("flipV", elementView.flipY)
//                    elementJsonObject.put("agl", elementView.rotation)
//
//                    //TODO: Font
//                    if (elementView.fontId != -1) {
//                        val filterJsonObject = JSONObject()
//                        filterJsonObject.put("categoryId", elementView.fontCategoryId)
//                        filterJsonObject.put("id", elementView.fontId)
//                        filterJsonObject.put("name", elementView.fontName)
//                        filterJsonObject.put("imgUrl", elementView.fontServerUrl)
//                        elementJsonObject.put("font", filterJsonObject)
//                    }
//                    val fontFile = File(FileUtils.getFontFile(mActivity, elementView.fontName))
//                    if (fontFile.exists()) FileUtils.moveFile(fontFile.absolutePath,
//                            savedProjectDir!!.absolutePath)
//
//                    elementJsonObject.put("jsonV2", 2)
//
//                    elementDataJsonArray.put(elementJsonObject)
//                }
//
//                else -> {}
//            }
//        }
//        projectJsonObject.put("data", elementDataJsonArray)
//
//        if (savedProjectDir != null) {
//            val jsonFile = savedProjectDir.absolutePath + "/" + projectName + ".json"
//            FileUtils.storeProjectJson(jsonFile, projectJsonObject.toString())
//        }
//    }
}