package com.u.marketapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.adapter.ViewPagerAdapter
import com.u.marketapp.chat.ChatActivity
import com.u.marketapp.databinding.ActivityProductBinding
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.activity_product.*

class ProductActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private val TAG = ProductActivity::class.java.simpleName
    }

    private lateinit var pid: String // 상품 문서 ID
    private lateinit var uid: String // 판매자 문서 ID
    private lateinit var userName: String // 판매자 닉네임
    private lateinit var binding: ActivityProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product)

        setActionBar()

        if (intent.hasExtra("itemId")) {
            pid = intent.getStringExtra("itemId")
            Log.i(TAG, pid)
            getProductData(pid)
            button_chatting.setOnClickListener(this)
        } else {
            Log.i(TAG, "Intent Not Signal!!")
        }

    }

    // 액션바 정의
    private fun setActionBar() {
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar!!
        // set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayShowTitleEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_product, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> { // 공유
                true
            }
            R.id.action_refresh -> { // 새로고침
                getProductData(pid)
                true
            }
            R.id.action_do_not_see -> { // 이 사용자의 글 보지 않기
                true
            }
            android.R.id.home -> { // 뒤로가기
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_chatting -> { // 거래하기 버튼
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("pid", pid)
                intent.putExtra("seller", uid)
                intent.putExtra("name", userName)
                startActivity(intent)
            }
        }
    }

    // 상품 데이터 가져오기
    private fun getProductData(pid: String?) {
        val db = FirebaseFirestore.getInstance()
        pid?.let {
            db.collection(resources.getString(R.string.db_product)).document(it).get().addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    Log.i(TAG, "Successful! ${document.result!!.id}")
                    val item = document.result!!.toObject(ProductEntity::class.java)!!
                    binding.setVariable(BR.product, item)
                    uid = item.seller.toString()
                    getUserData(item.seller)
                    setPagerAdater(item.imageArray)
                }
            }
        }
    }

    // 유저 정보 가져오기
    private fun getUserData(uid: String?) {
        val db = FirebaseFirestore.getInstance()
        uid?.let {
            db.collection(resources.getString(R.string.db_user)).document(it).get().addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    Log.i(TAG, "Successful! ${document.result!!.id}")
                    val user = document.result!!.toObject(UserEntity::class.java)
                    userName = user!!.name.toString()
                    binding.setVariable(BR.user, user)
                }
            }
        }
    }

    // 이미지 페이저 정의
    private fun setPagerAdater(array: ArrayList<String>?) {
        if (array != null && array.size > 0) {
            view_pager.visibility = View.VISIBLE
            val viewPagerAdapter = ViewPagerAdapter()
            viewPagerAdapter.addImageList(array)
            view_pager.adapter = viewPagerAdapter
            viewPagerAdapter.itemClick = object : ViewPagerAdapter.ItemClick {
                override fun onClick(view: View, position: Int) {
                    Log.i(TAG, "image path : ${viewPagerAdapter.getImage(position)}")
                }
            }
        } else {
            view_pager.visibility = View.GONE

        }
    }

}
