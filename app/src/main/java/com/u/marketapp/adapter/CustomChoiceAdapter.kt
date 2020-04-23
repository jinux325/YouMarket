package com.u.marketapp.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.databinding.DataBindingUtil
import com.u.marketapp.R
import com.u.marketapp.databinding.LayoutDialogItemBinding
import com.u.marketapp.entity.ListViewItem

class CustomChoiceAdapter(private val context: Activity, private val list: List<ListViewItem>) : BaseAdapter() {

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        var binding: LayoutDialogItemBinding
        var convertView = view
        if (convertView == null) {
            convertView = LayoutInflater.from(parent?.context).inflate(R.layout.layout_dialog_item, parent, false)
            binding = DataBindingUtil.bind(convertView)!!
            convertView.tag = binding
        } else {
            binding = convertView.tag as LayoutDialogItemBinding
        }

        binding.item = list[position]

//        val image = convertView?.findViewById<ImageView>(R.id.image_view_dialog_icon)
//        val text = convertView?.findViewById<TextView>(R.id.text_view_dialog_name)
//
//        text?.text = list[position].name
//
//        Glide.with(convertView?.context!!)
//            .load(list[position].icon)
//            .apply(RequestOptions.bitmapTransform(CircleCrop()))
//            .thumbnail(0.1f)
//            .error(R.drawable.ic_orange)
//            .into(image!!)

//        return convertView

        return binding.root
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}