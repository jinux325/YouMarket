package com.u.marketapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.u.marketapp.R
import com.u.marketapp.vo.ChatRoomVO
import com.u.marketapp.vo.ChattingVO
import kotlinx.android.synthetic.main.item_chatting.view.*

class ChattingAdapter(val context: Context?, val chattingList:MutableList<ChattingVO>):
    RecyclerView.Adapter<ChattingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount(): Int =chattingList.size

    override fun onBindViewHolder(holder: ChattingAdapter.ViewHolder, position: Int) {

        holder.message.text = chattingList[position].message

    }

    inner class ViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_chatting,parent,false)) {
        val message = itemView.chatting_text
        val image = itemView.chatting_image
        val linearLayout = itemView.layout
        val cardView = itemView.chatting_cardview
        val rightTime = itemView.chatting_time_right
        val leftTime = itemView.chatting_time_left
        val date_linear = itemView.linear_date_text
        val date_txt = itemView.date_text
    }
}