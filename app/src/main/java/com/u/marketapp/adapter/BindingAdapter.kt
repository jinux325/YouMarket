package com.u.marketapp.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.u.marketapp.R

@BindingAdapter("bindUrl")
fun bindViewFromUrl(view: ImageView, path: String) {
    if (!path.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(path)
            .thumbnail(0.1f)
            .error(R.drawable.ic_no_photo)
            .into(view)
    } else {
        Glide.with(view.context)
            .load(path)
            .thumbnail(0.1f)
            .error(R.drawable.ic_no_photo)
            .into(view)
    }
}