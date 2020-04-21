package com.u.marketapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.R
import com.u.marketapp.adapter.ProductRVAdapter
import com.u.marketapp.listener.EndlessRecyclerViewScrollListener
import kotlinx.android.synthetic.main.activity_category.*

class CategoryActivity : AppCompatActivity() {

    companion object {
        private val TAG = CategoryActivity::class.java.simpleName
        private const val REQUEST_PRODUCT = 100
        private const val REQUEST_ITEM_LIMIT = 20L
    }

    private lateinit var adapter : ProductRVAdapter
    private lateinit var scrollListener : EndlessRecyclerViewScrollListener
    private lateinit var category: String
    private lateinit var address: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)
        if (intent.hasExtra("title")) {
            setActionBar(intent.getStringExtra("title"))
            getSharedAddress()
            initView()
        }
    }

    // 액션바 정의
    private fun setActionBar(title: String) {
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar!!
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayShowTitleEnabled(false)
        text_view_app_title.text = title
        category = title
    }

    // 화면 초기화
    private fun initView() {
        setRVAdapter()
        setRVLayoutManager()
        requestItems()
    }

    private fun getSharedAddress() {
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val result = pref.getString("address", "내 동네 설정")!!
        Log.i(TAG, "주소 가져오기 : $result")
        address = result
    }

    // 리사이클뷰 레이아웃 매니저 설정
    private fun setRVLayoutManager() {
        recycler_view.apply {
            setHasFixedSize(true)
            val linearlayout = LinearLayoutManager(context)
            layoutManager = linearlayout
            scrollListener = object : EndlessRecyclerViewScrollListener(linearlayout) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    requestPagingItems(totalItemsCount-1)
                }
            }
            addOnScrollListener(scrollListener)
        }
    }

    // 리사이클뷰 어댑터 설정
    private fun setRVAdapter() {
        if (recycler_view.adapter == null) {
            adapter = ProductRVAdapter()
            recycler_view.adapter = adapter
            adapter.setItemClickListener(object : ProductRVAdapter.ItemClickListener {
                override fun onClick(view: View, position: Int) {
                    moveProductActivity(adapter.getItem(position).id)
                }
            })
        }
    }

    // 데이터 로드
    private fun requestItems() {
        scrollListener.resetState()
        val db = FirebaseFirestore.getInstance()
        val query = db.collection(resources.getString(R.string.db_product))
            .whereEqualTo("address", address)
            .whereEqualTo("category", category)
            .whereEqualTo("status", true)
            .orderBy("regDate", Query.Direction.DESCENDING)
            .limit(REQUEST_ITEM_LIMIT)

        query.get()
            .addOnSuccessListener { documentSnapshots ->
                val items = documentSnapshots.documents
                checkItemsData(items.size == 0)
                for (item in items) {
                    adapter.addItem(item)
                }
                swip_refresh_layout.isRefreshing = false
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 데이터 로드
    private fun requestPagingItems(next: Int) {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection(resources.getString(R.string.db_product))
            .whereEqualTo("address", address)
            .whereEqualTo("category", category)
            .whereEqualTo("status", true)
            .orderBy("regDate", Query.Direction.DESCENDING)
            .startAfter(adapter.getItem(next))
            .limit(REQUEST_ITEM_LIMIT)

        query.get()
            .addOnSuccessListener { documentSnapshots ->
                val items = documentSnapshots.documents
                for (item in items) {
                    adapter.addItem(item)
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 상품 존재 여부
    private fun checkItemsData(isCheck: Boolean) {
        if (!isCheck) {
            Log.i(TAG, "데이터 있음!!")
            recycler_view.visibility = View.VISIBLE
            text_view_empty.visibility = View.GONE
        } else {
            Log.i(TAG, "데이터 없음!!")
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

    // 검색 페이지 이동
    private fun moveSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_category, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_search -> {
                moveSearchActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
