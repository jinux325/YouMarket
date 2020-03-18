package com.u.marketapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.adapter.ProductRVAdapter
import com.u.marketapp.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
    }

    private lateinit var adapter : ProductRVAdapter
    private lateinit var binding : FragmentHomeBinding

    // 새로고침
    override fun onRefresh() {
        adapter.clear()
        setItemsData()
        binding.swipRefreshLayout.isRefreshing = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 데이터 바인딩
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        setRVAdapter()
        setRVLayoutManager()
        setItemsData()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.swipRefreshLayout.setOnRefreshListener(this)
    }

    // 리사이클뷰 레이아웃 매니저 설정
    private fun setRVLayoutManager() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.setHasFixedSize(true)
    }

    // 리사이클뷰 어댑터 설정
    private fun setRVAdapter() {
        adapter = ProductRVAdapter()
        binding.recyclerView.adapter = adapter
        adapter.setItemClickListener(object : ProductRVAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int) {
                moveActivity(adapter.getItem(position))
            }
        })
    }

    // 데이터 설정
    private fun setItemsData() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).whereEqualTo("status", "active").orderBy("regDate", Query.Direction.DESCENDING).limit(30).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result?.documents!!.size > 0) {
                    checkItemsData(true)
                    for(document in it.result?.documents!!) {
                        adapter.addItem(document)
                    }
                } else {
                    checkItemsData(false)
                }
            }
        }
    }

    // 상품 존재 여부
    private fun checkItemsData(isCheck: Boolean) {
        if (isCheck) {
            recycler_view.visibility = View.VISIBLE
            text_view_empty.visibility = View.GONE
        } else {
            recycler_view.visibility = View.GONE
            text_view_empty.visibility = View.VISIBLE
        }
    }

    // 상품 상세 정보 페이지 이동
    private fun moveActivity(item: DocumentSnapshot) {
        Log.i(TAG, item.toString())
        val intent = Intent(context, ProductActivity::class.java)
        intent.putExtra("itemId", item.id)
        startActivity(intent)
    }

}
