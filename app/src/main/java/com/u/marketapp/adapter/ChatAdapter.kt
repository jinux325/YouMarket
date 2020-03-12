package com.u.marketapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.u.marketapp.R
import com.u.marketapp.vo.ChatRoomVO
import kotlinx.android.synthetic.main.item_chatroom.view.*

class ChatAdapter(val context: Context?, val chatList:MutableList<ChatRoomVO> ):
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount(): Int = chatList.size

    @SuppressLint("LongLogTag")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Log.d("!#@!@#!@# !@# ! #!@ #!  ", chatList[position].buyer)
        chatList[position].let { item ->
            with(holder) {
                nickname.text = item.buyer
            }
        }

    }

    inner class ViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chatroom,parent,false)) {
        val time = itemView.chat_time
        val pImage = itemView.product_iamge
        val cardView = itemView.chat_cardview
        val image = itemView.chat_image
        val comment = itemView.chat_comment
        val nickname = itemView.chat_nickname
    }
}