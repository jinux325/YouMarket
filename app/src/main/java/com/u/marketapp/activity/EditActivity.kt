package com.u.marketapp.activity

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.u.marketapp.R
import com.u.marketapp.adapter.PreviewRVAdapter
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.utils.BaseApplication
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.type.MediaType
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditActivity : AppCompatActivity() {

    companion object {
        private val TAG = EditActivity::class.java.simpleName
    }

    private lateinit var actionbar: ActionBar
    private lateinit var adapter: PreviewRVAdapter
    private lateinit var pid: String
    private lateinit var userData: UserEntity
    private val currentArray: ArrayList<Uri> = ArrayList()
    private val delImageArray: ArrayList<Uri> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        initView() // 화면 초기화
    }

    // 화면 초기화
    private fun initView() {
        setLoadUserData()
        setActionbar() // 액션바 설정
        setRVAdapter() // 어댑터 설정
        setRVLayoutManager() // 레이아웃 매니저 설정
        setButtonListener() // 버튼 클릭 설정
        setEditTextPrice() // 가격 콤마 처리
        if (intent.hasExtra("pid")) {
            pid = intent.getStringExtra("pid")
            loadBeforeData()
        }
    }

    // 액션바
    private fun setActionbar() {
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        actionbar.title = resources.getString(R.string.edit_app_title)
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayShowTitleEnabled(false)
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
    private var pointNumStr: String = ""
    private fun setEditTextPrice() {
        edit_text_price.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var str = s.toString()
                Log.i(TAG, "필터 전 : $str")
                str = str.replace("[^0-9,]".toRegex(), "")
                Log.i(TAG, "필터 후 : $str")
                if(str.isNotEmpty() && str != pointNumStr) {
                    pointNumStr = makeCommaNumber(str.replace(",", "").toLong())
                    edit_text_price.setText(pointNumStr)
                    edit_text_price.setSelection(pointNumStr.length)  //커서를 오른쪽 끝으로 보냄
                }
            }
        })
        edit_text_price.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                var str = edit_text_price.text.toString()
                Log.i(TAG, "포커스 아웃 - 필터 전 : $str")
                str = str.replace("[^0-9,]".toRegex(), "")
                Log.i(TAG, "포커스 아웃 - 필터 후 : $str")
                edit_text_price.setText(str)
            }
        }
    }

    // 콤마 처리
    private fun makeCommaNumber(input: Long): String {
        val formatter = DecimalFormat("###,###")
        return formatter.format(input)
    }

    // 이전 데이터 로드
    private fun loadBeforeData() {
        val db = FirebaseFirestore.getInstance()
        pid.let {
            db.collection(resources.getString(R.string.db_product)).document(it).get().addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    val item = snapshot.result?.toObject(ProductEntity::class.java)
                    item?.let { it1 -> loadData(it1) }
                }
            }
        }
    }

    // 데이터 로드
    private fun loadData(item: ProductEntity) {
        text_view_category.text = item.category // 카테고리
        edit_text_title.text = item.title.toEditable() // 제목
        edit_text_contents.text = item.contents.toEditable() // 내용
        edit_text_price.text = item.price.toEditable() // 가격
        check_box_suggestion.isChecked = item.suggestion // 가격 제안 여부
        if (!item.imageArray.isNullOrEmpty()) {
            for (path in item.imageArray) {
                addBeforePreviewLayout(Uri.parse(path))
            }
        }
    }

    // 유저 데이터 로드
    private fun setLoadUserData() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(FirebaseAuth.getInstance().currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                userData = documentSnapshot.toObject(UserEntity::class.java)!!
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
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

    // 여러개의 이미지 추가
    private fun addPreviewLayout(uriList: List<Uri>) {
        currentArray.addAll(uriList)
        adapter.addAllData(uriList)
        if (adapter.itemCount > 0) recycler_view.visibility = View.VISIBLE
        showCurrentPreviewCount()
    }

    // 이전 이미지 추가
    private fun addBeforePreviewLayout(uri: Uri) {
        adapter.addData(uri)
        if (adapter.itemCount > 0) recycler_view.visibility = View.VISIBLE
        showCurrentPreviewCount()
    }

    // 이미지 제거
    private fun removePreviewLayout(position: Int) {
        if(adapter.itemCount > 0) {
            Log.i(TAG, "position: $position")
            if (::pid.isInitialized) {
                delImageArray.add(adapter.getData(position))
            }
            for (uri in currentArray) {
                if (adapter.getData(position) == uri) {
                    currentArray.remove(uri)
                    break
                }
            }
            adapter.removeData(position)
            if (adapter.itemCount <= 0) recycler_view.visibility = View.GONE
            showCurrentPreviewCount()
        }
    }

    // 현재 선택된 이미지 수 표시
    private fun showCurrentPreviewCount() {
        val str = String.format(resources.getString(R.string.format_picker), adapter.itemCount)
        Log.i(TAG, "Added Image -> Count : $str")
        text_view_picker_count.text = str
    }

    // 저장 확인 팝업창
    private fun showPopupForSave() {
        MaterialAlertDialogBuilder(this)
            .setTitle("저장하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                BaseApplication.instance.progressON(this, resources.getString(
                    R.string.loading
                ))
                if (::pid.isInitialized) {
                    updateProduct(pid, getNewEditData())
                } else {
                    saveProduct(getEditData())
                }
            }
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
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val item = ProductEntity()
        item.seller = FirebaseAuth.getInstance().currentUser!!.uid // 판매자 정보
        item.category = text_view_category.text.toString() // 카테고리
        item.title = edit_text_title.text.toString() // 제목
        item.address = pref.getString(resources.getString(R.string.edit_address), userData.address)?:userData.address
        item.suggestion = check_box_suggestion.isChecked // 가격 제안 여부
        item.contents = edit_text_contents.text.toString() // 내용
        if (edit_text_price.text.toString().isNotEmpty()) {
            var str = edit_text_price.text.toString()
            str = str.replace("[^0-9]".toRegex(), "")
            item.price = str.toInt() // 가격
//            item.price = edit_text_price.text.toString().replace(",", "").toInt() // 가격
        }
        return item
    }

    // 데이터 수집 - 수정
    private fun getNewEditData() : Map<String, Any> {
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        val map : HashMap<String, Any> = hashMapOf()
        map["category"] = text_view_category.text // 카테고리
        map["title"] = edit_text_title.text.toString() // 제목
        map["address"] = pref.getString(resources.getString(R.string.edit_address), userData.address)?:userData.address
        map["contents"] = edit_text_contents.text.toString() // 내용
        map["suggestion"] = check_box_suggestion.isChecked // 가격 제안 여부
        map["modDate"] = Date() // 수정일
        map["status"] = false // 비활성화
        if (edit_text_price.text.toString().isNotEmpty()) {
            var str = edit_text_price.text.toString()
            str = str.replace("[^0-9]".toRegex(), "")
            map["price"] = str.toInt() // 가격
//            map["price"] = edit_text_price.text.toString().replace(",", "").toInt() // 가격
        }
        return map
    }

    // 상품 저장
    private fun saveProduct(item: ProductEntity) {
        Log.i(TAG, "상품 저장!!")
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).add(item).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "추가된 상품 문서 : ${it.result!!.id}")
                if (adapter.itemCount > 0) {
                    saveImage1(it.result!!.id)

                } else {
                    updateActiveProduct(it.result!!.id)
                }
                addSellList(it.result!!.id)
                BaseApplication.instance.progressOFF()
                setResult(Activity.RESULT_OK)
                finish() // 종료
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    // 상품 수정
    private fun updateProduct(pid: String, data: Map<String, Any>) {
        Log.i(TAG, "상품 수정!!")
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update(data).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "상품 수정 완료!")
                if (currentArray.size > 0 || delImageArray.size > 0) {
                    Log.i(TAG, "상품 이미지 수정!!")
                    deleteImage(pid)
                    saveImage1(pid)
                    BaseApplication.instance.progressOFF()
                    setResult(Activity.RESULT_OK)
                    finish() // 종료
                } else {
                    Log.i(TAG, "상품 이미지 없어서 그냥 활성화!!")
                    updateActiveProduct(pid)
                }
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    // 판매내역 등록
    private fun addSellList(documentId: String?) {
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        uid.let {
            documentId.let { item ->
                db.collection(resources.getString(R.string.db_user)).document(it).update("salesArray", FieldValue.arrayUnion(item)).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "판매내역 업데이트 성공! : $item")
                    }
                }
            }
        }
    }

    // 이미지 저장
    private fun saveImage(pid: String) {
        Log.i(TAG, "새로운 이미지 추가!!")
        if (currentArray.size > 0) {
            val storage = FirebaseStorage.getInstance()
            val dir = "${SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())}/${FirebaseAuth.getInstance().currentUser!!.uid}/${pid}"
            var count = 0
            for (uri in currentArray) {
                val fileName = "${System.currentTimeMillis()}.${getFileExtension(uri)}"
                val ref = storage.getReference(resources.getString(R.string.db_product)).child(dir).child(fileName)
                ref.putFile(uri).continueWithTask {
                    if (!it.isSuccessful) {
                        throw it.exception!!
                    }
                    return@continueWithTask ref.downloadUrl
                }.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "이미지 추가 성공 : ${it.result.toString()}")
                        updateImage(pid, it.result.toString())
                        if ((++count) >= currentArray.size) {
                            updateActiveProduct(pid)
                        }
                    }
                }
            }
        } else {
            Log.i(TAG, "새로운 이미지가 없음!!!")
            updateActiveProduct(pid)
        }
    }

    // 이미지 저장
    private fun saveImage1(pid: String) {
        Log.i(TAG, "새로운 이미지 추가!!")
        CoroutineScope(Dispatchers.Main).launch {
            if (currentArray.size > 0) {
                var count = 0
                for (uri in currentArray) {
                    val downloadUrl = saveStorageImage(pid, uri)
                    if (downloadUrl.isNullOrEmpty()) {
                        Log.i(TAG, "이미지 저장 실패!")
                    } else {
                        Log.i(TAG, "이미지 추가 성공 : $downloadUrl")
                        updateImage(pid, downloadUrl)
                        if ((++count) >= currentArray.size) {
                            updateActiveProduct(pid)
                        }
                    }
                }
            } else {
                Log.i(TAG, "새로운 이미지 없음!")
                updateActiveProduct(pid)
            }
        }
    }

    private suspend fun saveStorageImage(pid: String, uri: Uri) : String? {
        val storage = FirebaseStorage.getInstance()
        val dir = "${SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date())}/${FirebaseAuth.getInstance().currentUser!!.uid}/${pid}"
        val fileName = "${System.currentTimeMillis()}.${getFileExtension(uri)}"
        val ref = storage.getReference(resources.getString(R.string.db_product)).child(dir).child(fileName)
        return withContext(Dispatchers.IO) {
            ref.putFile(uri).await().storage.downloadUrl.await().toString()
        }
    }

    // 이미지 제거
    private fun deleteImage(pid: String) {
        Log.i(TAG, "기존 이미지 제거!!")
        if (delImageArray.size > 0) {
            val db = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()
            for (uri in delImageArray) {
                db.collection(resources.getString(R.string.db_product)).document(pid).update("imageArray", FieldValue.arrayRemove(uri.toString())).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "이미지 삭제 성공 (데이터) : $uri")
                    }
                }
                storage.getReferenceFromUrl(uri.toString()).delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "이미지 삭제 성공 (저장소) : $uri")
                    }
                }
            }
        } else {
            Log.i(TAG, "제거할 기존 이미지가 없음!!!")
        }
    }

    // 이미지 업데이트
    private fun updateImage(item: String?, path: String?) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(item!!).update("imageArray", FieldValue.arrayUnion(path)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "이미지 업데이트 성공 : $path")
            }
        }
    }

    // 상품 활성화
    private fun updateActiveProduct(item: String?) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(item!!).update("status", true).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "상품 업데이트 성공!")
                BaseApplication.instance.progressOFF()
                setResult(Activity.RESULT_OK)
                finish() // 종료
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
        menuInflater.inflate(R.menu.toolbar_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_done -> {
                if (edit_text_title.text.trim().isNotEmpty()) {
                    showPopupForSave()
                } else {
                    Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    edit_text_title.requestFocus()
                }
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

    // Editable 변환
    private fun Int.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this.toString())
    private fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

}

