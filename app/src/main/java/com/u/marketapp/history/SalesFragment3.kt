package com.u.marketapp.history

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.activity.EditActivity
import com.u.marketapp.activity.ProductActivity
import com.u.marketapp.adapter.HideHistoryRVAdapter
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.utils.FireStoreUtils
import kotlinx.android.synthetic.main.fragment_history.view.*
import java.util.*

class SalesFragment3 : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = SalesFragment3::class.java.simpleName
        private const val REQUEST_PRODUCT = 100
        private const val REQUEST_UPDATE = 200
    }

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var textView: TextView
    private lateinit var adapterSales: HideHistoryRVAdapter
    private var selectPosition: Int = -1

    // 새로고침
    override fun onRefresh() {
        adapterSales.clear()
        requestItems()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        swipeRefreshLayout = view.swip_refresh_layout
        swipeRefreshLayout.setOnRefreshListener(this)
        recyclerView = view.recycler_view
        textView = view.text_view_empty
        registerForContextMenu(view)
        initView()
        return view
    }

    private fun initView() {
        setRVAdapter()
        setRVLayoutManager()
        checkItemsData(true)
        requestItems()
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
            adapterSales = HideHistoryRVAdapter()
            recyclerView.adapter = adapterSales
            adapterSales.setItemClickListener(object : HideHistoryRVAdapter.ItemClickListener {
                override fun onClick(view: View, position: Int) {
                    selectPosition = position
                    Log.i(TAG, "Item Click : $selectPosition")
                    moveProductActivity(adapterSales.getItem(position).id)
                }
            })
            adapterSales.setMoreClickListener(object : HideHistoryRVAdapter.MoreClickListener {
                override fun onClick(view: View, position: Int) {
                    selectPosition = position
                    Log.i(TAG, "More Click : $selectPosition")
                    activity?.apply {
                        openContextMenu(view)
                    }
                }
            })
            adapterSales.setVisibleClickListener(object : HideHistoryRVAdapter.VisibleClickListener {
                override fun onClick(view: View, position: Int) {
                    selectPosition = position
                    Log.i(TAG, "Visible Click : $selectPosition")
                    showPopupForVisible()
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
                val array = user.salesArray
                if (array.size > 0) {
                    for (item in user.salesArray) {
                        getProductItem(item)
                    }
                    swipeRefreshLayout.isRefreshing = false
                } else {
                    checkItemsData(true)
                    swipeRefreshLayout.isRefreshing = false
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
                    if (!getActiveProductItem(documentSnapshot)) {
                        adapterSales.addItem(documentSnapshot)
                        checkItemsData(false)
                    }
                } else {
                    Log.i(TAG, "No such DocumentSnapshot!")
                }
            }.addOnFailureListener { e ->
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

    private fun moveEditActivity(id: String) {
        Log.i(TAG, "Document ID : $id")
        val intent = Intent(context, EditActivity::class.java)
        intent.putExtra("pid", id)
        startActivityForResult(intent, REQUEST_UPDATE)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        activity?.apply {
            menuInflater.apply {
                inflate(R.menu.context_sales_complete, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_review_push -> { // 거래 후기 보내기
                //TODO 거래 후기 보내기
                Log.i(TAG, "action_change_sales : $selectPosition")
                Toast.makeText(context, resources.getString(R.string.feature_to_be_added), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_pull -> { // 끌어올리기
                Log.i(TAG, "action_pull : $selectPosition")
                showPopupForPull()
                true
            }
            R.id.action_update -> { // 수정
                Log.i(TAG, "action_hide : $selectPosition")
                moveEditActivity(adapterSales.getItem(selectPosition).id)
                true
            }
            R.id.action_delete -> { // 삭제
                Log.i(TAG, "action_delete : $selectPosition")
                showPopupForDelete()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    // 상품 끌어올리기
    private fun pullUpItem() {
        if (selectPosition != -1) {
            val pid = adapterSales.getItem(selectPosition).id
            val db = FirebaseFirestore.getInstance()
            db.collection(resources.getString(R.string.db_product))
                .document(pid)
                .update("modDate", Date())
                .addOnSuccessListener {
                    adapterSales.removeItem(selectPosition)
                    getItem(selectPosition, pid)
                }
                .addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                }
        } else {
            Log.i(TAG, "선택이 잘못되었습니다.....")
        }
    }

    // 상품 숨기기
    private fun visibleItem() {
        if (selectPosition != -1) {
            val pid = adapterSales.getItem(selectPosition).id
            val db = FirebaseFirestore.getInstance()
            db.collection(resources.getString(R.string.db_product))
                .document(pid)
                .update("status", true)
                .addOnSuccessListener {
                    adapterSales.removeItem(selectPosition)
                }
                .addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                }
        } else {
            Log.i(TAG, "선택이 잘못되었습니다.....")
        }
    }

    // 상품 재정의
    private fun refreshItem() {
        if (selectPosition != -1) {
            Log.i(TAG, "일시정지에서 돌아옴 : 상품 재정의")
            if (adapterSales.itemCount > selectPosition) {
                val pid = adapterSales.getItem(selectPosition).id
                adapterSales.removeItem(selectPosition)
                checkGetItem(selectPosition, pid)
            } else {
                Log.i(TAG, "해당되는 상품이 없음!")
            }
        } else {
            Log.i(TAG, "일시정지에서 돌아옴 : 재정의 실패!!")
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
                if (user.salesArray.contains(pid)) {
                    getItem(position, pid)
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
                    val item = documentSnapshot.toObject(ProductEntity::class.java)!!
                    if (!item.status) {
                        adapterSales.addItem(position, documentSnapshot)
                    }
                } else {
                    Log.i(TAG, "No such DocumentSnapshot!")
                }
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 삭제 확인 팝업창
    private fun showPopupForDelete() {
        MaterialAlertDialogBuilder(context)
            .setTitle("거래중인 게시글이 삭제되면 거래 상대방이 당황할 수 있어요. 게시글을 정말 삭제하시겠어요?")
            .setPositiveButton("확인") { _, _ -> FireStoreUtils.instance.deleteProduct((context as AppCompatActivity), adapterSales.getItem(selectPosition).id) }
            .setNegativeButton("취소", null)
            .show()
    }

    // 보여주기 확인 팝업창
    private fun showPopupForVisible() {
        MaterialAlertDialogBuilder(context)
            .setTitle("게시물이 다시 목록에서 노출됩니다.")
            .setPositiveButton("숨기기 해제") { _, _ -> visibleItem() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 끌어올리기 확인 팝업창
    private fun showPopupForPull() {
        val item = adapterSales.getItem(selectPosition).toObject(ProductEntity::class.java)
        val day: Long = 24 * 60 * 60 * 1000
        val reg = item?.regDate?.time ?: 0
        val mod = item?.modDate?.time ?: 0
        if (item?.modDate != item?.regDate && (mod - reg > day) ) {
            val str = "상품 끌어올리기를 하시겠습니까?"
            MaterialAlertDialogBuilder(context)
                .setTitle(str)
                .setPositiveButton("끌어올리기") { _, _ -> pullUpItem() }
                .setNegativeButton("취소", null)
                .show()
        } else {
            val str = "${remainingTime(mod - reg)} 후에 사용가능합니다."
            MaterialAlertDialogBuilder(context)
                .setTitle(str)
                .setPositiveButton("확인", null)
                .setNegativeButton("취소", null)
                .show()
        }
    }

    private fun remainingTime(date: Long) : String {
        var result = ""
        val time = System.currentTimeMillis() - date
        val second = 1000L
        val minute = second * 60
        val hour = minute * 60
        val day = hour * 24

        when {
            time < minute -> {
                result = "${time/second}분"
            }
            time < hour -> {
                result = "${time/minute}시간"
            }
            time < day -> {
                result = "${time/hour}일"
            }
        }

        return result
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "Selected Item : $selectPosition")
        refreshItem()
    }

}
