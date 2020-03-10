package com.u.marketapp

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.u.marketapp.adapter.PreviewRVAdapter
import com.u.marketapp.entity.ProductEntity
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.type.MediaType
import kotlinx.android.synthetic.main.activity_edit.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    private lateinit var actionbar: ActionBar
    private lateinit var adapter: PreviewRVAdapter

    companion object {
        private val TAG = EditActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        initView() // 화면 초기화
    }

    // 화면 초기화
    private fun initView() {
        setActionbar() // 액션바 설정
        setRVAdapter() // 어댑터 설정
        setRVLayoutManager() // 레이아웃 매니저 설정
        setButtonListener() // 버튼 클릭 설정
        setEditTextPrice() // 가격 콤마 처리
    }

    // 액션바
    private fun setActionbar() {
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    // 어댑터 설정
    private fun setRVAdapter() {
        adapter = PreviewRVAdapter()
        recycler_view.adapter = adapter
        adapter.itemClick = object: PreviewRVAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                showPopupRemoveImage(position) // 이미지 삭제 버튼
            }
        }
    }

    // 리사이클뷰 설정
    private fun setRVLayoutManager() {
        recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view.setHasFixedSize(true)
    }

    // 추가 버튼 설정
    private fun setButtonListener() {
        // 이미지 추가
        layout_picker.setOnClickListener {
            selectImagePicker()
        }
        // 카테고리 변경
        layout_category.setOnClickListener {
            changeCategory()
        }
    }

    // 가격 콤마 처리
    private var pointNumStr = ""
    private fun setEditTextPrice() {
        edit_text_price.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!TextUtils.isEmpty(s.toString()) && s.toString() != pointNumStr) {
                    pointNumStr = makeCommaNumber(s.toString().replace(",","").toLong())
                    edit_text_price.setText(pointNumStr)
                    edit_text_price.setSelection(pointNumStr.length)  //커서를 오른쪽 끝으로 보냄
                }
            }
        })
    }

    // 콤마 처리
    private fun makeCommaNumber(input: Long): String {
        val formatter = DecimalFormat("###,###")
        return formatter.format(input)
    }

    // 카테고리 변경
    private fun changeCategory() {
        val list = resources.getStringArray(R.array.array_category)
        MaterialAlertDialogBuilder(this)
            .setTitle("카테고리")
            .setItems(list)  { _, which ->
                Log.i(TAG, "position : ${list[which]}")
                text_view_category.text = list[which]
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 이미지 추가 확인
    private fun selectImagePicker() {
        TedImagePicker.with(this)
            .mediaType(MediaType.IMAGE) // 미디어 타입 : 이미지
            .max(10 - adapter.itemCount, "더이상 추가할 수 없습니다.")
            .startMultiImage { uriList -> addPreviewLayout(uriList) }
    }

    // 이미지 제거 확인창
    private fun showPopupRemoveImage(position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ -> removePreviewLayout(position) }
            .setNegativeButton("취소", null)
            .show()
    }

    // 이미지 추가
    private fun addPreviewLayout(uriList: List<Uri>) {
        adapter.addAllData(uriList)
        if (adapter.itemCount > 0) recycler_view.visibility = View.VISIBLE
        text_view_picker_count.text = adapter.itemCount.toString()
        //recycler_view.smoothScrollToPosition(adapter.itemCount-1)
    }

    // 이미지 제거
    private fun removePreviewLayout(position: Int) {
        if(adapter.itemCount > 0) {
            Log.i(TAG, "position: $position")
            adapter.removeData(position)
            if (adapter.itemCount <= 0) recycler_view.visibility = View.GONE
            text_view_picker_count.text = adapter.itemCount.toString()
        }
    }

    // 저장 확인 팝업창
    private fun showPopupForSave() {
        MaterialAlertDialogBuilder(this)
            .setTitle("저장하시겠습니까?")
            .setPositiveButton("확인") { _, _ -> saveProduct() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 취소 확인 팝업창
    private fun showPopupForCancel() {
        MaterialAlertDialogBuilder(this)
            .setTitle("취소하시겠습니까?")
            .setPositiveButton("확인") { _, _ -> finish() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 데이터 수집
    private fun getEditData() : ProductEntity {
        val item = ProductEntity()
        item.seller = FirebaseFirestore.getInstance().collection("User").document("QdqJ1cFReQ7EHxf4bptP") // 판매자 정보
        item.category = text_view_category.text.toString() // 카테고리
        item.title = edit_text_title.text.toString() // 제목
        item.address = "망포동"
        item.price = edit_text_price.text.toString().replace(",", "").toInt() // 가격
        item.suggestion = check_box_suggestion.isChecked // 가격 제안 여뷰
        item.contents = edit_text_contents.text.toString() // 내용
        return item
    }

    // 상품 저장
    private fun saveProduct() {
        val item = getEditData()
        val db = FirebaseFirestore.getInstance()
        db.collection("Product").add(item).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "추가된 상품 문서 : ${it.result?.id}")
                if (adapter.itemCount > 0) {
                    saveImage(it.result?.id)
                }
                addSellList(it.result)
                finish() // 종료
            }
        }
    }

    // 판매내역 등록
    private fun addSellList(document: DocumentReference?) {
        val db = FirebaseFirestore.getInstance()
//        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        document.let { item ->
            db.collection("User").document("QdqJ1cFReQ7EHxf4bptP").update("salesHistory", FieldValue.arrayUnion(item)).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.i(TAG, "판매내역 업데이트 성공! : ${item?.id}")
                }
            }
        }
    }

    // 이미지 저장
    private fun saveImage(pid: String?) {
        val storage = FirebaseStorage.getInstance()
        pid.let { item ->
            val dir = "${SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())}/${item}"
            for (uri in adapter.getAllData()) {
                val fileName = "${System.currentTimeMillis()}.${getFileExtension(uri)}"
                val ref = storage.getReference("Product").child(dir).child(fileName)
                ref.putFile(uri).continueWithTask {
                    if (!it.isSuccessful) {
                        throw it.exception!!
                    }
                    return@continueWithTask ref.downloadUrl
                }.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "이미지 추가 성공 : ${it.result.toString()}")
                        updateImage(item, it.result.toString())
                    }
                }
            }
        }
    }

    // 이미지 업데이트
    private fun updateImage(item: String?, path: String?) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Product").document(item!!).update("imageArray", FieldValue.arrayUnion(path)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "이미지 업데이트 성공 : ${it.result}")
            }
        }
    }

    // 미디어 타입
    private fun getFileExtension(uri: Uri): String? {
        val cr = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri)) ?: "jpg"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_done -> {
                showPopupForSave()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // 취소 확인 팝업창
        showPopupForCancel()
    }

}

