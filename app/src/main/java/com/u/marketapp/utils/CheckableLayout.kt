package com.u.marketapp.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.layout_dialog_item.view.*

class CheckableLayout: LinearLayout, Checkable {

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun isChecked(): Boolean {
        return check_box_dialog.isChecked
    }

    override fun toggle() {
        isChecked = check_box_dialog.isChecked.not()
    }

    override fun setChecked(checked: Boolean) {
        if (check_box_dialog.isChecked != checked) {
            check_box_dialog.isChecked = checked
        }
    }

}