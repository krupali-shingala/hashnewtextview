package com.hashone.module.textview.interfaces

import com.hashone.module.textview.model.ContentData


interface FontItemCallback {
    fun onItemClick(position: Int, contentData: ContentData)
}