package com.u.marketapp.adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.u.marketapp.R
import kotlinx.android.synthetic.main.layout_image.view.*

class ViewPagerAdapter() : PagerAdapter() {

    private val images: ArrayList<String> = ArrayList()

    interface ItemClick { fun onClick(view: View, position: Int) }
    var itemClick: ItemClick? = null

    // 리스트 데이터 추가
    fun addImageList(newList: List<String>) {
        images.addAll(newList)
        notifyDataSetChanged()
    }

    // 단일 데이터 추가
    fun addImage(newImage: String) {
        images.add(newImage)
        notifyDataSetChanged()
    }

    // 단일 데이터 제거
    fun removeImage(position: Int) {
        images.removeAt(position)
        notifyDataSetChanged()
    }

    // 조회
    fun getImage(position: Int): String {
        return images[position]
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val view = inflater.inflate(R.layout.layout_image, container, false)

        view.image_view_thumnail.setColorFilter(view.resources.getColor(R.color.colorFilter), PorterDuff.Mode.MULTIPLY)
        Glide.with(view.context)
            .load(images[position])
            .thumbnail(0.1f)
            .error(R.drawable.ic_no_photo)
            .into(view.image_view_thumnail)

        view.image_view_thumnail.setOnClickListener { v -> itemClick?.onClick(v, position) }
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View?)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return images.size
    }

}