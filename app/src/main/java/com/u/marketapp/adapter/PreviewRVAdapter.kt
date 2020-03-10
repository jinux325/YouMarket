package com.u.marketapp.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.u.marketapp.R
import kotlinx.android.synthetic.main.layout_image_preview.view.*

class PreviewRVAdapter : RecyclerView.Adapter<PreviewRVAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var imageList: ArrayList<Uri> = ArrayList()

    interface ItemClick { fun onClick(view: View, position: Int) }
    var itemClick: ItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.layout_image_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageList[position], context)
        if (itemClick != null) holder.itemView.setOnClickListener { v -> itemClick?.onClick(v, position) }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    // 전체 조회
    fun getAllData(): ArrayList<Uri> {
        return imageList
    }

    // 전체 입력
    fun addAllData(newUriList: List<Uri>) {
        imageList.addAll(newUriList)
        notifyDataSetChanged()
    }

    // 입력
    fun addData(newUri: Uri) {
        imageList.add(newUri)
        notifyDataSetChanged()
    }

    // 삭제
    fun removeData(position: Int) {
        imageList.removeAt(position)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(image: Uri, context: Context) {
            Glide.with(context)
                .load(image)
                .thumbnail(0.1f)
                .error(R.drawable.ic_no_photo)
                .into(itemView.image_view_thumnail)
        }
    }
}