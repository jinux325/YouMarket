package com.u.marketapp.entity

import com.u.marketapp.R
import java.io.Serializable

data class ListViewItem (
    var uid: String,
    var icon: String,
    var name: String
) : Serializable {
    constructor() : this ("", "", "")
}