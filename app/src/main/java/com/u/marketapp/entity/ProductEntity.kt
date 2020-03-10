package com.u.marketapp.entity

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class ProductEntity (
    var seller: DocumentReference?, // 판매자
    var category: String?, // 카테고리
    var title: String?, // 제목
    var contents: String?, // 내용
    var price: Int, // 가격
    var suggestion: Boolean, // 가격 제시 여부
    var address: String?, // 등록 주소
    var imageArray: ArrayList<String>?, // 이미지 저장소 경로
    var attentionArray: ArrayList<AttentionEntity>?, // 관심 유저 목록
    var chattingRoomArray: ArrayList<ChattingRoomEntity>?, // 채팅방 목록
    var commentArray: ArrayList<CommentEntity>?, // 댓글 목록
    var lookUpSize: Int, // 조회 수
    var attentionSize: Int, // 관심 수
    var chattingRoomSize: Int, // 채팅방 수
    var commentSize: Int, // 댓글 수
    var regDate: Date, // 등록일
    var status: String // 상태( unactive : 비활성화, active : 활성화, done : 거래완료)
) : Serializable {
    constructor() : this(null, null, null, null, 0, false, null, null, null, null, null, 0, 0, 0, 0, Date(), "active")
}