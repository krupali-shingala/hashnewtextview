package com.hashone.module.textview.interfaces

import com.hashone.module.textview.model.ContentData


interface FontUICallback {
    fun onSetSelectedData(position: Int, contentData: ContentData)
    fun onGetSelectedData()
    fun onSetSelectedName(position: Int, contentData: ContentData)
    fun onGetSelectedName()
    fun onItemClick(position: Int, contentData: ContentData)
}