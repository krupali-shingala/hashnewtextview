package com.hashone.textview.textviewnew

interface IStickerOperation {
    fun onSelect(tag: String)
    fun onDoubleClick(tag: String)
    fun onDelete(tag: String)
    fun onDuplicate(tag: String)
    fun onRotate(tag: String)
    fun onScale(tag: String)
    fun onDrag(tag: String)
    fun onDragEnd(tag: String)

    fun onEdit(tag: String, isVisible: Boolean)
}