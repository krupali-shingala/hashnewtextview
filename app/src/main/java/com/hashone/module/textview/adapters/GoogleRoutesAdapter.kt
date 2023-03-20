package com.hashone.module.textview.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hashone.commonutils.enums.ContentType
import com.hashone.commonutils.enums.DownloadState
import com.hashone.commonutils.utils.AnimUtils
import com.hashone.commonutils.utils.FileUtils
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.R
import com.hashone.module.textview.base.MyApplication
import com.hashone.module.textview.databinding.DividerItemBinding
import com.hashone.module.textview.databinding.FontItemBinding
import com.hashone.module.textview.interfaces.ItemCallback
import com.hashone.module.textview.model.ContentData
import com.hashone.module.textview.utils.AppFileUtils
import com.hashone.module.textview.utils.UrlUtils.getFontPreviewImage50pc
import com.hashone.module.textview.utils.UrlUtils.getFontsPreviewImage128px
import kotlin.math.roundToInt

class GoogleRoutesAdapter(
    private val mContext: Context, private val contentsList: ArrayList<ContentData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mItemCallback: ItemCallback? = null
    var selectedIndex: Int = -1
    var downloadProgress: Int = -1

    override fun getItemViewType(position: Int): Int {
        return if (contentsList[position].contentType == ContentType.DIVIDER) 1 else 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            DividerViewHolder(
                DividerItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            GoogleRouteViewHolder(
                FontItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GoogleRouteViewHolder) {
            val contentData = contentsList[position]

//            val ratio =
//                contentData.preview_image!!.files.original.width / contentData.preview_image!!.files.original.height.toDouble()
//
//            val constraintSet1 = ConstraintSet()
//            constraintSet1.clone(holder.binding.layoutFontItemSub)
//            constraintSet1.setDimensionRatio(holder.binding.imageViewFont.id, "W, $ratio:1")
//            constraintSet1.applyTo(holder.binding.layoutFontItemSub)
//
//            val constraintSet = ConstraintSet()
//            constraintSet.clone(holder.binding.layoutFontItemSub)
//            constraintSet.setDimensionRatio(holder.binding.imageViewFontStub.id, "W, $ratio:1")
//            constraintSet.applyTo(holder.binding.layoutFontItemSub)

            holder.binding.fontItemParentLayout.setBackgroundColor(
                ContextCompat.getColor(
                    mContext,
                    if (contentData.isSelected) R.color.extra_extra_light_gray else R.color.white
                )
            )
            holder.binding.progressBarFont.visibility = if (Utils.isNetworkAvailable(mContext)) {
                    if (contentData.isSelected && !AppFileUtils.isFontFileExit(
                            mContext, contentData
                        )
                    ) View.VISIBLE else View.GONE
            } else View.GONE

            val isPro = if (!MyApplication.instance.isPremiumVersion()) {
                contentData.free == 0
            } else {
                false
            }
            if (selectedIndex > -1 && selectedIndex == position) {
                if (AppFileUtils.isFontFileExit(
                        mContext, contentData
                    ) && (selectedIndex == position) && (holder.downloadState != DownloadState.DOWNLOAD_STARTED)
                ) {
                    if (holder.binding.progressBarFont.isVisible) AnimUtils.toggleFade(
                        false,
                        holder.binding.fontItemParentLayout,
                        holder.binding.progressBarFont
                    )
                    Handler().postDelayed({
                        holder.binding.imageViewSelectFont.setImageResource(R.drawable.ic_check)
                        AnimUtils.toggleFade(
                            true,
                            holder.binding.fontItemParentLayout,
                            holder.binding.imageViewSelectFont
                        )
                    }, 10L)
                }
            } else {
                if (isPro) {
                    holder.binding.imageViewSelectFont.visibility = View.VISIBLE
                    holder.binding.imageViewSelectFont.setImageResource(R.drawable.ic_pro_badge)
                } else {
                    holder.binding.progressBarFont.visibility = View.GONE
                    holder.binding.imageViewSelectFont.visibility = View.INVISIBLE
                }
            }
            val url =  getFontPreviewImage50pc(contentData)
            val url128px = getFontsPreviewImage128px(contentData)
            Glide.with(mContext).load(url128px)
                .thumbnail(
                    Glide.with(mContext).load(url128px)
                )
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .dontTransform()
//                        .override(Target.SIZE_ORIGINAL)
                        .placeholder(R.drawable.font_placeholder)
                        .error(R.drawable.font_placeholder)
                )
                .into(holder.binding.imageViewFont)
        }
    }

    override fun getItemCount(): Int {
        return contentsList.size
    }

    inner class GoogleRouteViewHolder(itemViewBinding: FontItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        val binding = FontItemBinding.bind(itemViewBinding.root)
        var downloadState: DownloadState = DownloadState.NONE

        init {
            itemView.setOnClickListener {
                if (Utils.checkClickTime400()) {
                    if (selectedIndex != adapterPosition) {
                        if (mItemCallback != null) mItemCallback?.onItemClick(
                            adapterPosition,
                            contentsList[adapterPosition]
                        )
                        val isPro = if (!MyApplication.instance.isPremiumVersion()) {
                            contentsList[adapterPosition].free == 0
                        } else {
                            false
                        }
                        if (!isPro) {
                            if (AppFileUtils.isFontFileExit(mContext, contentsList[adapterPosition])
                                || Utils.isNetworkAvailable(mContext)) {
                                setItemSelection(adapterPosition)
                            }
                        }
//                    notifyDataSetChanged()
                    }
                }
            }
        }
    }

    inner class DividerViewHolder(itemViewBinding: DividerItemBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root)

    fun setItemSelection(index: Int = -1) {
        for (i in 0 until contentsList.size) {
            if (contentsList[i].isSelected) {
                contentsList[i].isSelected = false
                break
            }
        }
        if (index != -1) {
            contentsList[index].isSelected = true
        }
        selectedIndex = index
        notifyItemRangeChanged(0, contentsList.size)
    }

    fun prepareDrawable(view: View): GradientDrawable {
        val shape = GradientDrawable()
        shape.setSize(view.width, Utils.dpToPx(8F).roundToInt())
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadius = 28F
        shape.setColor(Color.parseColor("#efefef"))
       return shape
    }
}