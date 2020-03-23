package com.u.marketapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.adapter.ProductRVAdapter
import com.u.marketapp.databinding.FragmentHomeBinding
import com.u.marketapp.listener.EndlessRecyclerViewScrollListener
import com.u.marketapp.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        private const val REQUEST_PRODUCT = 100
        private const val REQUEST_FILTER = 200
        private const val REQUEST_ITEM_LIMIT = 20L
    }

    private lateinit var actionbar: ActionBar
    private lateinit var adapter : ProductRVAdapter
    private lateinit var binding : FragmentHomeBinding
    private lateinit var scrollListener: EndlessRecyclerViewScrollListener

    // 새로고침
    override fun onRefresh() {
        adapter.clear()
        requestItems()
        binding.swipRefreshLayout.isRefreshing = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 데이터 바인딩
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        setRVAdapter()
        setRVLayoutManager()
        requestItems()
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.action_filter -> {
                moveFilterAcitity()
                true
            }
            R.id.action_notification -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        setActionbar()
        binding.swipRefreshLayout.setOnRefreshListener(this)
    }

    // 액션바
    private fun setActionbar() {
        if (activity is AppCompatActivity) {
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
            actionbar = (activity as AppCompatActivity).supportActionBar!!
            actionbar.setDisplayShowTitleEnabled(false)
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
                    requestPagingItems(totalItemsCount-1)
                }
            }
            addOnScrollListener(scrollListener)
        }
    }

    // 리사이클뷰 어댑터 설정
    private fun setRVAdapter() {
        if (binding.recyclerView.adapter == null) {
            adapter = ProductRVAdapter()
            binding.recyclerView.adapter = adapter
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

        val list = SharedPreferencesUtils.instance.getStringArrayPref(requireContext(), "category")
        val db = FirebaseFirestore.getInstance()
        var query = db.collection(resources.getString(R.string.db_product))
            .whereEqualTo("status", true)
            .orderBy("regDate", Query.Direction.DESCENDING)
            .limit(REQUEST_ITEM_LIMIT)

        if (!list.isNullOrEmpty()) {
            Log.i(TAG, "COUNT : ${list.size} -> $list")
            query = query.whereIn("category", list)
        }

        query.get()
            .addOnSuccessListener { documentSnapshots ->
                val items = documentSnapshots.documents
                checkItemsData(items.size == 0)
                for (item in items) {
                    adapter.addItem(item)
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 데이터 로드
    private fun requestPagingItems(next: Int) {
        val list = SharedPreferencesUtils.instance.getStringArrayPref(requireContext(), "category")
        val db = FirebaseFirestore.getInstance()
        var query = db.collection(resources.getString(R.string.db_product))
            .whereEqualTo("status", true)
            .orderBy("regDate", Query.Direction.DESCENDING)
            .startAfter(adapter.getItem(next))
            .limit(REQUEST_ITEM_LIMIT)

        if (!list.isNullOrEmpty()) {
            Log.i(TAG, "COUNT : ${list.size} -> $list")
            query = query.whereIn("category", list)
        }

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
        val intent = Intent(context, ProductActivity::class.java)
        intent.putExtra("id", id)
        startActivityForResult(intent, REQUEST_PRODUCT)
    }

    // 필터 페이지 이동
    private fun moveFilterAcitity() {
        val intent = Intent(context, FilterActivity::class.java)
        startActivityForResult(intent, REQUEST_FILTER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PRODUCT -> { // 상품 페이지에서 리턴
                    adapter.clear()
                    requestItems()
                }
                REQUEST_FILTER -> { // 필터 페이지에서 리턴
                    val list = SharedPreferencesUtils.instance.getStringArrayPref(requireContext(), "category")
                    Log.i(TAG, list.toString())
                    adapter.clear()
                    requestItems()
                }
            }
        }
    }

}
