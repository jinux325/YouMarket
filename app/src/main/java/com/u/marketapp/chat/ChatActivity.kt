package com.u.marketapp.chat

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.io.Files.getFileExtension
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.u.marketapp.R
import com.u.marketapp.adapter.ChattingAdapter
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.vo.ChatRoomVO
import com.u.marketapp.vo.ChattingVO
import gun0912.tedimagepicker.builder.TedImagePicker
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
    private var imageMsg:String = ""

    private lateinit var imageUri:Uri
    private lateinit var token:String
    private var mStorageRef: StorageReference? = FirebaseStorage.getInstance().getReference("Chatting")

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val intentItems = intent
        tv_partner_nickname.text = intentItems.getStringExtra("name")

        myData()

        if(intentItems.hasExtra("chatRoomUid")) {
            chatRoomUid = intentItems.getStringExtra("chatRoomUid")
            //pid = intentItems.getStringExtra("chatPid")
            getChattingList()
        }else{
            getChattingRoom()
        }

        iv_add_menu.setOnClickListener {
            //permission()
            TedImagePicker.with(this)
                .start { uri ->
                    imageUri = uri
                    getImageList() }
        }

        iv_send.setOnClickListener {
            comment= et_message.text.toString()

            if(!(comment.isBlank())){
                val intentItem = intent
                if(intentItem.hasExtra("pid")){
                    addChatRoom(myUid, pid, seller)
                }else{
                    addChatComment(myUid, comment, chatRoomUid)
                }
                et_message.text.clear()
            }else{
                Toast.makeText(this,"메시지를 적어주세요.",Toast.LENGTH_SHORT).show()
            }

        }


    }
    // toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_chatting, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
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
                            storageImageDel()
                           // chattingMyUidDel()
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


    private fun getImageList(){

        if(chatRoomUid == ""){
            val ref = db.collection("Chatting").document()
            chatRoomUid = ref.id

            val fileReference: StorageReference = mStorageRef!!.child(chatRoomUid)
                .child(System.currentTimeMillis().toString() + "." + getFileExtension(imageUri.toString())
                )
            fileReference.putFile(imageUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                fileReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    imageMsg = downloadUri.toString()

                    FirebaseFirestore.getInstance().collection("Chatting").document(ref.id)
                        .set(chatRoom).addOnSuccessListener {
                            addChatComment(myUid, pid, comment, seller)
                        }
                } else {
                    Toast.makeText(
                        this,
                        "upload failed: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }else{
            val fileReference: StorageReference = mStorageRef!!.child(chatRoomUid)
                .child(System.currentTimeMillis().toString() + "." + getFileExtension(imageUri.toString())
                )
            fileReference.putFile(imageUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                fileReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result

                    imageMsg = downloadUri.toString()

                    val intentItem = intent
                    if(intentItem.hasExtra("pid")){
                        addChatRoom(myUid, pid, seller)
                    }else{
                        addChatComment(myUid, et_message.text.toString().trim(), chatRoomUid)
                    }

                } else {
                    Toast.makeText(
                        this,
                        "upload failed: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
            "imageMsg" to imageMsg ,
            "registDate" to registDate
        )
        FirebaseFirestore.getInstance().collection("Chatting").document(documentId)
            .collection("comment").document().set(chat).addOnSuccessListener {
            FirebaseFirestore.getInstance().collection("Chatting").document(chatRoomUid)
                .update("comment", comment,"registDate", registDate).addOnSuccessListener {
                    token()
                }
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
                        "imageMsg" to imageMsg ,
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
                    FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_product)).document(pid)
                        .update("chattingRoom", FieldValue.arrayUnion(document.id))
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
                if(snapshot != null ){

                chattingList= mutableListOf()
                for (doc in snapshot) {
                    val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                    chattingList.add(chattingVO)
                }

                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = ChattingAdapter(this, chattingList)
                recyclerView.scrollToPosition(chattingList.size -1)
/*
                Log.e(" 스크롤 ", "${chattingList.size}")
                val smoothScroller: RecyclerView.SmoothScroller by lazy {
                    object : LinearSmoothScroller(this) {
                        override fun getVerticalSnapPreference() = SNAP_TO_START
                    }
                }
                smoothScroller.targetPosition = chattingList.size -1
                recyclerView.layoutManager?.startSmoothScroll(smoothScroller)*/
                //recyclerView.scrollToPosition(20)

                }
            }
    }

/*
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
        getDocumentId(pid, myUid,true)
    }*/

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
                    if(imageMsg != ""){
                        comment = "이미지"
                    }

                    val fcm = FCM(
                        token,
                        myData.name,
                        comment,
                        chatRoomUid,
                        tv_partner_nickname.text.toString(),
                        resources.getString(R.string.ChatActivity)
                    )
                    fcm.start()
                    comment =""
                    imageMsg=""
                }
        }
    }

    // 스토리지 이미지 삭제
    private fun storageImageDel(){
        db.collection("Chatting").document(chatRoomUid).collection("comment")
            .addSnapshotListener{ snapshot, _ ->
                for (doc in snapshot!!) {
                    val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                    if(chattingVO.imageMsg!=""){
                        chattingVO.imageMsg?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it).delete() }
                    }
                }
                chattingMyUidDel()
            }

      /*  db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).collection(resources.getString(R.string.db_comment))
            .addSnapshotListener{ snapshot, _ ->
                Log.e(" 스토리지 삭제 ", " 삭제 1")

                for (doc in snapshot!!) {
                    Log.e(" 스토리지 삭제 ", " 삭제 3 "+doc)
                    val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                    Log.e(" 스토리지 삭제 ", " 삭제 2 "+chattingVO.imageMsg)
                    chattingVO.imageMsg?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it).delete() }
                }

              //  chattingMyUidDel()
            }*/

      /*  db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).collection(resources.getString(R.string.db_comment)).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("TWS @@@ ", "${document.id} => ${document.data}")
                }
            }*/
            /*.addOnSuccessListener { document ->

                val chattingVO: ChattingVO? = document.toObject(ChattingVO::class.java)

            }
            .addOnSuccessListener {

                for(i in DocumentSnapshot){
                    val chattingVO: ChattingVO? = it.toObject(ChattingVO::class.java)
                }

            }
        val photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(mImageUrl)*/
        //FirebaseStorage.getInstance().getReference("Chatting")
        //mStorageRef!!.child(chatRoomUid).getFile()

    }

    // product채팅리스트 삭제
    private fun productChattingListDel(){
        db.collection(resources.getString(R.string.db_product)).document(pid).get()
            .addOnSuccessListener {
                val productEntity: ProductEntity? = it.toObject(ProductEntity::class.java)

                val arr : MutableList<String> = productEntity!!.chattingRoom
                for(i in 0 until arr.size){
                    if(arr[i] == chatRoomUid){
                        arr.removeAt(i)
                        break
                    }
                }
                val delete = hashMapOf<String, Any>(
                    "chattingRoom" to arr
                )

                db.collection(resources.getString(R.string.db_product)).document(pid).update(delete)

            }


    }

    private fun chattingMyUidDel(){
        db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).get()
            .addOnSuccessListener { document ->
                val chatRoomVO: ChatRoomVO? = document.toObject(ChatRoomVO::class.java)
                if (chatRoomVO != null) {
                    pid = chatRoomVO.pid!!
                }
                if(myUid == chatRoomVO?.buyer){
                    when {
                        myUid == chatRoomVO.seller -> {
                            document.reference.collection("comment")
                                .addSnapshotListener{ snapshot, _ ->
                                    for (doc in snapshot!!) {
                                        val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                                        if(chattingVO.imageMsg!=""){
                                            chattingVO.imageMsg?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it).delete() }
                                        }
                                    }
                                    document.reference.collection("comment").get()
                                        .addOnSuccessListener { result ->
                                            for(documents in result){
                                                documents.reference.delete().addOnSuccessListener {
                                                    productChattingListDel()
                                                    db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).delete().addOnSuccessListener {
                                                        finish()
                                                    }
                                                    // storageImageDel()
                                                }


                                            }
                                        }
                                }
                            //chattingCommentDel(document.reference)

                        }
                        chatRoomVO.seller == "" -> {
                            //chattingCommentDel(document.reference)
                            /*document.reference.collection("comment").get()
                                .addOnSuccessListener { result ->
                                    for(documents in result){
                                        documents.reference.delete().addOnSuccessListener {
                                            db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).delete().addOnSuccessListener {
                                                finish()
                                            }
                                            productChattingListDel()
                                           // storageImageDel()
                                        }


                                    }
                                }*/
                            document.reference.collection("comment")
                                .addSnapshotListener{ snapshot, _ ->
                                    for (doc in snapshot!!) {
                                        val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                                        if(chattingVO.imageMsg!=""){
                                            chattingVO.imageMsg?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it).delete() }
                                        }
                                    }
                                    document.reference.collection("comment").get()
                                        .addOnSuccessListener { result ->
                                            for(documents in result){
                                                documents.reference.delete().addOnSuccessListener {
                                                    productChattingListDel()
                                                    db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).delete().addOnSuccessListener {
                                                        finish()
                                                    }
                                                    // storageImageDel()
                                                }


                                            }
                                        }
                                }
                        }
                        else -> {
                            db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid)
                                .update("buyer", "").addOnSuccessListener { finish() }

                        }
                    }

                }else{
                    if(chatRoomVO?.buyer == ""){
                        //chattingCommentDel(document.reference)
                        document.reference.collection("comment")
                            .addSnapshotListener{ snapshot, _ ->
                                for (doc in snapshot!!) {
                                    val chattingVO: ChattingVO = doc.toObject(ChattingVO::class.java)
                                    if(chattingVO.imageMsg!=""){
                                        chattingVO.imageMsg?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it).delete() }
                                    }
                                }
                                document.reference.collection("comment").get()
                                    .addOnSuccessListener { result ->
                                        for(documents in result){
                                            documents.reference.delete().addOnSuccessListener {
                                                productChattingListDel()
                                                db.collection(resources.getString(R.string.db_chatting)).document(chatRoomUid).delete().addOnSuccessListener {
                                                    finish()
                                                }
                                                // storageImageDel()
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
