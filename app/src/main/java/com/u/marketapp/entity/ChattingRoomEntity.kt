package com.u.marketapp.entity

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable
import java.util.*

data class ChattingRoomEntity (
    val product: DocumentReference, // 상품 문서
    val buyer: DocumentReference, // 구매자
    val seller: DocumentReference, // 판매자
    val recentMessage: String, // 최근 메시지
    val regDate: Date // 채팅방 생성일
) : Serializable