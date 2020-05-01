package com.u.marketapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.activity.MainActivity
import com.u.marketapp.R
import com.u.marketapp.adapter.ChatAdapter
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.vo.ChatRoomVO
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
/*

        swipe.setOnRefreshListener{
            chattingRoomList.clear()
            chattingRoomUidList.clear()
            userData()

            swipe.isRefreshing= false
        }
*/

    }

    override fun onResume() {
        super.onResume()
        Log.e("ChatFragment ", "onResume")

        chattingRoomList.clear()
        chattingRoomUidList.clear()
        userData()
    }


    private fun userData(){
        Log.e("ChatFragment ", "userData")
        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user)).document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                    val userEntity: UserEntity? = documentSnapshot.toObject<UserEntity>(UserEntity::class.java)
                Log.e("ChatFragment ", "userData "+userEntity?.chatting)
                    if(userEntity?.chatting != null){
                        Log.e("ChatFragment ", "userData 2 ")
                        for(i in userEntity.chatting){
                            chattingRoomUidList.add(i)
                        }
                        chattingRoomList(chattingRoomUidList)
                    }

            }

        Log.e("ChatFragment ", "userData 3 "+uid)
    }



    @SuppressLint("LongLogTag")
    fun chattingRoomList(list:MutableList<String>){
        if(list.size == 0){
            Log.e(" chattingRoomList ", "리스트 사이즈 0")
            chat_recyclerView.visibility=View.GONE
            chat_tv.visibility= View.VISIBLE
        }else{
            Log.e(" chattingRoomList ", "리스트 사이즈 not 0 ")
            chat_recyclerView.visibility=View.VISIBLE
            chat_tv.visibility= View.GONE

            for(i in list){
                Log.e(" chattingRoomList ", "for 문 list ")
                FirebaseFirestore.getInstance().collection("Chatting").document(i).get()
                    .addOnCompleteListener{ task ->
                        Log.e(" chattingRoomList ", "for 문 list  firebase  ")
                        val chatRoomVO: ChatRoomVO? = task.result!!.toObject<ChatRoomVO>(ChatRoomVO::class.java)
                        if(chatRoomVO != null ){
                            Log.e(" chattingRoomList ", "for 문 list  firebase if  ")
                            if (task.isSuccessful) {
                                Log.e(" chattingRoomList ", "for 문 list  firebase if  if  ")
                                Log.e(" chattingRoom : ", "$chatRoomVO")
                                Log.d(chatRoomVO.buyer, chatRoomVO.buyer)
                                Log.d(chatRoomVO.seller, chatRoomVO.seller)
                                Log.d(chatRoomVO.pid, chatRoomVO.pid)
                                Log.d(chatRoomVO.registDate.toString(), chatRoomVO.registDate.toString())
                                Log.d(chatRoomVO.comment, chatRoomVO.comment)

                                Log.d("chattingRoomList ", chatRoomVO.buyer)

                                chatRoomVO.cId=i
                                chattingRoomList.add(chatRoomVO)
                                Log.d("chattingRoomList ", chattingRoomList[0].comment+"  "+chattingRoomList[0].cId)
                                chattingRoomList.sortWith(Comparator { data1, data2 -> data2.registDate!!.compareTo(data1.registDate)})

                            }
                            if(list.size == chattingRoomUidList.size){
                                Log.e(" chattingRoomList ", "for 문 list  firebase if  if  ${list.size}   ${chattingRoomUidList.size}")
                                chat_recyclerView.layoutManager = LinearLayoutManager(context)
                                chat_recyclerView.adapter = ChatAdapter(context, chattingRoomList)
                            }
                        }
                    }
            }
        }

    }




}
