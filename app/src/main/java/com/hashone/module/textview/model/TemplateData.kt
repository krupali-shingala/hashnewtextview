package com.hashone.module.textview.model

import com.hashone.commonutils.utils.Constants
import com.hashone.textview.textviewnew.TextCaseType
import java.io.Serializable

class TemplateData : Serializable {
    var w: Int = 0
    var h: Int = 0
    var projectId: Int = 0
    var projectName: String = ""
    var projectFileName: String = ""
    var fileType: String = Constants.EXTENSION_PNG
    var data = ArrayList<ElementData>()
    var isBlank: Int = 0
}

class ElementData : Serializable {
    var ele: String = ""
    var idx: Int = 0
    var img: String = ""
    var clr: String = ""
    var agl: Float = 0F
    var lck: Int = 0
    var opa: Int = 100
    var x: Double = 0.0
    var y: Double = 0.0
    var w: Double = 0.0
    var h: Double = 0.0
    var flt: String = ""
    var txt: String = ""
    var fnt: String = ""
    var lh: Double = 0.0
    var ls: Double = 0.0
    var fs: Double = 0.0
    var aln: Int = 1

    //TODO: User Data
    var isPhoto: Int = 0
    var photoImg: String = ""
    var mskimg: String = ""

    var originalImg: String = ""

    var graphic: ResourceData? = null
    var filter: ResourceData? = null
    var background: ResourceData? = null
    var font: ResourceData? = null

    var scale: Float = 1F
    var flipH: Int = 0
    var flipV: Int = 0
    var translateX: Float = 0F
    var translateY: Float = 0F

    var format: String = ""
    var textCaseIndex: TextCaseType = TextCaseType.DEFAULT

    var jsonV2: Int = 0

    var cornerRadius: Float = 0F

    //TODO: Sticker Shadow
    var isStickerShadow: Int = 0
    var shadowColor: String = ""
    var shadowOpacity: Int = 0
    var shadowDistanceX: Float = 0F
    var shadowDistanceY: Float = 0F
    var shadowBlurRadius: Float = 0F
}

class ResourceData : Serializable {
    var categoryId: Int = -1
    var id: Int = -1
    var name: String = ""
    var imgUrl: String = ""
}