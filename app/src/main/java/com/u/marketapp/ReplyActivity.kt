package com.u.marketapp

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.adapter.CommentRVAdapter
import com.u.marketapp.chat.FCM
import com.u.marketapp.databinding.ActivityReplyBinding
import com.u.marketapp.entity.CommentEntity
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.activity_reply.*

class ReplyActivity : AppCompatActivity() {

    companion object {
        private val TAG = ReplyActivity::class.java.simpleName
    }

    private lateinit var adapter: CommentRVAdapter
    private lateinit var binding: ActivityReplyBinding
    private lateinit var document: DocumentSnapshot
    private lateinit var pid: String
    private lateinit var cid: String
    private var checkCurrentComment: Boolean = false
    private var checkUseContext: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reply)
        if (intent.hasExtra("cid")) {
            cid = intent.getStringExtra("cid")
        }
        if (intent.hasExtra("pid")) {
            pid = intent.getStringExtra("pid")
            initView()
        }
    }

    // 화면 초기화
    private fun initView() {
        setActionbar() // 액션바 설정
        setRVAdapter() // 어댑터 설정
        setRVLayoutManager() // 레이아웃 매니저 설정
        getCurrentComment()
        setItemsData(false)
        setButtonListener() // 버튼 클릭 설정
        setEditTextChangedListener()
    }

    // 액션바
    private fun setActionbar() {
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar!!
        actionbar.title = resources.getString(R.string.reply_app_title)
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    // 어댑터 설정
    private fun setRVAdapter() {
        adapter = CommentRVAdapter(this)
        binding.recyclerView.adapter = adapter
        adapter.setMoreClickListener(object : CommentRVAdapter.MoreClickListener {
            override fun onClick(view: View, position: Int) {
                Log.i(TAG, "More Click : $position")
                checkLicense(view, position)
            }
        })
    }

    // 권한 확인
    private fun checkLicense(view: View, position: Int) {
        if (adapter.getItem(position).toObject(CommentEntity::class.java)!!.user == FirebaseAuth.getInstance().currentUser!!.uid) {
            checkUseContext = true
            document = adapter.getItem(position)
            registerForContextMenu(view)
            openContextMenu(view)
        } else {
            val db = FirebaseFirestore.getInstance()
            db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val item = task.result!!.toObject(ProductEntity::class.java)!!
                    if (item.seller == FirebaseAuth.getInstance().currentUser!!.uid) {
                        checkUseContext = true
                        document = adapter.getItem(position)
                        registerForContextMenu(view)
                        openContextMenu(view)
                    } else {
                        document = adapter.getItem(position)
                        registerForContextMenu(view)
                        openContextMenu(view)
                    }
                }
            }
        }
    }

    // 리사이클뷰 설정
    private fun setRVLayoutManager() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.setHasFixedSize(true)
    }

    // 데이터 설정
    private fun setItemsData(isScroll: Boolean) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid)
            .collection(resources.getString(R.string.db_reply)).orderBy("regDate", Query.Direction.ASCENDING).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.documents!!.size > 0) {
                    for(document in it.result?.documents!!) {
                        Log.i(TAG, "Added Comment : ${document.id}")
                        adapter.addItem(document)
                    }
                    if (isScroll) binding.recyclerView.smoothScrollToPosition(adapter.itemCount-1)
                    BaseApplication.instance.progressOFF()
                } else {
                    BaseApplication.instance.progressOFF()
                }
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    // 추가 버튼 설정
    private fun setButtonListener() {
        // 작성 버튼
        text_view_add_input.setOnClickListener {
            BaseApplication.instance.progressON(this, resources.getString(R.string.loading))
            val msg = edit_text_input.text.toString()
            Log.i(TAG, "input : $msg")
            val isReply = ::cid.isInitialized
            addComment(getData(msg, isReply))
        }
        image_view_more.setOnClickListener {
            checkCurrentComment = true
            openContext(it)
        }
    }

    private fun openContext(view: View) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    document = task.result!!
                    val item = document.toObject(CommentEntity::class.java)!!
                    if (item.user == FirebaseAuth.getInstance().currentUser!!.uid) checkUseContext = true
                    registerForContextMenu(view)
                    openContextMenu(view)
                }
            }
    }

    private fun getData(msg: String, reply: Boolean) : CommentEntity {
        val comment = CommentEntity()
        comment.user = FirebaseAuth.getInstance().currentUser!!.uid // 사용자 정보
        comment.contents = msg // 내용
        comment.reply = reply
        return comment
    }

    // 현재 댓글 조회
    private fun getCurrentComment() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).collection(resources.getString(R.string.db_comment)).document(cid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val item = task.result!!.toObject(CommentEntity::class.java)
                getCurrentUser(item!!.user!!)
                binding.setVariable(BR.reply, item)
            }
        }
    }

    // 현재 댓글 유저 조회
    private fun getCurrentUser(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(uid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val item = task.result!!.toObject(UserEntity::class.java)
                binding.setVariable(BR.user, item)
            }
        }
    }

    // 데이터베이스 추가
    private fun addComment(item: CommentEntity) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid)
            .collection(resources.getString(R.string.db_reply)).add(item).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Added Comment ID : ${it.result!!.id}")
//                item.contents?.let { it1 -> getToken(it1) }
                refresh()
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    // 데이터베이스 삭제
    private fun delComment() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val item = task.result!!.toObject(CommentEntity::class.java)!!
                if (item.replySize > 0) {
                    task.result!!.reference.collection(resources.getString(R.string.db_reply)).get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                for (document in it.result!!.documents) {
                                    document.reference.delete().addOnCompleteListener { it1 ->
                                        if (it1.isSuccessful) {
                                            updateCommentSize(-1)
                                        }
                                    }
                                }
                            }
                        }
                    task.result!!.reference.delete().addOnCompleteListener {
                        if (it.isSuccessful) {
                            updateCommentSize(-1)
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            BaseApplication.instance.progressOFF()
                        }
                    }
                }
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    private fun delReply() {
        document.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Delete Reply!!")
                updateCommentSize(-1)
                updateReplySize(-1)
                delAdapterItem()
                BaseApplication.instance.progressOFF()
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    private fun delAdapterItem() {
        adapter.removeItem(document)
    }

    private fun refresh() {
        adapter.clear()
        updateReplySize(1)
        updateCommentSize(1)
        setItemsData(true)
        clearEditText()
    }

    private fun getToken(msg: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(ProductEntity::class.java)
                item?.let {
                    getTargetUser(item.seller, msg)
                }
            }
        }
    }

    private fun getTargetUser(uid: String?, msg: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(uid!!).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(UserEntity::class.java)
                item?.let { getCurrentUser(item.token, msg) }
            }
        }
    }

    private fun getCurrentUser(token: String?, msg: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(FirebaseAuth.getInstance().currentUser!!.uid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(UserEntity::class.java)
                item?.let { sendFCM(token, item.name, msg) }
            }
        }
    }

    private fun sendFCM(token: String?, name: String?, msg: String) {
        val fcm = FCM(token!!, name, msg, pid, "")
        fcm.start()
    }

    private fun updateCommentSize(num: Long) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("commentSize", FieldValue.increment(num)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Added Comment Size!")
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    private fun updateReplySize(num: Long) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid).update("replySize", FieldValue.increment(num)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Added Reply Size!")
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    // 입력에 따른 버튼 활성화
    private fun setEditTextChangedListener() {
        edit_text_input.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(TextUtils.isEmpty(s.toString())) {
                    text_view_add_input.isEnabled = false
                    layout_add_input.background.setTint(ContextCompat.getColor(applicationContext, R.color.txt_white_gray))
                } else {
                    text_view_add_input.isEnabled = true
                    layout_add_input.background.setTint(ContextCompat.getColor(applicationContext, R.color.hintcolor))
                }
            }
        })
    }

    private fun clearEditText() {
        edit_text_input.text.clear()
        edit_text_input.requestFocus()
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (checkUseContext) {
            menuInflater.inflate(R.menu.context_current_reply, menu)
        } else {
            menuInflater.inflate(R.menu.context_reply, menu)
        }
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_declaration -> {
                Log.i(TAG, "action_declaration!!")
                true
            }
            R.id.action_delete -> {
                Log.i(TAG, "action_delete!!")
                BaseApplication.instance.progressON(this, resources.getString(R.string.loading))
                if (checkCurrentComment) delComment()
                else delReply()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // 뒤로가기
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
