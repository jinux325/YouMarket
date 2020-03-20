package com.u.marketapp

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.u.marketapp.adapter.CommentRVAdapter
import com.u.marketapp.databinding.ActivityReplyBinding
import kotlinx.android.synthetic.main.activity_reply.*

class ReplyActivity : AppCompatActivity() {

    companion object {
        private val TAG = ReplyActivity::class.java.simpleName
    }

    private lateinit var adapter: CommentRVAdapter
    private lateinit var binding: ActivityReplyBinding
    private lateinit var pid: String

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
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.setHasFixedSize(true)
    }

    // 추가 버튼 설정
    private fun setButtonListener() {
        // 작성 버튼
        text_view_add_input.setOnClickListener {
            val msg = edit_text_input.text.toString()
            Log.i(TAG, "input : $msg")
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
}
