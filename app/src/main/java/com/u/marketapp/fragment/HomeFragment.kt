package com.u.marketapp.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.R
import com.u.marketapp.activity.FilterActivity
import com.u.marketapp.activity.ProductActivity
import com.u.marketapp.activity.SearchActivity
import com.u.marketapp.adapter.ProductRVAdapter
import com.u.marketapp.databinding.FragmentHomeBinding
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.listener.EndlessRecyclerViewScrollListener
import com.u.marketapp.setting.LocationSettingActivity
import com.u.marketapp.utils.BaseApplication
import com.u.marketapp.utils.SharedPreferencesUtils
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
        private const val REQUEST_PRODUCT = 100
        private const val REQUEST_FILTER = 200
        private const val REQUEST_ITEM_LIMIT = 20L
    }

    private lateinit var listener: OnFragmentInteractionListener
    private lateinit var actionbar: ActionBar
    private lateinit var adapter : ProductRVAdapter
    private lateinit var binding : FragmentHomeBinding
    private lateinit var scrollListener : EndlessRecyclerViewScrollListener
    private val info : ArrayList<String> = ArrayList()
    private var selectPosition = -1

    // 새로고침
    override fun onRefresh() {
        adapter.clear()
        requestItems()
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(address: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 데이터 바인딩
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        getUserAddrss()
        setRVAdapter()
        setRVLayoutManager()
        requestItems()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                moveSearchActivity()
                true
            }
            R.id.action_filter -> {
                moveFilterActivity()
                true
            }
            R.id.action_notification -> {
                Toast.makeText(requireContext(), resources.getString(R.string.feature_to_be_added), Toast.LENGTH_SHORT).show()
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
        setSpinnerListener()
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
                    selectPosition = position
                    moveProductActivity(adapter.getItem(position).id)
                }
            })
        }
    }

    // 스피너 클릭 이벤트
    private fun setSpinnerListener() {
        text_view_spinner.text = getSharedAddress()
        layout_spinner.setOnClickListener {
            animRotate(true)
            showAlertDialog(info.toTypedArray())
        }
    }

    private fun animRotate(rotate: Boolean) {
        val anim =
            if (rotate) AnimationUtils.loadAnimation(context, R.anim.clockwise_rotate)
            else AnimationUtils.loadAnimation(context, R.anim.counter_clockwise_rotate)
        image_view_spinner.startAnimation(anim)
    }

    private fun getUserAddrss() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(FirebaseAuth.getInstance().currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(UserEntity::class.java)
                info.clear()
                user?.let {
                    if (it.address.isNotEmpty()) info.add(it.address)
                    if (it.address2.isNotEmpty()) info.add(it.address2)
                }
                info.add("내 동네 설정")
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    private fun showAlertDialog(info: Array<String>) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setItems(info) { _, which ->
            val item = info[which]
            if (item != "내 동네 설정") {
                changeAddress(item)
            } else {
                moveLocationSetting()
            }
        }
        val dialog = builder.create()
        dialog.window?.let { window ->
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setGravity(Gravity.TOP)
                attributes.y = 80
                attributes.x = -40
            }
        }
        dialog.show()
        dialog.setOnDismissListener {
            animRotate(false)
        }
    }

    private fun moveLocationSetting() {
        Log.i(TAG, "LocationSettingActivity로 이동!!!!")
        val intent = Intent(requireContext(), LocationSettingActivity::class.java)
        startActivity(intent)
    }

    private fun changeAddress(item: String) {
        showSnackbar("${item}으로 변경 되었습니다.")
        if (text_view_spinner.text != item) {
            setSharedAddress(item)
            text_view_spinner.text = item
            adapter.clear()
            requestItems()
        }
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(layout_root, msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun checkSharedAddress() {
        val prefs = activity!!.getSharedPreferences("User", Context.MODE_PRIVATE)
        val address = prefs.getString("address", "내 동네 설정")
        address?.let {
            Log.i(TAG, "주소 확인 : $address")
            if (address != "내 동네 설정" && address != text_view_spinner.text) {
                changeAddress(address)
            }
        }
    }

    private fun getSharedAddress() : String {
        val pref = context!!.getSharedPreferences("User", Context.MODE_PRIVATE)
        val result = pref.getString("address", "내 동네 설정")!!
        Log.i(TAG, "주소 가져오기 : $result")
        return result
    }

    private fun setSharedAddress(address: String) {
        val prefs = context!!.getSharedPreferences("User", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putString("address", address)
        edit.apply()
        if (::listener.isInitialized) listener.onFragmentInteraction(address)
        Log.i(TAG, "주소 변경 : ${prefs.getString("address", "세류동")}")
    }

    // 데이터 로드
    private fun requestItems() {
        scrollListener.resetState()

        val list = SharedPreferencesUtils.instance.getStringArrayPref(requireContext(), "category")
        val db = FirebaseFirestore.getInstance()
        var query = db.collection(resources.getString(R.string.db_product))
            .whereEqualTo("address", getSharedAddress())
            .whereEqualTo("status", true)
            .orderBy("modDate", Query.Direction.DESCENDING)
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
                BaseApplication.instance.progressOFF()
                binding.swipRefreshLayout.isRefreshing = false
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 데이터 로드
    private fun requestPagingItems(next: Int) {
        val list = SharedPreferencesUtils.instance.getStringArrayPref(requireContext(), "category")
        val db = FirebaseFirestore.getInstance()
        var query = db.collection(resources.getString(R.string.db_product))
            .whereEqualTo("address", getSharedAddress())
            .whereEqualTo("status", true)
            .orderBy("modDate", Query.Direction.DESCENDING)
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
        startActivityForResult(intent,
            REQUEST_PRODUCT
        )
    }

    // 검색 페이지 이동
    private fun moveSearchActivity() {
        val intent = Intent(context, SearchActivity::class.java)
        startActivity(intent)
    }

    // 필터 페이지 이동
    private fun moveFilterActivity() {
        val intent = Intent(context, FilterActivity::class.java)
        startActivityForResult(intent, REQUEST_FILTER)
    }

    override fun onResume() {
        super.onResume()
        checkSharedAddress()
        getUserAddrss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PRODUCT -> { // 상품 페이지에서 리턴
                    Log.i(TAG, "상품 페이지에서 리턴! : $selectPosition")
                    if (selectPosition != -1) {
                        adapter.removeItem(selectPosition)
                        selectPosition = -1 // 초기화
                    }
                }
                REQUEST_FILTER -> { // 필터 페이지에서 리턴
                    val list = SharedPreferencesUtils.instance.getStringArrayPref(requireContext(), "category")
                    Log.i(TAG, list.toString())
                    BaseApplication.instance.progressON(requireActivity(), resources.getString(R.string.loading))
                    adapter.clear()
                    requestItems()
                }
            }
        }
    }

}
