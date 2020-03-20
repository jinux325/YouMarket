package com.u.marketapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.adapter.ChatAdapter
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.vo.ChatRoomVO
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.*


class ChatFragment : Fragment() {

    private var chattingRoomList: MutableList<ChatRoomVO> = mutableListOf()
    private var chattingRoomUidList: MutableList<String> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity?)!!.setSupportActionBar(account_toolbar)
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
                        Log.d(chatRoomVO?.pid, chatRoomVO?.pid)
                        Log.d(chatRoomVO?.registDate.toString(), chatRoomVO?.registDate.toString())
                        Log.d(chatRoomVO?.comment, chatRoomVO?.comment)

                        Log.d("chattingRoomList ", chatRoomVO!!.buyer)

                        chatRoomVO.cId=i
                        chattingRoomList.add(chatRoomVO)
                        Log.d("chattingRoomList 11111 ", chattingRoomList[0].comment+"  "+chattingRoomList[0].cId)
                        chattingRoomList.sortWith(Comparator { data1, data2 -> data2.registDate!!.compareTo(data1.registDate)})

                    }
                    Log.d("chattingRoomList 4444444 ", chattingRoomList[0].buyer)

                    if(list.size == chattingRoomUidList.size){
                        chat_recyclerView.layoutManager = LinearLayoutManager(context)
                        chat_recyclerView.adapter = ChatAdapter(context, chattingRoomList)
                    }
                }
        }
    }




}
