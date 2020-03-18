package com.u.marketapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
        private const val REQUEST_UPDATE = 100
    }

    private lateinit var pid: String // 상품 문서 ID
    private lateinit var uid: String // 판매자 문서 ID
    private lateinit var userName: String // 판매자 닉네임
    private lateinit var currentUid: String // 유저 ID
    private lateinit var binding: ActivityProductBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product)
        setActionBar()
        currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        if (intent.hasExtra("itemId")) {
            pid = intent.getStringExtra("itemId")
            Log.i(TAG, pid)
            getProductData()
            button_chatting.setOnClickListener(this)
            setAttentionCheckListener()
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

    // 소유권 확인
    private fun userVerification(menu: Menu?) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(ProductEntity::class.java)
                if (currentUid == item?.seller) {
                    menuInflater.inflate(R.menu.toolbar_product_current, menu)
                } else {
                    menuInflater.inflate(R.menu.toolbar_product, menu)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        userVerification(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> { // 공유
                share()
                true
            }
            R.id.action_refresh -> { // 새로고침
                getProductData()
                true
            }
            R.id.action_declaration -> { // 신고하기
                Toast.makeText(this, "기능 추가 예정입니다.", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_do_not_see -> { // 이 사용자의 글 보지 않기
                Toast.makeText(this, "기능 추가 예정입니다.", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_hide -> { // 숨기기
                unActiveProduct()
                true
            }
            R.id.action_update -> { // 수정
                updateProduct()
                true
            }
            R.id.action_delete -> { // 삭제
                deleteProduct()
                true
            }
            android.R.id.home -> { // 뒤로가기
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 조회 확인
    private fun checkLookup(lookup: ArrayList<String>?) {
        if (lookup.isNullOrEmpty()) {
            addLookup()
        } else {
            var isCheck = false
            for (k in lookup) {
                if (k == currentUid) {
                    isCheck = true
                    break
                }
            }
            if (!isCheck) {
                addLookup()
            }
        }
    }

    // 조회 추가
    private fun addLookup() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("lookup", FieldValue.arrayUnion(currentUid)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "조회 추가 : $currentUid")
                setLookupSize()
            }
        }
    }

    // 조회 수 입력
    private fun setLookupSize() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(ProductEntity::class.java)
                item?.let { a -> text_view_lookup.text = String.format(resources.getString(R.string.lookup_format), a.lookup?.size) }
            }
        }
    }

    // 관심 확인
    private fun checkAttention(attention: ArrayList<String>?) {
        if (!attention.isNullOrEmpty()) {
            var isCheck = false
            for (k in attention) {
                if (k == currentUid) {
                    isCheck = true
                    break
                }
            }
            setAttentionCheck(isCheck)
        }
    }

    // 관심 체크
    private fun setAttentionCheck(isCheck: Boolean) {
        check_box_attention.isChecked = isCheck
    }

    // 관심 체크 리스너
    private fun setAttentionCheckListener() {
        check_box_attention.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                addAttention()
            } else {
                delAttention()
            }
        }
    }

    // 관심 추가
    private fun addAttention() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("attention", FieldValue.arrayUnion(currentUid)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "관심 추가 : $currentUid")
                setAttentionSize()
            }
        }
    }

    // 관심 제거
    private fun delAttention() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("attention", FieldValue.arrayRemove(currentUid)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "관심 제거 : $currentUid")
                setAttentionSize()
            }
        }
    }

    // 조회 수 입력
    private fun setAttentionSize() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(ProductEntity::class.java)
                item?.let { a -> text_view_attention.text = String.format(resources.getString(R.string.attention_format), a.attention?.size) }
            }
        }
    }

    // 링크 공유
    private fun share() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "jpg/image"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(viewPagerAdapter.getImage(view_pager.currentItem)))
        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
    }

    // 상품 수정
    private fun updateProduct() {
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("pid", pid)
        startActivityForResult(intent, REQUEST_UPDATE)
    }

    // 상품 비활성화
    private fun unActiveProduct() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("status", "unactive").addOnCompleteListener { document ->
            if (document.isSuccessful) {
                Log.i(TAG, "상품 비활성화!")
            }
        }
    }

    // 상품 삭제
    private fun deleteProduct() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).delete().addOnCompleteListener { document ->
            if (document.isSuccessful) {
                Log.i(TAG, "상품 삭제!")
                finish() // 종료
            }
        }
    }

    // 상품 데이터 가져오기
    private fun getProductData() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener { document ->
            if (document.isSuccessful) {
                Log.i(TAG, "Successful! ${document.result!!.id}")
                val item = document.result!!.toObject(ProductEntity::class.java)!!
                binding.setVariable(BR.product, item)
                uid = item.seller.toString()
                getUserData()
                setPagerAdater(item.imageArray)
                checkLookup(item.lookup)
                checkAttention(item.attention)
            }
        }
    }

    // 유저 정보 가져오기
    private fun getUserData() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(uid).get().addOnCompleteListener { document ->
            if (document.isSuccessful) {
                Log.i(TAG, "Successful! ${document.result!!.id}")
                val user = document.result!!.toObject(UserEntity::class.java)
                userName = user?.name.toString()
                binding.setVariable(BR.user, user)
            }
        }
    }

    // 이미지 페이저 정의
    private fun setPagerAdater(array: ArrayList<String>?) {
        if (array != null && array.size > 0) {
            view_pager.visibility = View.VISIBLE
            viewPagerAdapter = ViewPagerAdapter()
            viewPagerAdapter.addImageList(array)
            view_pager.adapter = viewPagerAdapter
            viewPagerAdapter.itemClick = object : ViewPagerAdapter.ItemClick {
                override fun onClick(view: View, position: Int) {
                    intentViewPager(array, position)
                }
            }
        } else {
            view_pager.visibility = View.GONE
        }
    }

    // 이미지 뷰 페이저 인텐트
    private fun intentViewPager(array: ArrayList<String>?, position: Int) {
        val intent = Intent(this, ViewActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("imageArray", array)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_UPDATE -> {
                    getProductData()
                }
            }
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
}
