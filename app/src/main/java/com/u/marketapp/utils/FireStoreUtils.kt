package com.u.marketapp.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.u.marketapp.R
import com.u.marketapp.entity.CommentEntity
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.vo.ChatRoomVO

@SuppressLint("Registered")
class FireStoreUtils : AppCompatActivity() {

    companion object {
        private val TAG = FireStoreUtils::class.java.simpleName
        val instance = FireStoreUtils()
    }

    private lateinit var activity: AppCompatActivity

    // 전체 상품 제거
    fun allDeleteProduct(activity: AppCompatActivity) {
        this.activity = activity

        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_user))
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserEntity::class.java)!!
                if (user.salesArray.size > 0) {
                    for (sale in user.salesArray) {
                        deleteProduct(sale)
                    }
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 상품 제거
    fun deleteProduct(activity: AppCompatActivity, pid: String) {
        this.activity = activity

        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_product)).document(pid)
            .get()
            .addOnSuccessListener { document ->
                val item = document.toObject(ProductEntity::class.java)!!
                deleteSellList(document.id) // 사용자 판매목록 제거
                if (item.commentSize > 0) { // 댓글 목록 제거
                    document.reference.collection(activity.resources.getString(R.string.db_comment))
                        .get()
                        .addOnSuccessListener { task ->
                            deleteCommentList(task.documents)
                        }.addOnFailureListener { e ->
                            Log.i(TAG, e.toString())
                        }
                }
                // 실제 상품 제거
                document.reference
                    .delete()
                    .addOnSuccessListener {
                        Log.i(TAG, "상품 삭제 성공!")
                        deleteImage(item) // 이미지 제거
                        deleteBuyerHistory(pid)
                        deleteChatRoomList(pid)
                    }.addOnFailureListener { e ->
                        Log.i(TAG, e.toString())
                    }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 상품 제거
    fun deleteProduct(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_product)).document(pid)
            .get()
            .addOnSuccessListener { document ->
                val item = document.toObject(ProductEntity::class.java)!!
                deleteSellList(document.id) // 사용자 판매목록 제거
                if (item.commentSize > 0) { // 댓글 목록 제거
                    document.reference.collection(activity.resources.getString(R.string.db_comment))
                        .get()
                        .addOnSuccessListener { task ->
                            deleteCommentList(task.documents)
                        }.addOnFailureListener { e ->
                            Log.i(TAG, e.toString())
                        }
                }
                // 실제 상품 제거
                document.reference
                    .delete()
                    .addOnSuccessListener {
                        Log.i(TAG, "상품 삭제 성공!")
                        deleteImage(item) // 이미지 제거
                        deleteBuyerHistory(pid)
                        deleteAttentionHistory(pid)
                        deleteChatRoomList(pid)
                    }.addOnFailureListener { e ->
                        Log.i(TAG, e.toString())
                    }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 구매자의 구매목록 제거
    private fun deleteBuyerHistory(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_product))
            .document(pid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val item = documentSnapshot.toObject(ProductEntity::class.java)!!
                if (item.buyer.isNotEmpty()) {
                    db.collection(activity.resources.getString(R.string.db_user))
                        .document(item.buyer)
                        .update("purchaseArray", FieldValue.arrayRemove(pid))
                        .addOnSuccessListener {
                            Log.i(TAG, "구매자 구매목록 제거")
                        }.addOnFailureListener { e ->
                            Log.i(TAG, e.toString())
                        }
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 관심목록 제거
    private fun deleteAttentionHistory(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_product))
            .document(pid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val item = documentSnapshot.toObject(ProductEntity::class.java)!!
                if (item.attention.size > 0) {
                    for (uid in item.attention) {
                        deleteAttention(uid, pid)
                    }
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 관심자의 관심목록 제거
    private fun deleteAttention(uid: String, pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_user))
            .document(uid)
            .update("attentionArray", FieldValue.arrayRemove(pid))
            .addOnSuccessListener {
                Log.i(TAG, "관심자의 관심목록 제거")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 채팅방 제거
    private fun deleteChatRoomList(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_product))
            .document(pid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val item = documentSnapshot.toObject(ProductEntity::class.java)!!
                if (item.chattingRoom.size > 0) {
                    for (room in item.chattingRoom) {
                        db.collection(activity.resources.getString(R.string.db_chatting))
                            .document(room)
                            .get()
                            .addOnSuccessListener { documentSnapshot2 ->
                                val chatRoom = documentSnapshot2.toObject(ChatRoomVO::class.java)
                                chatRoom?.apply {
                                    buyer?.let { deleteBuyerChatRoom(it, room) }
                                    seller?.let { deleteSellerChatRoom(it, room) }
                                }
                            }.addOnFailureListener { e ->
                                Log.i(TAG, e.toString())
                            }
                    }
                }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 판매자 채팅방 제거
    private fun deleteSellerChatRoom(seller: String, room: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_user))
            .document(seller)
            .update("chatting", FieldValue.arrayRemove(room))
            .addOnSuccessListener {
                Log.i(TAG, "판매자 채팅방 제거")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 구매자들 채팅방 제거
    private fun deleteBuyerChatRoom(buyer: String, room: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_user))
            .document(buyer)
            .update("chatting", FieldValue.arrayRemove(room))
            .addOnSuccessListener {
                Log.i(TAG, "구매자 채팅방 제거")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 댓글목록 제거
    private fun deleteCommentList(list: List<DocumentSnapshot>) {
        var isCheck = false
        for (document in list) {
            val item = document.toObject(CommentEntity::class.java)!!
            if (item.replySize > 0) {
                document.reference.collection(activity.resources.getString(R.string.db_reply))
                    .get()
                    .addOnSuccessListener {
                        // 답글 삭제
                        deleteReplyList(it.documents)
                    }.addOnFailureListener { e ->
                        Log.i(TAG, e.toString())
                    }
            }
            // 댓글 삭제
            document.reference
                .delete()
                .addOnSuccessListener {
                    isCheck = true
                }.addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                    isCheck = false
                }
        }
        if (isCheck) {
            Log.i(TAG, "댓글 목록 삭제 성공!")
        } else {
            Log.i(TAG, "답글 목록 삭제 실패!")
        }
    }

    // 답글목록 제거
    private fun deleteReplyList(list: List<DocumentSnapshot>) {
        var isCheck = false
        for (document in list) {
            // 답글 삭제
            document.reference.delete()
                .addOnSuccessListener {
                    isCheck = true
                }.addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                    isCheck = false
                }
        }
        if (isCheck) {
            Log.i(TAG, "답글 목록 삭제 성공!")
        } else {
            Log.i(TAG, "답글 목록 삭제 실패!")
        }
    }

    // 판매자의 판매내역 제거
    private fun deleteSellList(pid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_user))
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .update("salesArray", FieldValue.arrayRemove(pid))
            .addOnSuccessListener {
                Log.i(TAG, "판매내역 제거 성공! : $pid")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 이미지 삭제
    private fun deleteImage(item: ProductEntity) {
        val storage = FirebaseStorage.getInstance()
        if (!item.imageArray.isNullOrEmpty()) {
            var count = 0
            val ref = storage.getReferenceFromUrl(item.imageArray[0]).parent
            for (uri in item.imageArray) {
                storage.getReferenceFromUrl(uri).delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, "이미지 삭제 성공 (저장소) : $uri")
                        count++
                    }
                }
            }
            Log.i(TAG, "Folder Name : ${ref.toString()}")
            if (item.imageArray.size == count) {
                ref!!.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "상품 폴더 삭제 성공 (저장소)")
                    }
                }
            }
        }
    }


}