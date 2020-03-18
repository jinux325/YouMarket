package com.u.marketapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.MainActivity
import com.u.marketapp.R
import com.u.marketapp.chat.ChatActivity
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.vo.ChatRoomVO
import com.u.marketapp.vo.UserEntity
import kotlinx.android.synthetic.main.item_chatroom.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(val context: Context?, val chatList:MutableList<ChatRoomVO>, val chatUidList:MutableList<String>  ):
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val myUid = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun getItemCount(): Int = chatList.size

    @SuppressLint("LongLogTag")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if(myUid.equals(chatList[position].buyer)){
            chatList[position].buyer?.let { getName(it,holder) }
        }else{
            chatList[position].seller?.let { getName(it,holder) }
        }
        chatList[position].pid?.let { getProductImage(it, holder) }


        /*holder.time.text = chatList[position].registDate.toString()
        holder.comment.text = chatList[position].comment*/
        chatList[position].let { item ->
            with(holder) {
                time.text = item.registDate.toString()
                comment.text = item.comment
                cardView.setOnClickListener {
                    if(myUid.equals(chatList[position].buyer)){
                        chatList[position].buyer?.let { it1 -> chattingIntent(it1, chatUidList[position]) }
                    }else{
                        chatList[position].seller?.let { it1 -> chattingIntent(it1, chatUidList[position]) }
                    }
                }
            }
        }


        /*chatList[position].let { item ->
                with(holder) {
                    nickname.text = item.seller
                }
            }*/
        val day = SimpleDateFormat("a hh:mm")
        val year = SimpleDateFormat("yyyy년")
        if (chatList.size >= 1 && year.format(chatList[position].registDate) == year.format(
                Date(
                    System.currentTimeMillis()
                )
            )
        ) {
            val month = SimpleDateFormat("MM월 dd일")
            if (month.format(chatList[position].registDate) == month.format(
                    Date(
                        System.currentTimeMillis()
                    )
                )
            ) {
                val day_filter = SimpleDateFormat("hh")
                if (day_filter.format(chatList[position].registDate) == day_filter.format(
                        Date(
                            System.currentTimeMillis()
                        )
                    )
                ) {
                    val minute = SimpleDateFormat("mm")
                    if (minute.format(chatList[position].registDate) == minute.format(
                            Date(
                                System.currentTimeMillis()
                            )
                        )
                    ) {
                        val second = SimpleDateFormat("ss")
                        val s =
                            Integer.valueOf(second.format(Date(System.currentTimeMillis()))) - Integer.valueOf(
                                second.format(chatList[position].registDate)
                            )
                        holder.time.setText(s.toString() + "초 전")
                    } else {
                        val minutes =
                            Integer.valueOf(minute.format(Date(System.currentTimeMillis()))) - Integer.valueOf(
                                minute.format(chatList[position].registDate)
                            )
                        holder.time.setText(minutes.toString() + "분 전")
                    }
                } else {
                    holder.time.setText(day.format(chatList[position].registDate))
                }
            } else {
                holder.time.setText(month.format(chatList[position].registDate))
            }
        } else {
            if (chatList.size >= 1) holder.time.setText(year.format(chatList[position].registDate))
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

    fun getName(uid:String, holder:ViewHolder){
        db.collection("User").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    //name = userEntity?.name.toString()
                    Log.d("getName 1231 231 2  ", userEntity?.name+"    image:   "+ userEntity?.imgPath)
                    holder.nickname.text = userEntity?.name
                    if (context != null) {
                        Glide.with(context).load(userEntity?.imgPath)
                            .apply(RequestOptions.bitmapTransform(CircleCrop())).into(holder.image)
                    }
                }
            }
    }

    fun getProductImage(uid:String, holder:ViewHolder){
        db.collection("Product").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val productEntity: ProductEntity? = task.result!!.toObject<ProductEntity>(
                        ProductEntity::class.java)
                    if (context != null) {
                       // Glide.with(context).load(productEntity?.imageArray!![0]).into(holder.pImage)
                    }
                }
            }
    }

    fun chattingIntent(uid:String, chatRoomUid:String){
        db.collection("User").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("chatRoomUid", chatRoomUid)
                    intent.putExtra("name", userEntity?.name.toString())
                    context!!.startActivity(intent)
                }
            }
    }

}