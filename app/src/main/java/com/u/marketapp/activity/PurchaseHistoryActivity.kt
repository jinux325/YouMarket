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
import com.u.marketapp.adapter.PurchaseHistoryRVAdapter
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.activity_purchase_history.*

class PurchaseHistoryActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = PurchaseHistoryActivity::class.java.simpleName
        private const val REQUEST_PRODUCT = 100
    }

    private lateinit var adapter : PurchaseHistoryRVAdapter
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
        setContentView(R.layout.activity_purchase_history)
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
        actionbar.title = "구매내역"
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
            adapter = PurchaseHistoryRVAdapter()
            recycler_view.adapter = adapter
            adapter.setItemClickListener(object : PurchaseHistoryRVAdapter.ItemClickListener {
                override fun onClick(view: View, position: Int) {
                    selectPosition = position
                    Log.i(TAG, "Item Click : $selectPosition")
                    moveProductActivity(adapter.getItem(position).id)
                }
            })
            adapter.setMoreClickListener(object : PurchaseHistoryRVAdapter.MoreClickListener {
                override fun onClick(view: View, position: Int) {
                    selectPosition = position
                    Log.i(TAG, "More Click : $selectPosition")
                    registerForContextMenu(view)
                    openContextMenu(view)
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
                val array = user.purchaseArray
                if (array.size > 0) {
                    for (item in array) {
                        getProductItem(item)
                    }
                    swip_refresh_layout.isRefreshing = false
                } else {
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

    // 구매 상품 제거
    private fun removeAttentionProductItem(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user))
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .update("purchaseArray", FieldValue.arrayRemove(pid))
            .addOnSuccessListener {
                removePurchaseUserItem(pid)
                Log.i(TAG, "유저목록에서 ${selectPosition}번 상품 구매 해제")
                Toast.makeText(this, "구매목록에서 제거하였습니다.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 구매 상품 추가
    private fun addPurchaseProductItem(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user))
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .update("purchaseArray", FieldValue.arrayUnion(pid))
            .addOnSuccessListener {
                addPurchaseUserItem(pid)
                Log.i(TAG, "유저목록에 ${selectPosition}번 상품 구매 추가")
                Toast.makeText(this, "구매목록에 추가하였습니다.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 구매 유저 추가
    private fun addPurchaseUserItem(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product))
            .document(pid)
            .update("purchase", FieldValue.arrayUnion(FirebaseAuth.getInstance().currentUser!!.uid))
            .addOnSuccessListener {
                Log.i(TAG, "${selectPosition}번 상품 구매 추가")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 구매 유저 제거
    private fun removePurchaseUserItem(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product))
            .document(pid)
            .update("purchase", FieldValue.arrayRemove(FirebaseAuth.getInstance().currentUser!!.uid))
            .addOnSuccessListener {
                Log.i(TAG, "${selectPosition}번 상품 구매 제거")
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
                if (user.purchaseArray.contains(pid)) {
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
