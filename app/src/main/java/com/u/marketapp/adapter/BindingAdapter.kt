package com.u.marketapp.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.u.marketapp.R
import java.util.*

@BindingAdapter("bindUrl")
fun bindViewFromUrlArray(view: ImageView, imageArray: ArrayList<String>) {
    if (!imageArray.isNullOrEmpty()) {
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

@BindingAdapter("bindImage")
fun bindViewFromUrl(view: ImageView, image: String?) {
    if (image != null) {
        Glide.with(view.context)
            .load(image)
            .thumbnail(0.1f)
            .error(R.drawable.ic_no_photo)
            .into(view)
    }
}