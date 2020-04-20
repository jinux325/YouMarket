package com.u.marketapp.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.u.marketapp.R
import com.u.marketapp.entity.ProductEntity
import java.util.*
import kotlin.collections.ArrayList

@BindingAdapter("bindUrlArray")
fun bindViewFromUrlArray(view: ImageView?, imageArray: ArrayList<String>?) {
    view?.let {
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
}

@BindingAdapter("bindUrl")
fun bindViewFromUrl(view: ImageView?, image: String?) {
    view?.let {
        if (image != null) {
            Glide.with(view.context)
                .load(image)
                .thumbnail(0.1f)
                .error(R.drawable.ic_no_photo)
                .into(view)
        }
    }
}

@BindingAdapter("bindSize")
fun bindViewFromSize(view: LinearLayout?, size: Int) {
    view?.let {
        if (size > 0) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }
}

@BindingAdapter("bindReplySize", "bindReplyLayout")
fun bindViewFromReplySize(view: LinearLayout?, size: Int, isReply: Boolean) {
    view?.let {
        if (!isReply && size > 0) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }
}

@BindingAdapter("bindReply")
fun bindViewFromReply(view: ImageView?, isReply: Boolean) {
    view?.let {
        if (isReply) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }
}

@BindingAdapter("bindReplyLayout")
fun bindViewFromReplyLayout(view: ConstraintLayout?, isReply: Boolean) {
    view?.let {
        if (isReply) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("bindReplyView")
fun bindViewFromReplyView(view: View?, size: Int) {
    if (size > 0) {
        view?.visibility = View.VISIBLE
    } else {
        view?.visibility = View.GONE
    }
}

@BindingAdapter("bindSuggestion")
fun bindViewFromSuggestion(view: MaterialTextView?, suggestion: Boolean) {
    if (suggestion) {
        view?.text = view?.resources?.getString(R.string.product_price_proposal)
        view?.setTextColor(ContextCompat.getColor(view?.context, R.color.hintcolor))
    } else {
        view?.text = view?.resources?.getString(R.string.product_unable_to_offer_price)
        view?.setTextColor(ContextCompat.getColor(view?.context, R.color.colorGray))
    }
}

@BindingAdapter("bindTrade")
fun bindViewFromTrade(view: MaterialTextView?, trade: Int) {
    view?.let {
        when (trade) {
            0 -> { // 거래중
                view.visibility = View.GONE
            }
            1 -> { // 예약중
                view.visibility = View.VISIBLE
                view.text = view.resources.getText(R.string.trade_1)
                view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.colorPrimary))
                view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            }
            2 -> { // 거래완료
                view.visibility = View.VISIBLE
                view.text = view.resources.getText(R.string.trade_2)
                view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.account_addressColor))
                view.setTextColor(ContextCompat.getColor(view.context, R.color.white))
            }
        }
    }
}

@BindingAdapter("bindDate")
fun bindViewFromDate(view: MaterialTextView?, item: ProductEntity?) {
    var time = System.currentTimeMillis() - (item?.regDate?.time ?: 0)
    val second = 1000L
    val minute = second * 60
    val hour = minute * 60
    val day = hour * 24
    val month = day * 30
    val year = month * 12

    val check = item?.regDate != item?.modDate
    var beforeStr = ""
    if (check) {
        beforeStr = "끌올 · "
        time = System.currentTimeMillis() - (item?.modDate?.time ?: 0)
    }

    view?.let {
        when {
            time < minute -> {
                val str = beforeStr + String.format(it.resources.getString(R.string.format_second), time/second)
                it.text = str
            }
            time < hour -> {
                val str = beforeStr + String.format(it.resources.getString(R.string.format_minute), time/minute)
                it.text = str
            }
            time < day -> {
                val str = beforeStr + String.format(it.resources.getString(R.string.format_hour), time/hour)
                it.text = str
            }
            time < month -> {
                val str = beforeStr + String.format(it.resources.getString(R.string.format_day), time/day)
                it.text = str
            }
            time < year -> {
                val str = beforeStr + String.format(it.resources.getString(R.string.format_month), time/month)
                it.text = str
            }
            else -> {
                val str = beforeStr + item?.modDate
                it.text = String.format(it.resources.getString(R.string.format_date), if (check) str else item?.regDate)
            }
        }
    }
}

@BindingAdapter("bindDate2")
fun bindViewFromDate(view: MaterialTextView?, date: Date?) {
    val time = System.currentTimeMillis() - (date?.time ?: 0)
    val second = 1000L
    val minute = second * 60
    val hour = minute * 60
    val day = hour * 24
    val month = day * 30
    val year = month * 12

    view?.let {
        when {
            time < minute -> {
                val str = String.format(it.resources.getString(R.string.format_second), time/second)
                it.text = str
            }
            time < hour -> {
                val str = String.format(it.resources.getString(R.string.format_minute), time/minute)
                it.text = str
            }
            time < day -> {
                val str = String.format(it.resources.getString(R.string.format_hour), time/hour)
                it.text = str
            }
            time < month -> {
                val str = String.format(it.resources.getString(R.string.format_day), time/day)
                it.text = str
            }
            time < year -> {
                val str = String.format(it.resources.getString(R.string.format_month), time/month)
                it.text = str
            }
            else -> {
                it.text = String.format(it.resources.getString(R.string.format_date), date)
            }
        }
    }
}

@BindingAdapter("bindTradeHistory")
fun bindViewFromTradeHistory(view: TextView?, trade: Int) {
    view?.let {
        when (trade) {
            0 -> { // 거래중
                view.text = "예약중으로 변경"
            }
            1 -> { // 예약중
                view.text = "판매중으로 변경"
            }
        }
    }
}