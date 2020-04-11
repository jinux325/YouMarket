package com.u.marketapp.vo

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class NoticeVO(
    val title:String="",
    val content:String="",
    val date: Date?=null
) : Parcelable