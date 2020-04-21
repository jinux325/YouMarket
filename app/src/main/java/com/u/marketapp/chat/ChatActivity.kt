package com.u.marketapp.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.adapter.ChattingAdapter
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.vo.ChatRoomVO
import com.u.marketapp.vo.ChattingVO
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.toolbar
import java.util.*


class ChatActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()
    private val myUid = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var chatRoom : HashMap<String,Any>
    private var chatRoomUid : String = ""
    private lateinit var chattingList : MutableList<ChattingVO>
    var comment:String=""
    lateinit var name:String
    private lateinit var myData: UserEntity
    private lateinit var pid:String
    private lateinit var seller:String
    private lateinit var token:String

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val intentItems = intent
        tv_partner_nickname.text = intentItems.getStringExtra("name")

        myData()

        if(intentItems.hasExtra("chatRoomUid")) {
            chatRoomUid = intentItems.getStringExtra("chatRoomUid")
            getChattingList()
        }else{
            getChattingRoom()
        }

        iv_send.setOnClickListener {
            comment= et_message.text.toString()

            val intentItem = intent
            if(intentItem.hasExtra("pid")){
                addChatRoom(myUid, pid, seller)
            }else{
                addChatComment(myUid, comment, chatRoomUid)
            }
            et_message.text.clear()

        }


    }
    // toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_chatting, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_chatting -> {
                if(chatRoomUid == ""){
                    // 채팅방 아직 없음
                    finish()
                }else{
                    // 채팅방 나가기
                    val arr : MutableList<String> = myData.chatting
                    for(i in 0 until arr.size){
                        if(arr[i] == chatRoomUid){
                            arr.removeAt(i)
                            break
                        }
                    }
                    val delete = hashMapOf<String, Any>(
                        "chatting" to arr
                    )

                    db.collection(resources.getString(R.string.db_user)).document(myUid)
                        .update(delete).addOnSuccessListener {
                            chattingMyUidDel()
                            Toast.makeText(applicationContext, "채팅 나가기", Toast.LENGTH_LONG).show()
                        }

                }

                return true
            }
            else -> {
                Toast.makeText(applicationContext, "나머지 버튼 클릭됨", Toast.LENGTH_LONG).show()
                super.onOptionsItemSelected(item)
            }
        }
    }

    @SuppressLint("LongLogTag")
    fun addChatRoom(uid:String, pid:String, sellerUid:String){
        Log.d("addChatRoom "," uid: $uid, pid: $pid, sellerUid: $sellerUid")
        FirebaseFirestore.getInstance().collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo("buyer", uid).get().addOnSuccessListener { result ->
                if(result.size() == 0){
                    FirebaseFirestore.getInstance().collection("Chatting").document()
                        .set(chatRoom).addOnSuccessListener {
                            addChatComment(uid, pid, comment, sellerUid)
                        }
                }else{
                    for(document in result){
                        addChatComment(uid, comment, document.id)
                    }
                }
            }
    }

    @SuppressLint("LongLogTag")
    fun addChatComment(uid:String, comment:String, documentId:String){
        Log.d("addChatComment "," uid: $uid, comment: $comment, documentId: $documentId")
        val registDate = Date(System.currentTimeMillis())
        val chat = hashMapOf(
            "image" to myData.imgPath,
            "name" to myData.name,
            "uid" to uid,
            "message" to comment ,
            "registDate" to registDate
        )
        FirebaseFirestore.getInstance().collection("Chatting").document(documentId).collection("comment").document().set(chat).addOnSuccessListener {
            FirebaseFirestore.getInstance().collection("Chatting").document(chatRoomUid).update("comment", comment,"registDate", registDate).addOnSuccessListener { token() }
        }


    }

    private fun addChatComment(buyerUid:String, pid:String, comment:String, sellerUid:String){
        Log.d("addChatComment "," buyerUid: $buyerUid, pid: $pid, comment: $comment, sellerUid: $sellerUid")
        FirebaseFirestore.getInstance().collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo("buyer", buyerUid).get().addOnSuccessListener { result ->
                for(document in result){
                    val registDate = Date(System.currentTimeMillis())

                    val chat = hashMapOf(
                        "image" to myData.imgPath,
                        "name" to myData.name,
                        "uid" to buyerUid,
                        "message" to comment ,
                        "registDate" to registDate
                    )
                    chatRoomUid = document.id
                    FirebaseFirestore.getInstance().collection("Chatting").document(document.id)
                        .collection("comment").document().set(chat).addOnSuccessListener { getChattingList() }
                    if(buyerUid != sellerUid){
                        FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user)).document(sellerUid)
                            .update("chatting", FieldValue.arrayUnion(document.id))
                    }
                    FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user)).document(buyerUid)
                        .update("chatting", FieldValue.arrayUnion(document.id))
                    FirebaseFirestore.getInstance().collection("Chatting").document(document.id)
                        .update("comment", comment,"registDate", registDate)
                    token()


                }
            }
    }

    private fun myData(){
        db.collection(resources.getString(R.string.db_user)).document(myUid).get()
            .addOnSuccessListener { documentSnapshot ->
                    val userEntity: UserEntity? = documentSnapshot.toObject<UserEntity>(
                        UserEntity::class.java)
                    myData= userEntity!!
                    Log.d(" @@@@ 내 데이터 @@@@ ", myData.name)

            }
    }

    private fun getDocumentId(pid:String, myUid:String){
        FirebaseFirestore.getInstance().collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo("buyer", myUid).get().addOnSuccessListener { result ->
                for(document in result){
                    chatRoomUid = document.id
                    getChattingList()
                }
            }


    }

    private fun getChattingList(){
        db.collection("Chatting").document(chatRoomUid).collection("comment").orderBy("registDate")
            .addSnapshotListener{ snapshot, _ ->
                chattingList= mutableListOf()
                for (doc in snapshot!!) {
                    val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                    chattingList.add(chattingVO)
                }
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = ChattingAdapter(this, chattingList)
                recyclerView.scrollToPosition(chattingList.size - 1)
            }
    }


    private fun getChattingRoom(){
        val intentItems = intent
        pid = intentItems.getStringExtra("pid")
        seller = intentItems.getStringExtra("seller")

        chatRoom = hashMapOf(
            "pid" to pid,
            "buyer" to myUid,
            "seller" to seller,
            "comment" to comment,
            "registDate" to Date(System.currentTimeMillis())
        )


        getDocumentId(pid, myUid)


    }

    private fun token(){
        db.collection("Chatting").document(chatRoomUid).get()
            .addOnSuccessListener { documentSnapshot ->
                val chatRoomVO: ChatRoomVO? = documentSnapshot.toObject(ChatRoomVO::class.java)
                if (chatRoomVO!!.buyer.equals(myUid)) {
                    Log.d("@@ Thread seller", chatRoomVO.buyer+"  $myUid")
                    getToken(chatRoomVO.seller.toString())
                } else {
                    Log.d("@@ Thread buyer", chatRoomVO.seller+"  $myUid")
                    getToken(chatRoomVO.buyer.toString())
                }
            }
    }

    private fun getToken(uid:String){
        if(uid != "") {
            Log.d("@@ getToken ", "uid: $uid")
            db.collection(resources.getString(R.string.db_user)).document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val userEntity: UserEntity? = documentSnapshot.toObject<UserEntity>(
                        UserEntity::class.java
                    )
                    token = userEntity!!.token
                    //FCM(userEntity!!.token)
                    val fcm = FCM(
                        token,
                        myData.name,
                        comment,
                        chatRoomUid,
                        tv_partner_nickname.text.toString(),
                        resources.getString(R.string.ChatActivity)
                    )
                    fcm.start()
                    /*val thread=FCM()
                thread.start()*/
                    //FCM(userEntity!!.token)

                }
        }
    }

    private fun chattingMyUidDel(){
        db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).get()
            .addOnSuccessListener { document ->
                val chatRoomVO: ChatRoomVO? = document.toObject(ChatRoomVO::class.java)
                if(myUid == chatRoomVO?.buyer){
                    if(chatRoomVO.seller == ""){
                        //chattingCommentDel(document.reference)
                        document.reference.collection("comment").get()
                            .addOnSuccessListener { result ->
                                for(documents in result){
                                    documents.reference.delete().addOnSuccessListener {
                                        db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).delete().addOnSuccessListener {
                                            finish()
                                        }
                                    }


                                }
                            }
                    }else{
                        db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid)
                            .update("buyer", "").addOnSuccessListener { finish() }

                    }

                }else{
                    if(chatRoomVO?.buyer == ""){
                        //chattingCommentDel(document.reference)
                       document.reference.collection("comment").get()
                            .addOnSuccessListener { result ->
                                for(documents in result){
                                    documents.reference.delete().addOnSuccessListener {
                                        db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).delete().addOnSuccessListener {
                                            finish()
                                        }
                                    }


                                }
                            }

                    }else{
                        db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid)
                            .update("seller", "").addOnSuccessListener { finish() }

                    }

                }
            }
    }


}
