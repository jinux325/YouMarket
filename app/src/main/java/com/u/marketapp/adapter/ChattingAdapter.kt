package com.u.marketapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.u.marketapp.R
import com.u.marketapp.vo.ChattingVO
import kotlinx.android.synthetic.main.item_chatting.view.*
import java.text.SimpleDateFormat

class ChattingAdapter(val context: Context?, private val chattingList:MutableList<ChattingVO>):
    RecyclerView.Adapter<ChattingAdapter.ViewHolder>() {

    private val myUid = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount(): Int =chattingList.size

    @SuppressLint("RtlHardcoded", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val date = SimpleDateFormat("a hh:mm")
        Log.e("chattingAdapter  ", chattingList[position].message +"  "+chattingList[position].image)
        Log.e("chattingAdapter if ", chattingList[position].uid +"   $myUid")
        if(chattingList[position].uid.equals(myUid)){
            holder.image.visibility = GONE
            holder.linearLayout.gravity = Gravity.RIGHT
            holder.leftTime.visibility = VISIBLE
            holder.leftTime.text = date.format(chattingList[position].registDate).toString()
            holder.rightTime.visibility = GONE

        }else{
            holder.image.visibility = VISIBLE
            holder.linearLayout.gravity = Gravity.LEFT
            holder.leftTime.visibility = GONE
            holder.rightTime.visibility = VISIBLE
            holder.rightTime.text = date.format(chattingList[position].registDate)
        }
        holder.message.text = chattingList[position].message
            Log.d("chattingAdapter 22 ", context.toString()+" "+chattingList[position].message +"  "+chattingList[position].image)
        Glide.with(context!!).load(chattingList[position].image)
            .apply(RequestOptions.bitmapTransform(CircleCrop())).into(holder.image)



    }

    inner class ViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_chatting,parent,false)) {
        val message = itemView.chatting_text!!
        val image = itemView.chatting_image!!
        val linearLayout = itemView.layout!!
       // val cardView = itemView.chatting_cardview
        val rightTime = itemView.chatting_time_right!!
        val leftTime = itemView.chatting_time_left!!
       // val dateLinear = itemView.linear_date_text
       // val dateTxt = itemView.date_text
    }
}