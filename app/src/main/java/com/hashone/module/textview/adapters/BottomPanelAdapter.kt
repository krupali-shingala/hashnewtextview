package com.hashone.module.textview.adapters

import android.app.Activity
import android.provider.Settings.Panel
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hashone.commonutils.enums.ElementType
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_FLIPH
import com.hashone.commonutils.utils.Constants.ELEMENT_ID_TEXT_FLIPV
import com.hashone.commonutils.utils.Constants.ORDER_BACK
import com.hashone.commonutils.utils.Constants.ORDER_DOWN
import com.hashone.commonutils.utils.Constants.ORDER_FRONT
import com.hashone.commonutils.utils.Constants.ORDER_UP
import com.hashone.commonutils.utils.Constants.currentSelectedViewType
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.databinding.PanelItemMainBinding
import com.hashone.module.textview.interfaces.ItemCallback
import com.hashone.module.textview.model.ContentData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BottomPanelAdapter(
    private val activity: Activity,
    private val contentsList: ArrayList<ContentData>,
    private val padding: Int = 0,
    private val isMainPanel: Boolean = false
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mItemCallback: ItemCallback? = null
    var selectedIndex: Int = -1

    var itemPadding: Int = 0
    var viewMinWidth: Int = 0

    init {
        updateItemSpacing(padding)
    }

    fun updateItemSpacing(padding: Int = 0) {
        itemPadding = padding
        viewMinWidth = ((Utils.getScreenWidth(activity) - itemPadding) / 5F).roundToInt()
        viewMinWidth =
            if (contentsList.size < 5) {
                if (currentSelectedViewType == ElementType.BACKGROUND)
                    ((Utils.getScreenWidth(activity) - itemPadding) / 4.5F).roundToInt()
                else
                    ((Utils.getScreenWidth(activity) - (itemPadding)) / contentsList.size)
            } else {
                ((Utils.getScreenWidth(activity) - itemPadding) / 4.5F).roundToInt()
            }
    }

    override fun getItemCount(): Int = contentsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        PanelItemMainBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        with(holder as ItemViewHolder) {
            if (position != -1) {
                with(contentsList[position]) {
                    bind(this, position)
                }
            }
        }

    inner class ItemViewHolder(itemViewBinding: PanelItemMainBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        val binding = PanelItemMainBinding.bind(itemViewBinding.root)

        fun bind(contentData: ContentData, position: Int) {
            if (adapterPosition != -1) {
                binding.panelItemMainFrame.minimumWidth = viewMinWidth

                binding.root.isEnabled = true
                binding.root.isEnabled = true

                if (contentData.id == ELEMENT_ID_TEXT_FLIPH || contentData.id == ELEMENT_ID_TEXT_FLIPV) {
                    binding.panelItemImage.isSelected = contentData.flipEnabled
                    binding.panelItemText.isSelected = contentData.flipEnabled
                } else if (contentData.id == ORDER_DOWN || contentData.id == ORDER_BACK
                    || contentData.id == ORDER_UP || contentData.id == ORDER_FRONT
                ) {
                    binding.root.isEnabled = contentData.orderEnabled
                    binding.root.isEnabled = contentData.orderEnabled
                    if (contentData.orderEnabled) {
                        binding.panelItemImage.isSelected = contentData.isSelected
                        binding.panelItemText.isSelected = contentData.isSelected
                    }
                } else {
                    if (contentData.enableSelectionMode && position > 0) {
                        binding.panelItemImage.isSelected = contentData.isSelected
                        binding.panelItemText.isSelected = contentData.isSelected
                    } else {
                        binding.panelItemImage.isSelected = contentData.isSelected
                        binding.panelItemText.isSelected = contentData.isSelected
                    }
                }
                binding.panelItemImage.setImageResource(contentData.ratioImage)
                binding.panelItemText.text = contentData.name

                binding.root.setOnClickListener {
                    if (Utils.checkClickTime400()) {
                        GlobalScope.launch(Dispatchers.Main) {
                            binding.root.postDelayed({
                                if (mItemCallback != null)
                                    mItemCallback?.onItemClick(adapterPosition, contentData)

                                if (contentData.enableSelectionMode) {
                                    setItemSelection(adapterPosition)
                                } else {
                                    setItemSelection()
                                }
                            }, 240L)
                        }
                    }
                }
            }
        }
    }

    fun setItemSelection(index: Int = -1) {
        for (i in 0 until contentsList.size) {
            if (contentsList[i].isSelected) {
                contentsList[i].isSelected = false
                notifyItemChanged(i)
                break
            }
        }

        if (index != -1) {
            contentsList[index].isSelected = true
            notifyItemChanged(index)
        }
    }

    fun setItemSelectionById(itemId: Int = -1): Int {
        var selectedIndex = -1
        contentsList.forEachIndexed { index, contentData ->
            if (contentData.isSelected) {
                contentData.isSelected = false
                notifyItemChanged(index)
                return@forEachIndexed
            }
        }
        if (itemId != -1) {
            contentsList.forEachIndexed { index, contentData ->
                if (itemId == contentData.id) {
                    contentData.isSelected = true
                    notifyItemChanged(index)
                    selectedIndex = index
                    return@forEachIndexed
                }
            }
        }
        return selectedIndex
    }
}