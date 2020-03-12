package com.u.marketapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.adapter.ChatAdapter
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.vo.ChatRoomVO
import com.u.marketapp.vo.UserEntity
import kotlinx.android.synthetic.main.fragment_chat.*


class ChatFragment : Fragment() {

    var chattingRoomList: MutableList<ChatRoomVO> = mutableListOf()
    var chattingRoomUidList: MutableList<String> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        (activity as AppCompatActivity).supportActionBar?.hide()
        //(activity as AppCompatActivity).setSupportActionBar(setting_toolbar)

        val chat_recyclerView = view.findViewById(R.id.chat_recyclerView) as RecyclerView
       // chattingRoomList = mutableListOf<ChatRoomVO>(ChatRoomVO("1","1", "","1","1","1","1","1"),ChatRoomVO("1","1", "","1","1","1","1","1"),ChatRoomVO("1","1", "","1","1","1","1","1"))

       // userData()

        chat_recyclerView.layoutManager = LinearLayoutManager(getContext())
        chat_recyclerView.adapter = ChatAdapter(getContext(), chattingRoomList)


        return view
    }

    fun userData(){
        var uid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("Users").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    if(userEntity?.chatting != null){
                        for(i in userEntity?.chatting!!){
                            chattingRoomUidList.add(i)
                        }
                        chattingRoomList(chattingRoomUidList)
                    }
                }
            }
    }

    fun chattingRoomList(list:MutableList<String>){
        for(i in list){
            FirebaseFirestore.getInstance().collection("Chatting").document(i).get()
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val chatRoomVO: ChatRoomVO? = task.result!!.toObject<ChatRoomVO>(ChatRoomVO::class.java)
                        Log.d(chatRoomVO?.buyer, chatRoomVO?.buyer)
                        Log.d(chatRoomVO?.seller, chatRoomVO?.seller)
                        Log.d(chatRoomVO?.pid, chatRoomVO?.pid)
                        Log.d(chatRoomVO?.registDate.toString(), chatRoomVO?.registDate.toString())
                        Log.d(chatRoomVO?.comment, chatRoomVO?.comment)

                        chattingRoomList.add(chatRoomVO!!)
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

}
