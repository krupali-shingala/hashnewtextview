package com.hashone.module.textview.adapters

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hashone.commonutils.enums.PanelType
import com.hashone.commonutils.utils.Constants.COLOR_ID_BLANK
import com.hashone.commonutils.utils.Constants.COLOR_ID_DROPPER
import com.hashone.commonutils.utils.Constants.FILTER_ID_NONE
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.R
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.databinding.DropperPanelItemBinding
import com.hashone.module.textview.databinding.ElementPanelItemBinding
import com.hashone.module.textview.databinding.NonePanelItemBinding
import com.hashone.module.textview.interfaces.ItemCallback
import com.hashone.module.textview.model.ContentData
import com.hashone.module.textview.utils.AppFileUtils
import com.hashone.module.textview.utils.UrlUtils
import kotlin.math.roundToInt

class ElementPanelAdapter(
    private val activity: Activity,
    private val contentsList: ArrayList<ContentData>,
    private val panelType: PanelType = PanelType.NONE
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mItemCallback: ItemCallback? = null

    override fun getItemCount(): Int = contentsList.size

    override fun getItemViewType(position: Int): Int {
        return contentsList[position].id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        COLOR_ID_DROPPER -> {
            DropperViewHolder(
                DropperPanelItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        FILTER_ID_NONE -> {
            NoneViewHolder(
                NonePanelItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        else -> {
            ItemViewHolder(
                ElementPanelItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        with(contentsList[position]) {
            when (holder) {
                is DropperViewHolder -> {
                    holder.bind(this)
                }

                is NoneViewHolder -> {
                    holder.bind(this)
                }

                else -> {
                    (holder as ItemViewHolder).bind(this)
                }
            }
        }

    inner class ItemViewHolder(itemViewBinding: ElementPanelItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        val binding = ElementPanelItemBinding.bind(itemViewBinding.root)

        fun bind(contentData: ContentData) {
            if (panelType == PanelType.COLORS || panelType == PanelType.STICKER_SHADOW || panelType == PanelType.BACKGROUND_FILL || panelType == PanelType.TEXT_MASK) {
                when (contentData.id) {
                    COLOR_ID_BLANK -> {
//                        binding.panelItemImageSelection.isVisible = false
                        binding.panelItemImage.setImageResource(R.drawable.ic_color_none)

                        if (contentData.isSelected) {
                            val paddingValue = Utils.dpToPx(4F).roundToInt()
                            binding.panelItemImage.setPadding(
                                paddingValue,
                                paddingValue,
                                paddingValue,
                                paddingValue
                            )
                            binding.panelItemImage.background = ResourcesCompat.getDrawable(
                                activity.resources,
                                R.drawable.ic_selection,
                                null
                            )
                        } else {
                            val paddingValue = Utils.dpToPx(0F).roundToInt()
                            binding.panelItemImage.setPadding(
                                paddingValue,
                                paddingValue,
                                paddingValue,
                                paddingValue
                            )
                            binding.panelItemImage.background = null
                        }
                    }

                    FILTER_ID_NONE -> {
                        binding.panelItemImage.setImageResource(R.drawable.none_selector)
                        binding.panelItemImage.isSelected = contentData.isSelected
//                        binding.panelItemImageSelection.isVisible = false
                        val paddingValue = Utils.dpToPx(0F).roundToInt()
                        binding.panelItemImage.setPadding(
                            paddingValue,
                            paddingValue,
                            paddingValue,
                            paddingValue
                        )
                        binding.panelItemImage.background = null
                    }

                    else -> {
                        if (contentData.colorHexString.equals(
                                "#FFFFFFFF", ignoreCase = true
                            )
                        ) {
                            val paddingValue =
                                if (contentData.isSelected) Utils.dpToPx(4F).roundToInt() else 0
                            binding.panelItemImageSelection.isVisible = true
                            binding.panelItemImageSelection.setImageResource(R.drawable.ic_ring_white)
                            binding.panelItemImageSelection.setPadding(
                                paddingValue,
                                paddingValue,
                                paddingValue,
                                paddingValue
                            )
                        } else {
                            binding.panelItemImageSelection.isVisible = false
                        }
                        binding.panelItemImage.setImageDrawable(circleColor(contentData.colorHexCode))
//                        binding.panelItemImageSelection.isVisible = contentData.isSelected
                        if (contentData.isSelected) {
                            val paddingValue = Utils.dpToPx(4F).roundToInt()
                            binding.panelItemImage.setPadding(
                                paddingValue,
                                paddingValue,
                                paddingValue,
                                paddingValue
                            )
                            binding.panelItemImage.background = ResourcesCompat.getDrawable(
                                activity.resources,
                                R.drawable.ic_selection,
                                null
                            )
                        } else {
                            val paddingValue = Utils.dpToPx(0F).roundToInt()
                            binding.panelItemImage.setPadding(
                                paddingValue,
                                paddingValue,
                                paddingValue,
                                paddingValue
                            )
                            binding.panelItemImage.background = null
                        }
                    }
                }
            }
            else if (panelType == PanelType.BACKGROUND_ELEMENTS) {
                Glide.with(binding.root.context)
                    .load(UrlUtils.getBackgroundPreviewImage128px(contentData))
//                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(
                        RequestOptions().placeholder(R.drawable.ic_graphics_ph).circleCrop()
                            .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                            .dontAnimate()
                    ).into(binding.panelItemImage)
//                binding.panelItemImageSelection.isVisible = contentData.isSelected

                if (contentData.isSelected) {
                    val paddingValue = Utils.dpToPx(4F).roundToInt()
                    binding.panelItemImage.setPadding(
                        paddingValue,
                        paddingValue,
                        paddingValue,
                        paddingValue
                    )
                    binding.panelItemImage.background = ResourcesCompat.getDrawable(
                        activity.resources,
                        R.drawable.ic_selection,
                        null
                    )
                } else {
                    val paddingValue = Utils.dpToPx(0F).roundToInt()
                    binding.panelItemImage.setPadding(
                        paddingValue,
                        paddingValue,
                        paddingValue,
                        paddingValue
                    )
                    binding.panelItemImage.background = null
                }

                binding.panelItemImagePro.isVisible =
                    if (!MyApplication.instance.isPremiumVersion()) {
                        contentData.free == 0
                    } else {
                        false
                    }

            }
            else if (panelType == PanelType.FILTER) {
                if (contentData.id == FILTER_ID_NONE) {
                    binding.panelItemImage.setImageResource(R.drawable.none_selector)
                    binding.panelItemImage.isSelected = contentData.isSelected
//                    binding.panelItemImageSelection.isVisible = contentData.isSelected

                    if (contentData.isSelected) {
                        val paddingValue = Utils.dpToPx(4F).roundToInt()
                        binding.panelItemImage.setPadding(
                            paddingValue,
                            paddingValue,
                            paddingValue,
                            paddingValue
                        )
                        binding.panelItemImage.background = ResourcesCompat.getDrawable(
                            activity.resources,
                            R.drawable.ic_selection,
                            null
                        )
                    } else {
                        val paddingValue = Utils.dpToPx(0F).roundToInt()
                        binding.panelItemImage.setPadding(
                            paddingValue,
                            paddingValue,
                            paddingValue,
                            paddingValue
                        )
                        binding.panelItemImage.background = null
                    }
                } else {
//                    binding.elementPanelItem.setPadding(Utils.dpToPx(6F).roundToInt())
                    Glide.with(binding.root.context)
                        .load(UrlUtils.getFiltersPreviewImage128px(contentData))
//                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(
                            RequestOptions().placeholder(R.drawable.ic_graphics_ph).circleCrop()
                                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                                .dontAnimate()
                        ).into(binding.panelItemImage)
//                    binding.panelItemImageSelection.isVisible = contentData.isSelected

                    if (contentData.isSelected) {
                        val paddingValue = Utils.dpToPx(4F).roundToInt()
                        binding.panelItemImage.setPadding(
                            paddingValue,
                            paddingValue,
                            paddingValue,
                            paddingValue
                        )
                        binding.panelItemImage.background = ResourcesCompat.getDrawable(
                            activity.resources,
                            R.drawable.ic_selection,
                            null
                        )
                    } else {
                        val paddingValue = Utils.dpToPx(0F).roundToInt()
                        binding.panelItemImage.setPadding(
                            paddingValue,
                            paddingValue,
                            paddingValue,
                            paddingValue
                        )
                        binding.panelItemImage.background = null
                    }

                    binding.panelItemImagePro.isVisible =
                        if (!MyApplication.instance.isPremiumVersion()) {
                            contentData.free == 0
                        } else {
                            false
                        }
                }
            }
            binding.root.setOnClickListener {
                if (Utils.checkClickTime600()) {
                    if (mItemCallback != null) mItemCallback?.onItemClick(adapterPosition, contentData)
                    val isPro =
                        if (!MyApplication.instance.isPremiumVersion() && !(panelType == PanelType.COLORS
                                    || panelType == PanelType.STICKER_SHADOW || panelType == PanelType.BACKGROUND_FILL)) {
                            contentData.free == 0
                        } else {
                            false
                        }
                    if (!isPro) setItemSelection(adapterPosition)
                }
            }
        }
    }

    inner class DropperViewHolder(itemViewBinding: DropperPanelItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        val binding = DropperPanelItemBinding.bind(itemViewBinding.root)

        fun bind(contentData: ContentData) {
            if (panelType == PanelType.COLORS || panelType == PanelType.STICKER_SHADOW || panelType == PanelType.BACKGROUND_FILL) {
                binding.panelItemImage.setBackgroundResource(R.drawable.ic_color_picker_prev)
                binding.panelItemImage.background.setTint(Color.parseColor(contentData.colorHexString.ifEmpty { "#efefef" }))
            }
            binding.root.setOnClickListener {
                if (Utils.checkClickTime600()) {
                    if (mItemCallback != null) mItemCallback?.onItemClick(
                        adapterPosition,
                        contentData
                    )
                    setItemSelection(adapterPosition)
                }
            }
        }
    }

    inner class NoneViewHolder(itemViewBinding: NonePanelItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        val binding = NonePanelItemBinding.bind(itemViewBinding.root)

        fun bind(contentData: ContentData) {
            if (panelType == PanelType.FILTER) {
                binding.panelItemImage.setImageResource(R.drawable.none_selector)
                binding.panelItemImage.isSelected = contentsList[adapterPosition].isSelected
//                if (contentsList[adapterPosition].isSelected) {
//                    binding.panelItemImage.background = ResourcesCompat.getDrawable(
//                        activity.resources, R.drawable.ic_selection, null
//                    )
//                } else {
                binding.panelItemImage.background = null
//                }
            } else if (panelType == PanelType.BACKGROUND_FILL || panelType == PanelType.COLORS || panelType == PanelType.STICKER_SHADOW) {
                binding.panelItemImage.setImageResource(R.drawable.none_selector)
                binding.panelItemImage.isSelected = contentsList[adapterPosition].isSelected
//                if (contentsList[adapterPosition].isSelected) {
//                    binding.panelItemImage.background = ResourcesCompat.getDrawable(
//                        activity.resources, R.drawable.ic_selection, null
//                    )
//                } else {
                binding.panelItemImage.background = null
//                }
            }
            binding.root.setOnClickListener {
                if (Utils.checkClickTime600()) {
                    if (mItemCallback != null) mItemCallback?.onItemClick(
                        adapterPosition,
                        contentData
                    )
                    setItemSelection(adapterPosition)
                }
            }
        }
    }

    fun setItemSelection(index: Int = -1) {
        var isSelected: Boolean = false
        if (index != -1) {
            when (panelType) {
                PanelType.BACKGROUND_ELEMENTS -> {
                    if (AppFileUtils.isBackgroundFileExit(activity, contentsList[index])) {
                        isSelected = true
                    }
                }

                PanelType.COLORS, PanelType.STICKER_SHADOW, PanelType.BACKGROUND_FILL, PanelType.PALETTE -> {
                    isSelected = true
                }

                else -> {

                }
            }
        }
        if (isSelected) {
            contentsList.forEachIndexed { mIndex, contentData ->
                if (contentData.isSelected) {
                    contentData.isSelected = false
                    notifyItemChanged(mIndex)
                    return@forEachIndexed
                }
            }
            for (i in 0 until contentsList.size) contentsList[i].isSelected = false
        }
        if (index != -1) {
            if (isSelected) {
                contentsList[index].isSelected = true
                notifyItemChanged(index)
            }
        }
    }

    var selectedPosition: Int = -1
    fun setColorSelection(colorName: String = ""): Int {
        var localColorName = colorName
        try {
            if (localColorName.isEmpty())
                localColorName = ""
            selectedPosition = -1
            for (i in 0 until contentsList.size) {
                if (localColorName.equals(contentsList[i].colorHexString, ignoreCase = true)) {
                    contentsList[i].isSelected = true
                    selectedPosition = i
                } else {
                    contentsList[i].isSelected = false
                }
            }
            notifyItemRangeChanged(0, contentsList.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return selectedPosition
    }

    fun setDropperColorSelection(colorName: String = ""): Int {
        try {
            selectedPosition = -1
            for (i in 0 until contentsList.size) {
                if ((contentsList[i].id) == COLOR_ID_DROPPER) {
                    contentsList[i].colorHexString = colorName
                    notifyItemChanged(i)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return selectedPosition
    }

    private fun circleColor(colorCode: Int): GradientDrawable {
        val shapeRing = GradientDrawable()
        shapeRing.shape = GradientDrawable.OVAL
        shapeRing.setColor(colorCode)
        return shapeRing
    }
}