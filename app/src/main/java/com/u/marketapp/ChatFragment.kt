package com.u.marketapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.adapter.ChatAdapter
import com.u.marketapp.vo.ChatRoomVO
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.fragment_chat.*


class ChatFragment : Fragment() {

    private var chattingRoomList: MutableList<ChatRoomVO> = mutableListOf()
    private var chattingRoomUidList: MutableList<String> = mutableListOf()
    //private val db = FirebaseFirestore.getInstance()
    //private val myUid = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        (activity as AppCompatActivity).supportActionBar?.hide()
        //(activity as AppCompatActivity).setSupportActionBar(setting_toolbar)

      //  val chat_recyclerView = view.findViewById(R.id.chat_recyclerView) as RecyclerView
       // chattingRoomList = mutableListOf<ChatRoomVO>(ChatRoomVO("1","1", "","1","1","1","1","1"),ChatRoomVO("1","1", "","1","1","1","1","1"),ChatRoomVO("1","1", "","1","1","1","1","1"))




//        Log.d("chattingRoomList ", chattingRoomList[0].buyer)
        /*chat_recyclerView.layoutManager = LinearLayoutManager(getContext())
        chat_recyclerView.adapter = ChatAdapter(getContext(), chattingRoomList)*/




        return view
    }

    override fun onResume() {
        super.onResume()
        chattingRoomList.clear()
        chattingRoomUidList.clear()
        userData()
    }
    private fun userData(){
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user)).document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    if(userEntity?.chatting != null){
                        for(i in userEntity.chatting){
                            chattingRoomUidList.add(i)
                        }
                        chattingRoomList(chattingRoomUidList)
                    }
                }
            }
    }

    @SuppressLint("LongLogTag")
    fun chattingRoomList(list:MutableList<String>){
        for(i in list){
            FirebaseFirestore.getInstance().collection("Chatting").document(i).get()
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val chatRoomVO: ChatRoomVO? = task.result!!.toObject<ChatRoomVO>(ChatRoomVO::class.java)
                        Log.d(chatRoomVO?.buyer, chatRoomVO?.buyer)
                        Log.d(chatRoomVO?.seller, chatRoomVO?.seller)
                        Log.d(chatRoomVO?.pid, if (chatRoomVO != null) chatRoomVO.pid else null)
                        Log.d(chatRoomVO?.registDate.toString(), chatRoomVO?.registDate.toString())
                        Log.d(chatRoomVO?.comment, chatRoomVO?.comment)

                        Log.d("chattingRoomList ", chatRoomVO!!.buyer)

                        chattingRoomList.add(chatRoomVO)
                        Log.d("chattingRoomList 11111 ", chattingRoomList[0].buyer)

                    }
                    Log.d("chattingRoomList 4444444 ", chattingRoomList[0].buyer)

                    if(list.size == chattingRoomUidList.size){
                        chat_recyclerView.layoutManager = LinearLayoutManager(context)
                        chat_recyclerView.adapter = ChatAdapter(context, chattingRoomList, chattingRoomUidList)
                    }
                }
        }
    }


/*

    fun getName(uid:String):String{
        lateinit var name:String
        FirebaseFirestore.getInstance().collection("Users").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    name = userEntity?.name.toString()
                    Log.d("getName 1231 231 2  ", name)
                }
            }
        Log.d("getName 1231 231 2  ", name)
        return name
    }

    fun getProductImage(uid:String):String{
        var image=""
        FirebaseFirestore.getInstance().collection("Product").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val productEntity: ProductEntity? = task.result!!.toObject<ProductEntity>(
                        ProductEntity::class.java)
                    image = productEntity?.imageArray!![0]
                }
            }
        return image
    }
*/

   /* fun getName(uid:String, holder: ChatAdapter.ViewHolder, context: Context){
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

    fun getProductImage(uid:String, holder: ChatAdapter.ViewHolder, context: Context){
        db.collection("Product").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val productEntity: ProductEntity? = task.result!!.toObject<ProductEntity>(
                        ProductEntity::class.java)
                    if (context != null) {
                         Glide.with(context).load(productEntity?.imageArray!![0]).into(holder.pImage)
                    }
                }
            }
    }

    fun chattingIntent(uid:String, chatRoomUid:String, context: Context){
        db.collection("User").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("chatRoomUid", chatRoomUid)
                    intent.putExtra("name", userEntity?.name.toString())
                    context.startActivity(intent)
                }
            }
    }*/

}
