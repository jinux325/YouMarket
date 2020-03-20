package com.u.marketapp.adapter

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
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
fun bindViewFromSize(view: LinearLayout, size: Int) {
    if (size > 0) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bindReplySize", "bindReplyLayout")
fun bindViewFromReplySize(view: LinearLayout, size: Int, isReply: Boolean) {
    if (!isReply && size > 0) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bindReply")
fun bindViewFromReply(view: ImageView, isReply: Boolean) {
    if (isReply) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bindReplyView")
fun bindViewFromReplyView(view: View, size: Int) {
    if (size > 0) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bindSuggestion")
fun bindViewFromSuggestion(view: MaterialTextView, suggestion: Boolean) {
    if (suggestion) {
        view.text = view.resources.getString(R.string.product_price_proposal)
        view.setTextColor(ContextCompat.getColor(view.context, R.color.hintcolor))
    } else {
        view.text = view.resources.getString(R.string.product_unable_to_offer_price)
        view.setTextColor(ContextCompat.getColor(view.context, R.color.colorGray))
    }
}