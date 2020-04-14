package com.u.marketapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.u.marketapp.R
import com.u.marketapp.setting.NoticeActivityReadPage
import com.u.marketapp.vo.NoticeVO
import kotlinx.android.synthetic.main.item_notice.view.*
import java.text.SimpleDateFormat

class NoticeAdapter (val context: Context?, private val noticeList:MutableList<NoticeVO>):
    RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount(): Int =noticeList.size

    @SuppressLint("SimpleDateFormat")
    val date = SimpleDateFormat("yyyy.MM.dd")

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text =  noticeList[position].title
        holder.date.text = date.format(noticeList[position].date)
        holder.noticeItem.setOnClickListener {
            val intent = Intent(context, NoticeActivityReadPage::class.java)
            intent.putExtra("title" , noticeList[position].title)
            intent.putExtra("content" , noticeList[position].content)
            intent.putExtra("date" , date.format(noticeList[position].date))
            context!!.startActivity(intent)
        }

    }

    inner class ViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_notice,parent,false)) {
        val title = itemView.notice_title!!
        val date = itemView.notice_date!!
        val noticeItem = itemView.notice_item!!
    }
}