package com.u.marketapp.adapter

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.u.marketapp.R
import java.util.*

@BindingAdapter("bindUrlArray")
fun bindViewFromUrlArray(view: ImageView, imageArray: ArrayList<String>?) {
    if (imageArray != null && imageArray.size > 0) {
        Glide.with(view.context)
            .load(imageArray[0])
            .thumbnail(0.1f)
            .error(R.drawable.ic_no_photo)
            .into(view)
    } else {
        Glide.with(view.context)
            .load(R.drawable.ic_no_photo)
            .thumbnail(0.1f)
            .into(view)
    }
}

@BindingAdapter("bindUrl")
fun bindViewFromUrl(view: ImageView, image: String?) {
    if (image != null) {
        Glide.with(view.context)
            .load(image)
            .thumbnail(0.1f)
            .error(R.drawable.ic_no_photo)
            .into(view)
    }
}

@BindingAdapter("bindSize")
fun bindViewFromSize(view: LinearLayout, size: Int?) {
    if (size != null && size > 0) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}