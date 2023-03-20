package com.hashone.module.textview.interfaces

import com.hashone.module.textview.model.ContentData

interface ItemCallback {
    fun onHeaderContentClick(position: Int, contentData: ContentData)
    fun onCategoryClick(position: Int, contentData: ContentData)
    fun onItemClick(position: Int, contentData: ContentData)
}