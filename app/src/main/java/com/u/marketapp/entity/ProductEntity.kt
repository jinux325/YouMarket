package com.wk.umarket.entity

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class ProductEntity (
    val seller: DocumentReference, // 판매자
    val category: String, // 카테고리
    val title: String, // 제목
    val contents: String, // 내용
    val price: Int, // 가격
    val suggestion: Boolean, // 가격 제시 여부
    val address: String, // 등록 주소
    val thumnail: String, // 썸네일 경로
    val imagePath: String, // 이미지 저장소 경로
    val attentionArray: ArrayList<AttentionEntity>, // 관심 유저 목록
    val chattingRoomArray: ArrayList<ChattingRoomEntity>, // 채팅방 목록
    val commentArray: ArrayList<CommentEntity>, // 댓글 목록
    val lookUpSize: Int, // 조회 수
    val attentionSize: Int, // 관심 수
    val chattingRoomSize: Int, // 채팅방 수
    val commentSize: Int, // 댓글 수
    val regDate: Date, // 등록일
    val status: String // 상태( 0 : 비활성화, 1 : 활성화, 2 : 거래완료)
) : Serializable