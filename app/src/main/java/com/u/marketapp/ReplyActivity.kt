package com.u.marketapp

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
    private lateinit var pid: String
    private var checkUseContext: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reply)
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
        setItemsData()
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
        adapter.setItemClickListener(object : CommentRVAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int) {
                Log.i(TAG, "Item Click : $position")
            }
        })
        adapter.setMoreClickListener(object : CommentRVAdapter.MoreClickListener {
            override fun onClick(view: View, position: Int) {
                Log.i(TAG, "More Click : $position")
                if (adapter.getItem(position).toObject(CommentEntity::class.java)!!.user == FirebaseAuth.getInstance().currentUser!!.uid) checkUseContext = true
                registerForContextMenu(view)
                openContextMenu(view)
            }
        })
        adapter.setReplyClickListener(object : CommentRVAdapter.ReplyClickListener {
            override fun onClick(view: View, position: Int) {
                Log.i(TAG, "Reply Click : $position")
            }
        })
    }

    // 리사이클뷰 설정
    private fun setRVLayoutManager() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.setHasFixedSize(true)
    }

    // 데이터 설정
    private fun setItemsData() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).collection(resources.getString(R.string.db_comment)).orderBy("regDate", Query.Direction.ASCENDING).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.documents!!.size > 0) {
                    for(document in it.result?.documents!!) {
                        Log.i(TAG, "Added Comment : ${document.id}")
                        adapter.addItem(document)
                    }
                    binding.recyclerView.smoothScrollToPosition(adapter.itemCount-1)
                }
            }
        }
    }

    // 추가 버튼 설정
    private fun setButtonListener() {
        // 작성 버튼
        text_view_add_input.setOnClickListener {
            val msg = edit_text_input.text.toString()
            Log.i(TAG, "input : $msg")
            addComment(getData(msg, false))
        }
    }

    private fun getData(msg: String, reply: Boolean) : CommentEntity {
        val comment = CommentEntity()
        comment.user = FirebaseAuth.getInstance().currentUser!!.uid // 사용자 정보
        comment.contents = msg // 내용
        comment.reply = reply
        return comment
    }

    // 데이터베이스 추가
    private fun addComment(item: CommentEntity) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).collection(resources.getString(R.string.db_comment)).add(item).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Added Comment ID : ${it.result!!.id}")
                item.contents?.let { it1 -> getToken(it1) }
                refresh()
            }
        }
    }

    // 데이터베이스 삭제
    private fun delComment() {

    }

    private fun refresh() {
        adapter.clear()
        addCommentSize()
        setItemsData()
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

    private fun addCommentSize() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("commentSize", FieldValue.increment(1)).addOnCompleteListener {
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
            R.id.action_show_profile -> {
                Log.i(TAG, "action_show_profile!!")
                true
            }
            R.id.action_declaration -> {
                Log.i(TAG, "action_declaration!!")
                true
            }
            R.id.action_delete -> {
                Log.i(TAG, "action_delete!!")
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
