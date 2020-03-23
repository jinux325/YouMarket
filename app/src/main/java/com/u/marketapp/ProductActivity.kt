package com.u.marketapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import com.u.marketapp.adapter.CommentRVAdapter
import com.u.marketapp.adapter.ViewPagerAdapter
import com.u.marketapp.chat.ChatActivity
import com.u.marketapp.databinding.ActivityProductBinding
import com.u.marketapp.entity.CommentEntity
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.activity_product.*

class ProductActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        private val TAG = ProductActivity::class.java.simpleName
        private const val REQUEST_UPDATE = 100
        private const val REQUEST_ITEM_LIMIT = 5L
    }

    private lateinit var pid: String // 상품 문서 ID
    private lateinit var uid: String // 판매자 문서 ID
    private lateinit var userName: String // 판매자 닉네임
    private lateinit var productEntity: ProductEntity // 상품 객체
    private lateinit var currentUid: String // 유저 ID
    private lateinit var binding: ActivityProductBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var commentAdapter : CommentRVAdapter
    private lateinit var document: DocumentSnapshot
    private var checkUseContext: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product)
        setActionBar()
        currentUid = FirebaseAuth.getInstance().currentUser!!.uid
        if (intent.hasExtra("id")) {
            pid = intent.getStringExtra("id")
            Log.i(TAG, pid)
            getProductData()
            button_chatting.setOnClickListener(this)
            text_view_comment_buttom.setOnClickListener(this)
            text_view_all_reply.setOnClickListener(this)
            setAttentionCheckListener()
        } else {
            Log.i(TAG, "Intent Not Signal!!")
        }
    }

    // 액션바 정의
    private fun setActionBar() {
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar!!
        // set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayShowTitleEnabled(false)
    }

    // Option Menu 소유권 확인
    private fun userVerification(menu: Menu?) {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(ProductEntity::class.java)
                if (currentUid == item?.seller) {
                    menuInflater.inflate(R.menu.toolbar_product_current, menu)
                } else {
                    menuInflater.inflate(R.menu.toolbar_product, menu)
                }
            }
        }
    }

    // 조회 확인
    private fun checkLookup(lookup: ArrayList<String>?) {
        if (lookup.isNullOrEmpty()) {
            addLookup()
        } else {
            var isCheck = false
            for (k in lookup) {
                if (k == currentUid) {
                    isCheck = true
                    break
                }
            }
            if (!isCheck) {
                addLookup()
            }
        }
    }

    // 조회 추가
    private fun addLookup() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("lookup", FieldValue.arrayUnion(currentUid)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "조회 추가 : $currentUid")
                setLookupSize()
            }
        }
    }

    // 조회 수 입력
    private fun setLookupSize() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(ProductEntity::class.java)
                item?.let { a -> text_view_lookup.text = String.format(resources.getString(R.string.format_lookup), a.lookup.size) }
            }
        }
    }

    // 관심 확인
    private fun checkAttention(attention: ArrayList<String>?) {
        if (!attention.isNullOrEmpty()) {
            var isCheck = false
            for (k in attention) {
                if (k == currentUid) {
                    isCheck = true
                    break
                }
            }
            setAttentionCheck(isCheck)
        }
    }

    // 관심 체크
    private fun setAttentionCheck(isCheck: Boolean) {
        check_box_attention.isChecked = isCheck
    }

    // 관심 체크 리스너
    private fun setAttentionCheckListener() {
        check_box_attention.setOnCheckedChangeListener{ _, isChecked ->
            if (isChecked) {
                addAttention()
            } else {
                delAttention()
            }
        }
    }

    // 관심 추가
    private fun addAttention() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("attention", FieldValue.arrayUnion(currentUid))
            .addOnSuccessListener {
                Log.i(TAG, "관심 추가 : $currentUid")
                setAttentionSize()
                addUserAttention()
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 유저 관심목록 추가
    private fun addUserAttention() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(currentUid).update("attentionArray", FieldValue.arrayUnion(pid))
            .addOnSuccessListener {
                Log.i(TAG, "유저 관심목록 추가 : $pid")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 관심 제거
    private fun delAttention() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("attention", FieldValue.arrayRemove(currentUid))
            .addOnSuccessListener {
                Log.i(TAG, "관심 제거 : $currentUid")
                setAttentionSize()
                delUserAttention()
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 유저 관심목록 제거
    private fun delUserAttention() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(currentUid).update("attentionArray", FieldValue.arrayRemove(pid))
            .addOnSuccessListener {
                Log.i(TAG, "유저 관심목록 제거 : $pid")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 조회 수 입력
    private fun setAttentionSize() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener {
            if (it.isSuccessful) {
                val item = it.result!!.toObject(ProductEntity::class.java)
                item?.let { a -> text_view_attention.text = String.format(resources.getString(R.string.format_attention), a.attention.size) }
            }
        }
    }

    // 링크 공유
    private fun share() {
        val templateId = "21927"

        val templateArgs = HashMap<String, String>()
        templateArgs["title"] = productEntity.title
        templateArgs["contents"] = String.format(resources.getString(R.string.format_price), productEntity.price)
        templateArgs["attention"] = productEntity.attention.size.toString()
        templateArgs["comment"] = productEntity.commentSize.toString()
        templateArgs["lookup"] = productEntity.lookup.size.toString()
        when (productEntity.imageArray.size) {
            1 -> {
                templateArgs["url1"] = productEntity.imageArray[0]
            }
            2 -> {
                templateArgs["url1"] = productEntity.imageArray[0]
                templateArgs["url2"] = productEntity.imageArray[1]
            }
            else -> {
                templateArgs["url1"] = productEntity.imageArray[0]
                templateArgs["url2"] = productEntity.imageArray[1]
                templateArgs["url3"] = productEntity.imageArray[2]
            }
        }

        val serverCallbackArgs = HashMap<String, String>()
        serverCallbackArgs["user_id"] = "\${current_user_id}"
        serverCallbackArgs["product_id"] = "\${shared_product_id}"

        KakaoLinkService.getInstance().sendCustom(this, templateId, templateArgs, serverCallbackArgs, object: ResponseCallback<KakaoLinkResponse>() {
            override fun onFailure(errorResult: ErrorResult?) {
                Log.i(TAG, errorResult.toString())
            }

            override fun onSuccess(result: KakaoLinkResponse?) {
                Log.i(TAG, "성공 : ${result.toString()}")
            }
        })

    }

    // 상품 수정
    private fun updateProduct() {
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("pid", pid)
        startActivityForResult(intent, REQUEST_UPDATE)
    }

    // 상품 비활성화
    private fun unActiveProduct() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("status", false).addOnCompleteListener { document ->
            if (document.isSuccessful) {
                Log.i(TAG, "상품 비활성화!")
            }
        }
    }

    // 상품 삭제
    private fun deleteProduct() {
        BaseApplication.instance.progressON(this, resources.getString(R.string.loading))
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val item = task.result!!.toObject(ProductEntity::class.java)!!
                if (item.commentSize > 0) {
                    task.result!!.reference.collection(resources.getString(R.string.db_comment)).get().addOnCompleteListener { task1 ->
                        if (task1.isSuccessful) {
                            for (document in task1.result!!.documents) {
                                val item1 = document.toObject(CommentEntity::class.java)!!
                                if (item1.replySize > 0) {
                                    document.reference.collection(resources.getString(R.string.db_reply)).get().addOnCompleteListener { task2 ->
                                        if (task2.isSuccessful) {
                                            for (document1 in task2.result!!.documents) {
                                                // 답글 삭제
                                                document1.reference.delete().addOnCompleteListener {
                                                    if (it.isSuccessful) {
                                                        Log.i(TAG, "Deleted Replay!!")
                                                    } else {
                                                        BaseApplication.instance.progressOFF()
                                                    }
                                                }
                                            }
                                        } else {
                                            BaseApplication.instance.progressOFF()
                                        }
                                    }
                                }
                                // 댓글 삭제
                                document.reference.delete().addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Log.i(TAG, "Deleted Comment!!")
                                    } else {
                                        BaseApplication.instance.progressOFF()
                                    }
                                }
                            }
                        } else {
                            BaseApplication.instance.progressOFF()
                        }
                    }
                }
                // 상품 삭제
                task.result!!.reference.delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "상품 삭제!")
                        deleteImage()
                        delSellList()
                        setResult(Activity.RESULT_OK)
                        finish() // 종료
                    } else {
                        BaseApplication.instance.progressOFF()
                    }
                }
            } else {
                BaseApplication.instance.progressOFF()
            }
        }
    }

    // 판매내역 제거
    private fun delSellList() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(currentUid).update("salesArray", FieldValue.arrayRemove(pid))
            .addOnSuccessListener {
                Log.i(TAG, "판매내역 제거 성공! : $pid")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 이미지 삭제
    private fun deleteImage() {
        val storage = FirebaseStorage.getInstance()
        if (!productEntity.imageArray.isNullOrEmpty()) {
            var count = 0
            val ref = storage.getReferenceFromUrl(productEntity.imageArray[0]).parent
            for (uri in productEntity.imageArray) {
                storage.getReferenceFromUrl(uri).delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "이미지 삭제 성공 (저장소) : $uri")
                        count++
                    }
                }
            }
            Log.i(TAG, "Folder Name : ${ref.toString()}")
            if (productEntity.imageArray.size == count) {
                ref!!.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "상품 폴더 삭제 성공 (저장소)")
                    }
                }
            }
        }
    }

    // 상품 데이터 가져오기
    private fun getProductData() {
        BaseApplication.instance.progressON(this, resources.getString(R.string.loading))
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).get().addOnCompleteListener { document ->
            if (document.isSuccessful) {
                Log.i(TAG, "Successful! ${document.result!!.id}")
                val item = document.result!!.toObject(ProductEntity::class.java)!!
                binding.setVariable(BR.product, item)
                productEntity = item
                uid = item.seller
                setPagerAdater(item.imageArray)
                checkLookup(item.lookup)
                checkAttention(item.attention)
                getUserData()
                initCommentView()
            }
            BaseApplication.instance.progressOFF()
        }
    }

    // 유저 정보 가져오기
    private fun getUserData() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(uid).get().addOnCompleteListener { document ->
            if (document.isSuccessful) {
                Log.i(TAG, "Successful! ${document.result!!.id}")
                val user = document.result!!.toObject(UserEntity::class.java)
                userName = user?.name.toString()
                binding.setVariable(BR.user, user)
            }
        }
    }

    // 이미지 페이저 정의
    private fun setPagerAdater(array: ArrayList<String>?) {
        if (array != null && array.size > 0) {
            view_pager.visibility = View.VISIBLE
            viewPagerAdapter = ViewPagerAdapter()
            viewPagerAdapter.addImageList(array)
            view_pager.adapter = viewPagerAdapter
            viewPagerAdapter.itemClick = object : ViewPagerAdapter.ItemClick {
                override fun onClick(view: View, position: Int) {
                    intentViewPager(array, position)
                }
            }
        } else {
            view_pager.visibility = View.GONE
        }
    }

    // 이미지 뷰 페이저 인텐트
    private fun intentViewPager(array: ArrayList<String>?, position: Int) {
        val intent = Intent(this, ViewActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("imageArray", array)
        startActivity(intent)
    }

    private fun initCommentView() {
        setCommentRVLayoutManager()
        setCommentRVAdapter()
        commentAdapter.clear()
        setCommentItemsData()
    }

    // 댓글 어댑터 설정
    private fun setCommentRVAdapter() {
        commentAdapter = CommentRVAdapter(this)
        binding.recyclerViewComment.adapter = commentAdapter
        commentAdapter.setMoreClickListener(object : CommentRVAdapter.MoreClickListener {
            override fun onClick(view: View, position: Int) {
                Log.i(TAG, "More Click : $position")
                if (commentAdapter.getItem(position).toObject(CommentEntity::class.java)!!.user == FirebaseAuth.getInstance().currentUser!!.uid) checkUseContext = true
                document = commentAdapter.getItem(position)
                registerForContextMenu(view)
                openContextMenu(view)
            }
        })
        commentAdapter.setReplyClickListener(object : CommentRVAdapter.ReplyClickListener {
            override fun onClick(view: View, position: Int) {
                document = commentAdapter.getItem(position)
                moveReplyIntent()
            }
        })
    }

    // 댓글 레이아웃 매니저 설정
    private fun setCommentRVLayoutManager() {
        binding.recyclerViewComment.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewComment.setHasFixedSize(true)
    }

    // 댓글 데이터 설정
    private fun setCommentItemsData() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid)
            .collection(resources.getString(R.string.db_comment)).orderBy("regDate", Query.Direction.ASCENDING)
            .limit(REQUEST_ITEM_LIMIT)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(TAG, "$pid -> Document Size : ${task.result?.documents!!.size}")
                    if (task.result?.documents!!.size > 0) {
                        for (document in task.result?.documents!!) {
                            Log.i(TAG, "Added Comment : ${document.id}")
                            commentAdapter.addItem(document)
                        }
                    }
                }
            }
    }

    // 데이터베이스 삭제
    private fun delComment() {
        document.reference.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "Delete Comment!!")
                updateCommentSize()
                val item = document.toObject(CommentEntity::class.java)
                if (item!!.replySize > 0) {
                    document.reference.collection(resources.getString(R.string.db_reply)).get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            for (document in it.result!!.documents) {
                                document.reference.delete().addOnCompleteListener { it1 ->
                                    if (it1.isSuccessful) {
                                        updateCommentSize()
                                        Log.i(TAG, "Reply Deleted!")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateCommentSize() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_product)).document(pid).update("commentSize", FieldValue.increment(-1)).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i(TAG, "Added Comment Size!")
            }
        }
    }

    // 댓글 상세 페이지 이동
    private fun moveCommentIntent() {
        val intent = Intent(this, CommentActivity::class.java)
        intent.putExtra("pid", pid)
        startActivity(intent)
    }

    // 답글 상세 페이지 이동
    private fun moveReplyIntent() {
        val intent = Intent(this, ReplyActivity::class.java)
        intent.putExtra("pid", pid)
        intent.putExtra("cid", document.id)
        startActivity(intent)
    }

    // 삭제 확인 팝업창
    private fun showPopupForDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle("삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ -> deleteProduct() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 수정 확인 팝업창
    private fun showPopupForUpdate() {
        MaterialAlertDialogBuilder(this)
            .setTitle("수정하시겠습니까?")
            .setPositiveButton("확인") { _, _ -> updateProduct() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 숨김 확인 팝업창
    private fun showPopupForHide() {
        MaterialAlertDialogBuilder(this)
            .setTitle("상품을 숨기시겠습니까?")
            .setPositiveButton("확인") { _, _ -> unActiveProduct() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 공유 확인 팝업창
    private fun showPopupForShare() {
        MaterialAlertDialogBuilder(this)
            .setTitle("상품을 공유하시겠습니까?(카톡)")
            .setPositiveButton("확인") { _, _ -> share() }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_UPDATE -> {
                    getProductData()
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_chatting -> { // 거래하기 버튼
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("pid", pid)
                intent.putExtra("seller", uid)
                intent.putExtra("name", userName)
                startActivity(intent)
            }
            R.id.text_view_comment_buttom, R.id.text_view_all_reply -> {
                moveCommentIntent()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        userVerification(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> { // 공유
                showPopupForShare()
                true
            }
            R.id.action_refresh -> { // 새로고침
                getProductData()
                true
            }
            R.id.action_declaration -> { // 신고하기
                Toast.makeText(this, resources.getString(R.string.feature_to_be_added), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_do_not_see -> { // 이 사용자의 글 보지 않기
                Toast.makeText(this, resources.getString(R.string.feature_to_be_added), Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_hide -> { // 숨기기
                showPopupForHide()
                true
            }
            R.id.action_update -> { // 수정
                showPopupForUpdate()
                true
            }
            R.id.action_delete -> { // 삭제
                showPopupForDelete()
                true
            }
            android.R.id.home -> { // 뒤로가기
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (checkUseContext) {
            menuInflater.inflate(R.menu.context_current_reply, menu)
        } else {
            menuInflater.inflate(R.menu.context_reply, menu)
        }
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_declaration -> {
                Log.i(TAG, "action_declaration!!")
                true
            }
            R.id.action_delete -> {
                Log.i(TAG, "action_delete!!")
                delComment()
                true
            }
            else -> {
                false
            }
        }
    }
}
