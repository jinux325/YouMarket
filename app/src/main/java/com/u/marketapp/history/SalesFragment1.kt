package com.u.marketapp.history

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.activity.ProductActivity
import com.u.marketapp.adapter.SalesHistoryRVAdapter
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.utils.BaseApplication
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_history.view.*

class SalesFragment1 : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = SalesFragment1::class.java.simpleName
        private const val REQUEST_PRODUCT = 100
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var textView: TextView
    private lateinit var adapterSales : SalesHistoryRVAdapter

    // 새로고침
    override fun onRefresh() {
        adapterSales.clear()
        requestItems()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = view.recycler_view
        textView = view.text_view_empty
        registerForContextMenu(view)
        initView()
        return view
    }

    private fun initView() {
        setRVAdapter()
        setRVLayoutManager()
        loading()
        checkItemsData(true)
        requestItems()
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        swip_refresh_layout.setOnRefreshListener(this)
    }

    private fun loading() {
        val activity = context as AppCompatActivity
        BaseApplication.instance.progressON(activity, activity.resources.getString(R.string.loading))
    }

    // 리사이클뷰 레이아웃 매니저 설정
    private fun setRVLayoutManager() {
        recyclerView.apply {
            setHasFixedSize(true)
            val linearlayout = LinearLayoutManager(context)
            layoutManager = linearlayout
        }
    }

    // 리사이클뷰 어댑터 설정
    private fun setRVAdapter() {
        if (recyclerView.adapter == null) {
            adapterSales = SalesHistoryRVAdapter()
            recyclerView.adapter = adapterSales
            adapterSales.setItemClickListener(object : SalesHistoryRVAdapter.ItemClickListener {
                override fun onClick(view: View, position: Int) {
                    moveProductActivity(adapterSales.getItem(position).id)
                }
            })
            adapterSales.setMoreClickListener(object : SalesHistoryRVAdapter.MoreClickListener {
                override fun onClick(view: View, position: Int) {
                    Log.i(TAG, "More Click : $position")
                    activity?.apply {
                        openContextMenu(view)
                    }
                }
            })
            adapterSales.setTradeClickListener(object : SalesHistoryRVAdapter.TradeClickListener {
                override fun onClick(view: View, position: Int) {
                    Log.i(TAG, "Trade Click : $position")
                }
            })
            adapterSales.setStateClickListener(object : SalesHistoryRVAdapter.StateClickListener {
                override fun onClick(view: View, position: Int) {
                    Log.i(TAG, "State Click : $position")
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
                for (item in user.salesArray) {
                    getProductItem(item)
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
                if (getActiveProductItem(documentSnapshot) && getStateProductItem(documentSnapshot) != 2) {
                    adapterSales.addItem(documentSnapshot)
                    checkItemsData(false)
                    BaseApplication.instance.progressOFF()
                    swip_refresh_layout.isRefreshing = false
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 상품 거래 상태 확인
    private fun getStateProductItem(document: DocumentSnapshot) : Int {
        val item = document.toObject(ProductEntity::class.java)!!
        return item.transactionStatus
    }

    // 상품 활성화 확인
    private fun getActiveProductItem(document: DocumentSnapshot) : Boolean {
        val item = document.toObject(ProductEntity::class.java)!!
        return item.status
    }

    // 상품 존재 여부
    private fun checkItemsData(isCheck: Boolean) {
        if (!isCheck) {
            recyclerView.visibility = View.VISIBLE
            textView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.GONE
            textView.visibility = View.VISIBLE
        }
    }

    // 상품 상세 정보 페이지 이동
    private fun moveProductActivity(id: String) {
        Log.i(TAG, "Document ID : $id")
        val intent = Intent(context, ProductActivity::class.java)
        intent.putExtra("id", id)
        startActivityForResult(intent, REQUEST_PRODUCT)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        activity?.apply {
            menuInflater.apply {
                inflate(R.menu.context_sales, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_pull -> { // 끌어올리기
                Log.i(TAG, "action_pull!!")
                true
            }
            R.id.action_update -> { // 수정
                Log.i(TAG, "action_update!!")
                true
            }
            R.id.action_hide -> { // 숨기기
                Log.i(TAG, "action_hide!!")
                true
            }
            R.id.action_delete -> { // 삭제
                Log.i(TAG, "action_delete!!")
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

}
