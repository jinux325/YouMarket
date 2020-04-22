package com.u.marketapp.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.BR
import com.u.marketapp.R
import com.u.marketapp.adapter.CommentRVAdapter
import com.u.marketapp.chat.FCM
import com.u.marketapp.databinding.ActivityReplyBinding
import com.u.marketapp.entity.CommentEntity
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.listener.EndlessRecyclerViewScrollListener
import com.u.marketapp.utils.BaseApplication
import kotlinx.android.synthetic.main.activity_reply.*

class ReplyActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = ReplyActivity::class.java.simpleName
        private const val REQUEST_ITEM_LIMIT = 30L
    }

    private lateinit var adapter: CommentRVAdapter
    private lateinit var binding: ActivityReplyBinding
    private lateinit var document: DocumentSnapshot
    private lateinit var pid: String
    private lateinit var cid: String
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private var checkCurrentComment: Boolean = false
    private var checkUseContext: Boolean = false

    // 새로고침
    override fun onRefresh() {
        adapter.clear()
        requestItems()
        binding.swipRefreshLayout.isRefreshing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_reply
        )
        binding.swipRefreshLayout.setOnRefreshListener(this)
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
        requestItems()
        setButtonListener() // 버튼 클릭 설정
        setEditTextChangedListener()
    }

    // 액션바
    private fun setActionbar() {
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar!!
        actionbar.title = resources.getString(R.string.reply_app_title)
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayShowTitleEnabled(false)
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

    // 리사이클뷰 설정
    private fun setRVLayoutManager() {
        binding.recyclerView.apply {
            setHasFixedSize(true)
            val linearlayout = LinearLayoutManager(context)
            layoutManager = linearlayout
            scrollListener = object : EndlessRecyclerViewScrollListener(linearlayout) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    requestPagingItems(totalItemsCount-1, false)
                }
            }
            addOnScrollListener(scrollListener)
        }
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

    // 데이터 로드
    private fun requestItems() {
        scrollListener.resetState()

        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid)
            .collection(resources.getString(R.string.db_reply))
            .orderBy("regDate", Query.Direction.ASCENDING)
            .limit(REQUEST_ITEM_LIMIT)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                val items = documentSnapshots.documents
                for (item in items) {
                    adapter.addItem(item)
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 데이터 로드
    private fun requestPagingItems(next: Int, isScroll: Boolean) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid)
            .collection(resources.getString(R.string.db_reply))
            .orderBy("regDate", Query.Direction.ASCENDING)
            .startAfter(adapter.getItem(next))
            .limit(REQUEST_ITEM_LIMIT)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                val items = documentSnapshots.documents
                for (item in items) {
                    adapter.addItem(item)
                }
                if (isScroll) {
                    if (items.size <= 0) BaseApplication.instance.progressOFF()
                    requestPagingItems(adapter.itemCount-1, (items.size > 0))
                    binding.recyclerView.smoothScrollToPosition(adapter.itemCount-1)
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 추가 버튼 설정
    private fun setButtonListener() {
        // 작성 버튼
        text_view_add_input.setOnClickListener(null)
        // 설정 버튼
        image_view_more.setOnClickListener {
            checkCurrentComment = true
            openContext(it)
        }
    }

    // 추가 버튼 클릭 이벤트
    private val clickListener = View.OnClickListener {
        if (edit_text_input.text.toString().trim().isNotEmpty()) {
            val msg = edit_text_input.text.toString()
            Log.i(TAG, "input : $msg")
            val isReply = ::cid.isInitialized
            addComment(getData(msg, isReply))
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
        db.collection(resources.getString(R.string.db_product)).document(pid).collection(resources.getString(
            R.string.db_comment
        )).document(cid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val item = task.result!!.toObject(CommentEntity::class.java)
                getCurrentUser(item!!.user)
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
            .collection(resources.getString(R.string.db_reply)).add(item).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Added Comment ID : ${task.result!!.id}")
                updateCommentSize(1)
                updateReplySize(1)
                clearEditText()
                addAdapterComment(task.result!!.id)
                getToken(item.contents)
            }
        }
    }

    // 어댑터에 추가
    private fun addAdapterComment(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid)
            .collection(resources.getString(R.string.db_reply)).document(documentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                endPageScroll(documentSnapshot)
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    private fun endPageScroll(documentSnapshot: DocumentSnapshot) {
        if (adapter.itemCount >= REQUEST_ITEM_LIMIT) {
            BaseApplication.instance.progressON(this, resources.getString(
                R.string.loading
            ))
            requestPagingItems(adapter.itemCount-1, true)
        } else {
            adapter.addItem(documentSnapshot)
            binding.recyclerView.smoothScrollToPosition(adapter.itemCount-1)
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
                    task.result!!.reference.collection(resources.getString(R.string.db_reply)).get().addOnCompleteListener {
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
                    val intent = Intent()
                    intent.putExtra("cid", cid)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
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
            }
        }
    }

    private fun delAdapterItem() {
        adapter.removeItem(document)
    }

    private fun getToken(msg: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get()
            .addOnSuccessListener { documentSnapshot ->
                val item = documentSnapshot.toObject(ProductEntity::class.java)
                item?.let {
                    if (item.seller != FirebaseAuth.getInstance().currentUser!!.uid) {
                        getTargetUser(item.seller, msg)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }

    }

    private fun getTargetUser(uid: String, msg: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(uid).get()
            .addOnSuccessListener { documentSnapshot ->
                val item = documentSnapshot.toObject(UserEntity::class.java)
                item?.let { getCurrentUser(item.token, msg) }
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    private fun getCurrentUser(token: String, msg: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(FirebaseAuth.getInstance().currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                val item = documentSnapshot.toObject(UserEntity::class.java)
                item?.let { sendFCM(token, item.name, msg) }
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    private fun sendFCM(token: String, name: String, msg: String) {
        val fcm = FCM(token, name, msg, pid, "",resources.getString(R.string.ReplyActivity))
        fcm.start()
    }

    private fun updateCommentSize(num: Long) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("commentSize", FieldValue.increment(num)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Added Comment Size!")
            }
        }
    }

    private fun updateReplySize(num: Long) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid).update("replySize", FieldValue.increment(num)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Added Reply Size!")
            }
        }
    }

    // 입력에 따른 버튼 활성화
    private fun setEditTextChangedListener() {
        edit_text_input.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().trim().isNotEmpty()) {
                    Log.i(TAG, "입력된 문자열이 존재 한다!!")
                    text_view_add_input.setOnClickListener(clickListener)
                    layout_add_input.background.setTint(ContextCompat.getColor(applicationContext, R.color.hintcolor))
                } else {
                    Log.i(TAG, "입력된 문자열이 존재하지 않음!!")
                    text_view_add_input.setOnClickListener(null)
                    layout_add_input.background.setTint(ContextCompat.getColor(applicationContext, R.color.txt_white_gray))
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
                Toast.makeText(this, resources.getString(R.string.feature_to_be_added), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_delete -> {
                Log.i(TAG, "action_delete!!")
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
