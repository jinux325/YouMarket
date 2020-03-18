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
import com.u.marketapp.adapter.ChattingAdapter
import com.u.marketapp.vo.ChatRoomVO
import com.u.marketapp.vo.ChattingVO
import com.u.marketapp.vo.UserEntity
import kotlinx.android.synthetic.main.activity_chat.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val myUid = FirebaseAuth.getInstance().currentUser!!.uid
    lateinit var chatRoom : HashMap<String,Any>
    lateinit var chatRoomUid : String
    lateinit var chattingList : MutableList<ChattingVO>
    var comment:String=""
    lateinit var name:String
    lateinit var myData:UserEntity
    lateinit var pid:String
    lateinit var seller:String
    private lateinit var token:String

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val intent_items = getIntent()
        tv_partner_nickname.text = intent_items.getStringExtra("name")

        myData()

        if(intent_items.hasExtra("chatRoomUid")) {
            Log.d("EQWRQ!@D!@D!d 11 ", "asada ")
            chatRoomUid = intent_items.getStringExtra("chatRoomUid")
            getChattingList()
        }else{
            Log.d("EQWRQ!@D!@D!d 22 ", "asada ")
            getChattingRoom()
            /*     pid = intent_items.getStringExtra("pid")
                 seller = intent_items.getStringExtra("seller")

                 chatRoom = hashMapOf(
                     "pid" to pid,
                     "buyer" to myUid,
                     "seller" to seller,
                     "comment" to comment,
                     "registDate" to Date(System.currentTimeMillis())
                 )*/

            //Log.d("EQWRQ!@D!@D!d  ", pid+"   "+myUid)


            //getDocumentId(pid, myUid)
            /* Log.d("EQWRQ!@D!@D!d 111 ", pid+"   "+myUid)
             db.collection("Chatting").whereEqualTo("pid", pid).whereEqualTo("buyer", myUid).get().addOnSuccessListener { result ->
                     Log.d("EQWRQ!@D!@D!d 444  ", result.toString())
                     for(document in result){
                         chatRoomUid = document.id
                         Log.d("EQWRQ!@D!@D!d 555 ", chatRoomUid)
                         getChattingList()
                     }
                 }*/
        }



       // getChattingList()
       /* db.collection("Chatting").document(chatRoomUid).collection("comment").orderBy("registDate")
            .addSnapshotListener{ snapshot, e ->
                chattingList= mutableListOf()
                for (doc in snapshot!!) {
                    val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                    chattingList.add(chattingVO)
                }
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = ChattingAdapter(this, chattingList)
                recyclerView.scrollToPosition(chattingList.size - 1)
            }*/




        /*db.collection("Chatting").document(chatRoomUid).collection("comment").orderBy("registDate").get()
            .addOnSuccessListener{ document ->
                for(doc in document){
                    val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                    chattingList.add(chattingVO)
                }
                //Log.d("@@@@@@@@@@@@@@@@@@@@@@@@ ", chattingList[0].message)
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = ChattingAdapter(this, chattingList)
            }*/

        iv_send.setOnClickListener {
            // kLIDekdZbCP0h99ZN8tIP3NhRct1
           // val uid = FirebaseAuth.getInstance().currentUser!!.uid
           // val uid = "3Nlu6jrJ0UcBC4cGjty17mOXoVj1"
            /*Log.d("채팅방 id ", chatRoomUid)
            Log.d(" 채팅보내기 ", " comment:   et_message: "+et_message.text+"  "+et_message.text.toString())*/
            comment= et_message.text.toString()

            val intent_items = intent
            if(intent_items.hasExtra("pid")){
                addChatRoom(myUid, pid, seller)
            }else{
                addChatComment(myUid, comment, chatRoomUid)
            }
            et_message.text.clear()

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
            "image" to myData!!.imgPath,
            "name" to myData!!.name,
            "uid" to uid,
            "message" to comment ,
            "registDate" to registDate
        )
        FirebaseFirestore.getInstance().collection("Chatting").document(documentId).collection("comment").document().set(chat).addOnSuccessListener {
            FirebaseFirestore.getInstance().collection("Chatting").document(chatRoomUid).update("comment", comment,"registDate", registDate).addOnSuccessListener { token() }
        }


    }

    fun addChatComment(buyerUid:String, pid:String, comment:String, sellerUid:String){
        Log.d("addChatComment "," buyerUid: $buyerUid, pid: $pid, comment: $comment, sellerUid: $sellerUid")
        FirebaseFirestore.getInstance().collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo("buyer", buyerUid).get().addOnSuccessListener { result ->
                for(document in result){
                    val registDate = Date(System.currentTimeMillis())

                    val chat = hashMapOf(
                        "image" to myData!!.imgPath,
                        "name" to myData!!.name,
                        "uid" to buyerUid,
                        "message" to comment ,
                        "registDate" to registDate
                    )
                    chatRoomUid = document.id
                    FirebaseFirestore.getInstance().collection("Chatting").document(document.id)
                        .collection("comment").document().set(chat).addOnSuccessListener { getChattingList() }
                    if(!(buyerUid.equals(sellerUid))){
                        FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user)).document(sellerUid)
                            .update("chatting", FieldValue.arrayUnion(document.id))
                    }
                    FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user)).document(buyerUid)
                        .update("chatting", FieldValue.arrayUnion(document.id))
                    FirebaseFirestore.getInstance().collection("Chatting").document(document.id)
                        .update("comment", comment,"registDate", registDate)
                    token()
                    /*
                    FirebaseFirestore.getInstance().collection("Users").document(sellerUid)
                        .update("chatting", FieldValue.arrayUnion(document.id))
                    */

                }
            }
    }

    fun myData(){
        db.collection(resources.getString(R.string.db_user)).document(myUid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    myData= userEntity!!
                    Log.d(" @@@@ 내 데이터 @@@@ ", myData!!.name)
                }
            }
    }

    fun getDocumentId(pid:String, myUid:String){
        Log.d("EQWRQ!@D!@D!d 333 ", pid+"   "+myUid)
        FirebaseFirestore.getInstance().collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo("buyer", myUid).get().addOnSuccessListener { result ->
                Log.d("EQWRQ!@D!@D!d 444 ", "firebase ")
                for(document in result){
                    Log.d("EQWRQ!@D!@D!d 555 ", document.id)
                    chatRoomUid = document.id
                    getChattingList()
                }
            }


    }

    fun getChattingList(){
        db.collection("Chatting").document(chatRoomUid).collection("comment").orderBy("registDate")
            .addSnapshotListener{ snapshot, e ->
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


    fun getChattingRoom(){
        val intent_items = getIntent()
        pid = intent_items.getStringExtra("pid")
        seller = intent_items.getStringExtra("seller")

        chatRoom = hashMapOf(
            "pid" to pid,
            "buyer" to myUid,
            "seller" to seller,
            "comment" to comment,
            "registDate" to Date(System.currentTimeMillis())
        )

        Log.d("EQWRQ!@D!@D!d  ", pid+"   "+myUid)

        getDocumentId(pid, myUid)

       /*

       FirebaseFirestore.getInstance().collection("Chatting")
            .whereEqualTo("pid", pid).whereEqualTo("buyer", myUid).get().addOnSuccessListener { result ->
                Log.d("EQWRQ!@D!@D!d  ", result.toString())
                for(document in result){
                    chatRoomUid = document.id
                    Log.d("EQWRQ!@D!@D!d  ", chatRoomUid)
                }
            }

*/
    }

    fun token(){
        db.collection("Chatting").document(chatRoomUid).get()
            .addOnSuccessListener { documentSnapshot ->
                val chatRoomVO: ChatRoomVO? = documentSnapshot.toObject(ChatRoomVO::class.java)
                if (chatRoomVO!!.buyer.equals(myUid)) {
                    Log.d("@@ Thread seller", chatRoomVO!!.buyer+"  $myUid")
                    getToken(chatRoomVO!!.seller.toString())
                } else {
                    Log.d("@@ Thread buyer", chatRoomVO!!.seller+"  $myUid")
                    getToken(chatRoomVO!!.buyer.toString())
                }
            }
    }
    fun getToken(uid:String){
        Log.d("@@ getToken ", "uid: $uid")
        db.collection(resources.getString(R.string.db_user)).document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    token = userEntity!!.token
                    Log.d("@@ getToken token  ", userEntity!!.token)
                    //FCM(userEntity!!.token)
                    val thread=FCM()
                    thread.start()
                    //FCM(userEntity!!.token)
                }
            }
    }

    /*class FCM : Thread() {
        override fun run() {
            try {
                val serverKey =
                    "AAAACA4EsA0:APA91bGdb7Oxa49X6z23tXjCn48DiosjzqYFZXM6G67I_gH5sFI_AKuoFJ6ayLyqBHGAmckEkMSO8UU5qD8XFesWRSlDKBVdx6zHI_cCEaz6xCTg4CbgWkKCNxVBzM3SupUJXio41w6a"
                val registrationToken: String = token
                val FCM_URL = "https://fcm.googleapis.com/fcm/send"
                val title = name
                val body: String = et_message.text.toString()
                Log.e("@@ ChatActivity Thread", token + "  " + title + "  " + body)
                // FMC 메시지 생성 start
                val root = JSONObject()
                val data = JSONObject()
                data.put("title", title)
                data.put("body", body)
                data.put("click_action", "ChatActivity")
                data.put("documentId", documentId)
                //root.put("notification", notification);
                root.put("to", registrationToken)
                root.put("data", data)
                // FMC 메시지 생성 end
                Log.d("Main_ Thread", "@@@")
                val Url = URL(FCM_URL)
                val conn =
                    Url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.doInput = true
                conn.addRequestProperty("Authorization", "key=$serverKey")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Content-type", "application/json")
                val os = conn.outputStream
                os.write(root.toString().toByteArray(charset("utf-8")))
                os.flush()
                conn.responseCode
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Log.d("Main_ Thread", "@@")
            }
        }
    }*/
/*
    fun FCM(token:String){
        Log.d("@@ FCM ", "token: $token")
        Thread(){
            run(){
                Log.d("@@ FCM 1111 ", "token: $token")
                try {
                    val serverKey =
                        "AAAACA4EsA0:APA91bGdb7Oxa49X6z23tXjCn48DiosjzqYFZXM6G67I_gH5sFI_AKuoFJ6ayLyqBHGAmckEkMSO8UU5qD8XFesWRSlDKBVdx6zHI_cCEaz6xCTg4CbgWkKCNxVBzM3SupUJXio41w6a"
                    val registrationToken: String = token
                    val FCM_URL = "https://fcm.googleapis.com/fcm/send"
                    val title = name
                    val body: String = et_message.text.toString()
                    Log.e("@@ ChatActivity Thread", token + "  " + title + "  " + body)
                    // FMC 메시지 생성 start
                    val root = JSONObject()
                    val data = JSONObject()
                    data.put("title", title)
                    data.put("body", body)
                    data.put("click_action", "ChatActivity")
                    data.put("documentId", chatRoomUid)
                    //root.put("notification", notification);
                    root.put("to", registrationToken)
                    root.put("data", data)
                    // FMC 메시지 생성 end
                    Log.d("Main_ Thread", "@@@")
                    val Url = URL(FCM_URL)
                    val conn =
                        Url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.doOutput = true
                    conn.doInput = true
                    conn.addRequestProperty("Authorization", "key=$serverKey")
                    conn.setRequestProperty("Accept", "application/json")
                    conn.setRequestProperty("Content-type", "application/json")
                    val os = conn.outputStream
                    os.write(root.toString().toByteArray(charset("utf-8")))
                    os.flush()
                    conn.responseCode
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("Main_ Thread", "@@")
                }
            }
        }
    }*/

    inner class FCM : Thread() {
        override fun run() {
            try {
                Log.d("@@ FCM ", "token: $token")
                val serverKey =
                    "AAAACA4EsA0:APA91bGdb7Oxa49X6z23tXjCn48DiosjzqYFZXM6G67I_gH5sFI_AKuoFJ6ayLyqBHGAmckEkMSO8UU5qD8XFesWRSlDKBVdx6zHI_cCEaz6xCTg4CbgWkKCNxVBzM3SupUJXio41w6a"
                val registrationToken: String = token
                val FCM_URL = "https://fcm.googleapis.com/fcm/send"
                val title = myData.name
                val body = comment
                Log.e("@@ ChatActivity Thread", token + "  " + title + "  " + body)
                // FMC 메시지 생성 start
                val root = JSONObject()
                val data = JSONObject()
                data.put("title", title)
                data.put("body", body)
                data.put("click_action", "ChatActivity")
                data.put("documentId", chatRoomUid)
                //root.put("notification", notification);
                root.put("to", registrationToken)
                root.put("data", data)
                // FMC 메시지 생성 end
                Log.d("Main_ Thread", "@@@")
                val Url = URL(FCM_URL)
                val conn =
                    Url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.doInput = true
                conn.addRequestProperty("Authorization", "key=$serverKey")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Content-type", "application/json")
                val os = conn.outputStream
                os.write(root.toString().toByteArray(charset("utf-8")))
                os.flush()
                conn.responseCode
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Main_ Thread", "@@")
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
