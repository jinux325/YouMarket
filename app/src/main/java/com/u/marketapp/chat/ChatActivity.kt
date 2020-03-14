package com.u.marketapp.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.adapter.ChatAdapter
import com.u.marketapp.adapter.ChattingAdapter
import com.u.marketapp.vo.ChattingVO
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val myUid = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
    lateinit var chatRoom : HashMap<String,Any>
    lateinit var chatRoomUid : String
    private var chattingList : MutableList<ChattingVO> = mutableListOf<ChattingVO>()

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent_items = getIntent()
        chatRoomUid = intent_items.getStringExtra("chatRoomUid")

        tv_partner_nickname.text = intent_items.getStringExtra("name")

        db.collection("Chatting").document(chatRoomUid).collection("comment").orderBy("registDate")
            .addSnapshotListener{ snapshot, e ->

            }


        db.collection("Chatting").document(chatRoomUid).collection("comment").orderBy("registDate").get()
            .addOnSuccessListener{ document ->
                for(doc in document){
                    val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                    chattingList.add(chattingVO)
                }
                //Log.d("@@@@@@@@@@@@@@@@@@@@@@@@ ", chattingList[0].message)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = ChattingAdapter(this, chattingList)
            }

        iv_send.setOnClickListener {
            // kLIDekdZbCP0h99ZN8tIP3NhRct1
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
           // val uid = "3Nlu6jrJ0UcBC4cGjty17mOXoVj1"
            val seller = "kLIDekdZbCP0h99ZN8tIP3NhRct1"
            val pid = "E1vYCUvs3algrwWCVRXF"

            val comment = et_message.text.toString()

            chatRoom = hashMapOf(
                "pid" to pid,
                "buyer" to uid,
                "seller" to seller,
                "comment" to comment ,
                "registDate" to Date(System.currentTimeMillis())
            )

/*
            addChatRoom(uid, pid, other, "seller")
            addChatRoom(other, pid, uid, "buyer")
*/

            val intent_items = intent
            if(intent_items.getStringExtra("Chatting") != null){
                addChatRoom(uid, pid, seller)
            }else{
                addChatComment(uid, et_message.text.toString(), chatRoomUid)
            }







            /*FirebaseFirestore.getInstance().collection("Users").document(buyer).collection("Chatting").document()
                .set(chatRoom).addOnCompleteListener {
                    FirebaseFirestore.getInstance().collection("Users").document(buyer).collection("Chatting")
                        .whereEqualTo("pid", pid).whereEqualTo("buyer", buyer).get().addOnSuccessListener { result ->
                            for(document in result){
                                val chat = hashMapOf(
                                    "image" to "https://firebasestorage.googleapis.com/v0/b/umarket-d3048.appspot.com/o/Profile%2FkLIDekdZbCP0h99ZN8tIP3NhRct1%2F1567085889874.jpg?alt=media&token=dd4e7132-2285-45e0-8912-9478142e1074",
                                    "name" to "이름",
                                    "uid" to "kLIDekdZbCP0h99ZN8tIP3NhRct1",
                                    "message" to comment ,
                                    "registDate" to Date(System.currentTimeMillis())
                                )

                                FirebaseFirestore.getInstance().collection("Users").document(buyer).collection("Chatting").document(document.id)
                                    .collection("comment").document().set(chat)
                            }
                        }
                }*/
        }


    }


    fun addChatRoom(uid:String, pid:String, sellerUid:String){
        FirebaseFirestore.getInstance().collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo("buyer", uid).get().addOnSuccessListener { result ->
                if(result.size() == 0){
                    FirebaseFirestore.getInstance().collection("Chatting").document()
                        .set(chatRoom).addOnSuccessListener {
                            addChatComment(uid, pid, et_message.text.toString(), sellerUid)
                        }
                }else{
                    for(document in result){
                        addChatComment(uid, et_message.text.toString(), document.id)
                    }
                }
            }
    }

    fun addChatComment(uid:String, comment:String, documentId:String){
        val chat = hashMapOf(
            "image" to "https://firebasestorage.googleapis.com/v0/b/umarket-d3048.appspot.com/o/Profile%2FkLIDekdZbCP0h99ZN8tIP3NhRct1%2F1567085889874.jpg?alt=media&token=dd4e7132-2285-45e0-8912-9478142e1074",
            "name" to "이름",
            "uid" to uid,
            "message" to comment ,
            "registDate" to Date(System.currentTimeMillis())
        )
        FirebaseFirestore.getInstance().collection("Chatting").document(documentId).collection("comment").document().set(chat)
    }

    fun addChatComment(buyerUid:String, pid:String, comment:String, sellerUid:String){
        FirebaseFirestore.getInstance().collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo("buyer", buyerUid).get().addOnSuccessListener { result ->
                for(document in result){
                    val chat = hashMapOf(
                        "image" to "https://firebasestorage.googleapis.com/v0/b/umarket-d3048.appspot.com/o/Profile%2FkLIDekdZbCP0h99ZN8tIP3NhRct1%2F1567085889874.jpg?alt=media&token=dd4e7132-2285-45e0-8912-9478142e1074",
                        "name" to "이름",
                        "uid" to buyerUid,
                        "message" to comment ,
                        "registDate" to Date(System.currentTimeMillis())
                    )

                    FirebaseFirestore.getInstance().collection("Chatting").document(document.id)
                        .collection("comment").document().set(chat)
                    if(!(buyerUid.equals(sellerUid))){
                        FirebaseFirestore.getInstance().collection("Users").document(sellerUid)
                            .update("chatting", FieldValue.arrayUnion(document.id))
                    }
                    FirebaseFirestore.getInstance().collection("Users").document(buyerUid)
                        .update("chatting", FieldValue.arrayUnion(document.id))
                    /*
                    FirebaseFirestore.getInstance().collection("Users").document(sellerUid)
                        .update("chatting", FieldValue.arrayUnion(document.id))
                    */

                }
            }
    }




   /* fun addChatRoom(addUid:String, pid:String, otherUid:String, otherStr:String){
        FirebaseFirestore.getInstance().collection("Users").document(addUid).collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo(otherStr, otherUid).get().addOnSuccessListener { result ->
                if(result.size() == 0){
                    FirebaseFirestore.getInstance().collection("Users").document(addUid).collection("Chatting").document()
                        .set(chatRoom).addOnSuccessListener {
                            addChatComment(addUid, pid, et_message.text.toString(), otherStr)
                        }
                }else{
                    addChatComment(addUid, pid, et_message.text.toString(), otherStr)
                }


            }
    }

    fun addChatComment(addUid:String, pid:String, comment:String, otherStr:String){
        FirebaseFirestore.getInstance().collection("Users").document(addUid).collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo(otherStr, addUid).get().addOnSuccessListener { result ->
                for(document in result){
                    val chat = hashMapOf(
                        "image" to "https://firebasestorage.googleapis.com/v0/b/umarket-d3048.appspot.com/o/Profile%2FkLIDekdZbCP0h99ZN8tIP3NhRct1%2F1567085889874.jpg?alt=media&token=dd4e7132-2285-45e0-8912-9478142e1074",
                        "name" to "이름",
                        "uid" to addUid,
                        "message" to comment ,
                        "registDate" to Date(System.currentTimeMillis())
                    )

                    FirebaseFirestore.getInstance().collection("Users").document(addUid).collection("Chatting").document(document.id)
                        .collection("comment").document().set(chat)
                }
            }
    }*/
}
