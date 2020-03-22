package com.u.marketapp

import android.app.Activity
import android.content.Intent
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
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.adapter.CommentRVAdapter
import com.u.marketapp.chat.FCM
import com.u.marketapp.databinding.ActivityCommentBinding
import com.u.marketapp.entity.CommentEntity
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.listener.EndlessRecyclerViewScrollListener
import kotlinx.android.synthetic.main.activity_reply.*

class CommentActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = CommentActivity::class.java.simpleName
        private const val REQUEST_REPLY = 100
        private const val REQUEST_ITEM_LIMIT = 13L
    }

    private lateinit var adapter: CommentRVAdapter
    private lateinit var binding: ActivityCommentBinding
    private lateinit var document: DocumentSnapshot
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private lateinit var pid: String
    private var checkUseContext: Boolean = false

    // 새로고침
    override fun onRefresh() {
        adapter.clear()
        requestItems()
        binding.swipRefreshLayout.isRefreshing = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        binding.swipRefreshLayout.setOnRefreshListener(this)
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
        requestItems()
        setButtonListener() // 버튼 클릭 설정
        setEditTextChangedListener()
    }

    // 액션바
    private fun setActionbar() {
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar!!
        actionbar.title = resources.getString(R.string.comment_app_title)
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
        adapter.setReplyClickListener(object : CommentRVAdapter.ReplyClickListener {
            override fun onClick(view: View, position: Int) {
                Log.i(TAG, "Reply Click : $position")
                document = adapter.getItem(position)
                moveReplyIntent()
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

    // 리사이클뷰 레이아웃 매니저 설정
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

    // 데이터 로드
    private fun requestItems() {
        scrollListener.resetState()

        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).collection(resources.getString(R.string.db_comment))
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
        db.collection(resources.getString(R.string.db_product)).document(pid).collection(resources.getString(R.string.db_comment))
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
        text_view_add_input.setOnClickListener {
            val msg = edit_text_input.text.toString()
            Log.i(TAG, "input : $msg")
            addComment(getData(msg))
        }
    }

    private fun getData(msg: String) : CommentEntity {
        val comment = CommentEntity()
        comment.user = FirebaseAuth.getInstance().currentUser!!.uid // 사용자 정보
        comment.contents = msg // 내용
        return comment
    }

    // 데이터베이스 추가
    private fun addComment(item: CommentEntity) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).collection(resources.getString(R.string.db_comment)).add(item).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Added Comment ID : ${task.result!!.id}")
                updateCommentSize(1)
                clearEditText()
                addAdapterComment(task.result!!.id)

                item.contents?.let { it1 -> getToken(it1) }
            }
        }
    }

    // 어댑터에 추가
    private fun addAdapterComment(documentId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(documentId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                endPageScroll(documentSnapshot)
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    private fun endPageScroll(documentSnapshot: DocumentSnapshot) {
        if (adapter.itemCount >= REQUEST_ITEM_LIMIT) {
            BaseApplication.instance.progressON(this, resources.getString(R.string.loading))
            requestPagingItems(adapter.itemCount-1, true)
        } else {
            adapter.addItem(documentSnapshot)
            binding.recyclerView.smoothScrollToPosition(adapter.itemCount-1)
        }
    }

    // 리플 상위 댓글 삭제
    private fun delComment(cid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).document(cid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                Log.i(TAG, "Delete Comment!!")
                adapter.removeItem(documentSnapshot)
                updateCommentSize(-1)
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 데이터베이스 삭제
    private fun delComment() {
        document.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Delete Comment!!")
                delAdapterItem()
                updateCommentSize(-1)
                val item = document.toObject(CommentEntity::class.java)
                if (item!!.replySize > 0) {
                    document.reference.collection(resources.getString(R.string.db_reply)).get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            for (document in it.result!!.documents) {
                                document.reference.delete().addOnCompleteListener { it1 ->
                                    if (it1.isSuccessful) {
                                        updateCommentSize(-1)
                                        Log.i(TAG, "Reply Deleted!")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun delAdapterItem() {
        adapter.removeItem(document)
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

    private fun moveReplyIntent() {
        val intent = Intent(this, ReplyActivity::class.java)
        intent.putExtra("pid", pid)
        intent.putExtra("cid", document.id)
        startActivityForResult(intent, REQUEST_REPLY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_REPLY -> {
                    val cid = data!!.getStringExtra("cid")
                    delComment(cid)
                }
            }
        }
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
                delComment()
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
