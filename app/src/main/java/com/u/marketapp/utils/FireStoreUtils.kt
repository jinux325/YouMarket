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
import com.u.marketapp.vo.ChattingVO

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
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(UserEntity::class.java)!!
                    if (user.salesArray.size > 0) {
                        for (sale in user.salesArray) {
                            deleteProduct(sale)
                        }
                    }
                } else {
                    Log.i(TAG, "유저 정보가 없음!")
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
            .addOnSuccessListener { documentSnapshot ->
                val item = documentSnapshot.toObject(ProductEntity::class.java)!!
                deleteImage(item.imageArray) // 이미지 제거
                deleteCommentList(documentSnapshot) // 댓글 제거
                deleteAttentionHistory(documentSnapshot) // 관심자 관심목록 제거
                deleteSellList(documentSnapshot) // 사용자 판매목록 제거
                deleteBuyerHistory(documentSnapshot) // 구매자 구매목록 제거
                deleteChatRoomList(documentSnapshot) // 채팅방 제거

                // 실제 상품 제거
                documentSnapshot.reference
                    .delete()
                    .addOnSuccessListener {
                        Log.i(TAG, "상품 삭제 성공!")
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
            .addOnSuccessListener { documentSnapshot ->
                val item = documentSnapshot.toObject(ProductEntity::class.java)!!
                deleteImage(item.imageArray) // 이미지 제거
                deleteCommentList(documentSnapshot) // 댓글 제거
                deleteAttentionHistory(documentSnapshot) // 관심자 관심목록 제거
                deleteSellList(documentSnapshot) // 사용자 판매목록 제거
                deleteBuyerHistory(documentSnapshot) // 구매자 구매목록 제거
                deleteChatRoomList(documentSnapshot) // 채팅방 제거

                // 실제 상품 제거
                documentSnapshot.reference
                    .delete()
                    .addOnSuccessListener {
                        Log.i(TAG, "상품 삭제 성공!")
                    }.addOnFailureListener { e ->
                        Log.i(TAG, e.toString())
                    }
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 댓글 목록 제거
    private fun deleteCommentList(documentSnapshot: DocumentSnapshot) {
        val item = documentSnapshot.toObject(ProductEntity::class.java)!!
        if (item.commentSize > 0) { // 댓글 목록 제거
            documentSnapshot.reference.collection(activity.resources.getString(R.string.db_comment))
                .get()
                .addOnSuccessListener { task ->
                    deleteCommentList(task.documents)
                }.addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                }
        }
    }

    // 구매자의 구매목록 제거
    private fun deleteBuyerHistory(documentSnapshot: DocumentSnapshot) {
        val item = documentSnapshot.toObject(ProductEntity::class.java)!!
        if (item.buyer.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection(activity.resources.getString(R.string.db_user))
                .document(item.buyer)
                .update("purchaseArray", FieldValue.arrayRemove(documentSnapshot.id))
                .addOnSuccessListener {
                    Log.i(TAG, "구매자 구매목록 제거")
                }.addOnFailureListener { e ->
                    Log.i(TAG, e.toString())
                }
        }
    }

    // 관심목록 제거
    private fun deleteAttentionHistory(documentSnapshot: DocumentSnapshot) {
        val item = documentSnapshot.toObject(ProductEntity::class.java)!!
        if (item.attention.size > 0) {
            for (uid in item.attention) {
                deleteAttention(uid, documentSnapshot.id)
            }
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
    private fun deleteChatRoomList(documentSnapshot: DocumentSnapshot) {
        val db = FirebaseFirestore.getInstance()
        val item = documentSnapshot.toObject(ProductEntity::class.java)!!
        if (item.chattingRoom.size > 0) {
            for (room in item.chattingRoom) {
                db.collection(activity.resources.getString(R.string.db_chatting))
                    .document(room)
                    .get()
                    .addOnSuccessListener { documentSnapshot2 ->
                        val chatRoom = documentSnapshot2.toObject(ChatRoomVO::class.java)
                        chatRoom?.apply {
                            buyer?.let { deleteBuyerChatRoom(it, room) } // 구매 희망자의 채팅방 제거
                            seller?.let { deleteSellerChatRoom(it, room) } // 판매자의 채팅방 제거
                        }

                        documentSnapshot2.reference
                            .collection(activity.resources.getString(R.string.db_chatting_comment))
                            .get()
                            .addOnSuccessListener { documentSnapshot3 ->
                                // 채팅 내역 제거
                                for (document in documentSnapshot3.documents) {
                                    val url = document.toObject(ChattingVO::class.java)!!.imageMsg
                                    url?.let { if (it.isNotEmpty()) deleteChatImage(it) } // 채팅에 포함된 이미지 (저장소) 삭제
                                    document.reference
                                        .delete()
                                        .addOnSuccessListener {
                                            Log.i(TAG, "채팅 내역 삭제!")
                                        }.addOnFailureListener { e ->
                                            Log.i(TAG, e.toString())
                                        }
                                }

                                // 실제 채팅방 제거
                                documentSnapshot2.reference
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.i(TAG, "채팅방 삭제 성공!")
                                    }.addOnFailureListener { e ->
                                        Log.i(TAG, e.toString())
                                    }
                            }.addOnFailureListener { e ->
                                Log.i(TAG, e.toString())
                            }
                    }.addOnFailureListener { e ->
                        Log.i(TAG, e.toString())
                    }
            }
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
    private fun deleteSellList(documentSnapshot: DocumentSnapshot) {
        val db = FirebaseFirestore.getInstance()
        db.collection(activity.resources.getString(R.string.db_user))
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .update("salesArray", FieldValue.arrayRemove(documentSnapshot.id))
            .addOnSuccessListener {
                Log.i(TAG, "판매내역 제거 성공! : ${documentSnapshot.id}")
            }.addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    // 이미지 삭제
    private fun deleteImage(imageArray: List<String>) {
        val storage = FirebaseStorage.getInstance()
        if (imageArray.isNotEmpty()) {
            for (url in imageArray) {
                storage.getReferenceFromUrl(url)
                    .delete()
                    .addOnSuccessListener {
                        Log.i(TAG, "이미지 삭제 성공 (저장소) : $url")
                    }.addOnFailureListener { e ->
                        Log.i(TAG, e.toString())
                    }
            }
        }
    }

    // 이미지 삭제
    private fun deleteChatImage(url: String) {
        val storage = FirebaseStorage.getInstance()
        storage.getReferenceFromUrl(url)
            .delete()
            .addOnSuccessListener {
                Log.i(TAG, "이미지 삭제 성공 (저장소) : $url")
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

}