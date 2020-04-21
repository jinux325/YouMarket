package com.u.marketapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.adapter.AttentionHistoryRVAdapter
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.activity_attention_history.*

class AttentionHistoryActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = AttentionHistoryActivity::class.java.simpleName
        private const val REQUEST_PRODUCT = 100
    }
    private lateinit var adapter : AttentionHistoryRVAdapter

    private lateinit var actionbar: ActionBar
    private var backPressedTime: Long = 200
    private var selectPosition: Int = -1

    // 새로고침
    override fun onRefresh() {
        adapter.clear()
        requestItems()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attention_history)
        initView()
    }

    private fun initView() {
        setActionBar()
        setRVLayoutManager()
        setRVAdapter()
        swip_refresh_layout.setOnRefreshListener(this)
        checkItemsData(true)
        requestItems()
    }

    // 액션바
    private fun setActionBar() {
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        actionbar.title = "관심목록"
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayShowTitleEnabled(false)
    }

    // 리사이클뷰 레이아웃 매니저 설정
    private fun setRVLayoutManager() {
        recycler_view.apply {
            setHasFixedSize(true)
            val linearlayout = LinearLayoutManager(context)
            layoutManager = linearlayout
        }
    }

    // 리사이클뷰 어댑터 설정
    private fun setRVAdapter() {
        if (recycler_view.adapter == null) {
            adapter = AttentionHistoryRVAdapter()
            recycler_view.adapter = adapter
            adapter.setItemClickListener(object : AttentionHistoryRVAdapter.ItemClickListener {
                override fun onClick(view: View, position: Int) {
                    selectPosition = position
                    Log.i(TAG, "Item Click : $selectPosition")
                    moveProductActivity(adapter.getItem(position).id)
                }
            })
            adapter.setCheckBoxClickListener(object : AttentionHistoryRVAdapter.CheckBoxClickListener {
                override fun onClick(view: View, position: Int, isChecked: Boolean) {
                    selectPosition = position
                    Log.i(TAG, "Attention Click : $isChecked")
                    if (isChecked) {
                        addAttentionProductItem(adapter.getItem(position).id)
                    } else {
                        removeAttentionProductItem(adapter.getItem(position).id)
                    }
                }
            })
        }
    }

    // 데이터 로드
    private fun requestItems() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user))
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                val user = documentSnapshots.toObject(UserEntity::class.java)!!
                val array = user.attentionArray
                if (array.size > 0) {
                    Log.i(TAG, "상품이 있네~!@ : ${array.size}")
                    for (item in array) {
                        getProductItem(item)
                    }
                    swip_refresh_layout.isRefreshing = false
                } else {
                    Log.i(TAG, "상품이 없네~!@")
                    checkItemsData(true)
                    swip_refresh_layout.isRefreshing = false
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 상품 조회
    private fun getProductItem(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product))
            .document(pid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    if (getActiveProductItem(documentSnapshot)) {
                        adapter.addItem(documentSnapshot)
                        checkItemsData(false)
                    }
                } else {
                    Log.i(TAG, "No such DocumentSnapshot!")
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 관심 상품 제거
    private fun removeAttentionProductItem(pid: String) {
        if (selectPosition != -1) {
            val db = FirebaseFirestore.getInstance()
            db.collection(resources.getString(R.string.db_user))
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .update("attentionArray", FieldValue.arrayRemove(pid))
                .addOnSuccessListener {
                    removeAttentionUserItem(pid)
                    Log.i(TAG, "유저목록에서 관심 해제")
                    Toast.makeText(this, "관심목록에서 제거하였습니다.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                }
        }
    }

    // 관심 상품 추가
    private fun addAttentionProductItem(pid: String) {
        if (selectPosition != -1) {
            val db = FirebaseFirestore.getInstance()
            db.collection(resources.getString(R.string.db_user))
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .update("attentionArray", FieldValue.arrayUnion(pid))
                .addOnSuccessListener {
                    addAttentionUserItem(pid)
                    Log.i(TAG, "유저목록에 상품 관심 추가")
                    Toast.makeText(this, "관심목록에 추가하였습니다.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                }
        }
    }

    // 관심 유저 추가
    private fun addAttentionUserItem(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product))
            .document(pid)
            .update("attention", FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser!!.uid))
            .addOnSuccessListener {
                Log.i(TAG, "상품 관심 추가")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 관심 유저 제거
    private fun removeAttentionUserItem(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product))
            .document(pid)
            .update("attention", FieldValue.arrayRemove(FirebaseAuth.getInstance().currentUser!!.uid))
            .addOnSuccessListener {
                Log.i(TAG, "상품 관심 제거")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 상품 재정의
    private fun refreshItem() {
        if (selectPosition != -1) {
            val pid = adapter.getItem(selectPosition).id
            adapter.removeItem(selectPosition)
            checkGetItem(selectPosition, pid)
        }
    }

    // 상품 존재 확인
    private fun checkGetItem(position: Int, pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user))
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(UserEntity::class.java)!!
                if (user.attentionArray.contains(pid)) {
                    getItem(position, documentSnapshot.id)
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 상품 로드
    private fun getItem(position: Int, pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product))
            .document(pid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    if (getActiveProductItem(documentSnapshot)) {
                        adapter.addItem(position, documentSnapshot)
                    }
                } else {
                    Log.i(TAG, "No such DocumentSnapshot!")
                }
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 상품 활성화 확인
    private fun getActiveProductItem(document: DocumentSnapshot) : Boolean {
        val item = document.toObject(ProductEntity::class.java)!!
        return item.status
    }

    // 상품 존재 여부
    private fun checkItemsData(isCheck: Boolean) {
        if (!isCheck) {
            recycler_view.visibility = View.VISIBLE
            text_view_empty.visibility = View.GONE
        } else {
            recycler_view.visibility = View.GONE
            text_view_empty.visibility = View.VISIBLE
        }
    }

    // 상품 상세 정보 페이지 이동
    private fun moveProductActivity(id: String) {
        Log.i(TAG, "Document ID : $id")
        val intent = Intent(this, ProductActivity::class.java)
        intent.putExtra("id", id)
        startActivityForResult(intent, REQUEST_PRODUCT)
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backPressedTime + 2000) {
            backPressedTime = System.currentTimeMillis()
            showToastMessage()
            return
        }

        if (System.currentTimeMillis() <= backPressedTime + 2000) {
            this.finish()
        }
    }

    private fun showToastMessage() {
        Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "Selected Item : $selectPosition")
        refreshItem()
    }

}
