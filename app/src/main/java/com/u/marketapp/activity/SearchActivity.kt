package com.u.marketapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.R
import com.u.marketapp.adapter.ProductRVAdapter
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.listener.EndlessRecyclerViewScrollListener
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    companion object {
        private val TAG = SearchActivity::class.java.simpleName
        private const val REQUEST_PRODUCT = 100
        private const val REQUEST_ITEM_LIMIT = 20L
    }

    private lateinit var adapter : ProductRVAdapter
    private lateinit var scrollListener : EndlessRecyclerViewScrollListener
    private var filterString = ""
    private var nowPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initView()
    }

    // 화면 초기화
    private fun initView() {
        setActionbar() // 액션바 설정
        setSearchView()
        setRVLayoutManager()
        setRVAdapter()
    }

    // 액션바
    private fun setActionbar() {
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        actionbar?.let {
            actionbar.title = resources.getString(R.string.title_search)
            actionbar.setDisplayShowTitleEnabled(false)
            actionbar.setDisplayHomeAsUpEnabled(true)
        }
    }

    // 검색창
    private fun setSearchView() {
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val address = pref.getString("address", "내 동네 설정")!!
        layout_text_input.hint = "'${address}' 근처에서 검색"
        edit_text_input.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    Log.i(TAG, "IME_ACTION_SEARCH Clicked!!!!")
                    nowPage = 1
                    adapter.clear()
                    filterString = edit_text_input.text.toString()
                    closeKeyboard()
                    requestItems()
                }
            }
            return@OnEditorActionListener true
        })
    }

    // 리사이클뷰 레이아웃 매니저 설정
    private fun setRVLayoutManager() {
        recycler_view.apply {
            setHasFixedSize(true)
            val linearlayout = LinearLayoutManager(context)
            layoutManager = linearlayout
            scrollListener = object : EndlessRecyclerViewScrollListener(linearlayout) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                    nowPage++
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
            .whereEqualTo("address", getSharedAddress())
            .whereEqualTo("status", true)
            .orderBy("regDate", Query.Direction.DESCENDING)
            .limit(REQUEST_ITEM_LIMIT)

        query.get()
            .addOnSuccessListener { documentSnapshots ->
                val items = documentSnapshots.documents
                checkItemsData(items.size == 0)
                searchList(items)
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 데이터 로드
    private fun requestPagingItems(next: Int) {
        val db = FirebaseFirestore.getInstance()
        val query = db.collection(resources.getString(R.string.db_product))
            .whereEqualTo("address", getSharedAddress())
            .whereEqualTo("status", true)
            .orderBy("regDate", Query.Direction.DESCENDING)
            .startAfter(adapter.getItem(next))
            .limit(REQUEST_ITEM_LIMIT)

        query.get()
            .addOnSuccessListener { documentSnapshots ->
                val items = documentSnapshots.documents
                searchList(items)
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    private fun searchList(list: List<DocumentSnapshot>) {
        val filterList = list.filter { documentSnapshot ->
            val vo = documentSnapshot.toObject(ProductEntity::class.java)!!
            checkCharacter(vo.title) || checkCharacter(vo.contents)
        }
        adapter.addAllItem(filterList)
        if (!filterList.isNullOrEmpty()) {
            val before = adapter.getItem(0)
            val now = filterList[0]
            if (before.id != now.id) {
                if (adapter.itemCount < (REQUEST_ITEM_LIMIT * nowPage)) {
                    requestPagingItems(adapter.itemCount-1)
                }
            }
        } else {
            if (adapter.itemCount > 0) {
                checkItemsData(false)
            } else {
                checkItemsData(true)
            }
        }
    }

    private fun checkCharacter(str: String) : Boolean {
        val array = filterString.split(" ")
        for (item in array) {
            if (str.contains(item)) return true
        }
        return false
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
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
        startActivityForResult(intent,
            REQUEST_PRODUCT
        )
    }

    private fun getSharedAddress() : String {
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val result = pref.getString("address", "내 동네 설정")!!
        Log.i(TAG, "주소 가져오기 : $result")
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
