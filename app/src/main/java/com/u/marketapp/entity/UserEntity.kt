package com.wk.umarket.entity

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class UserEntity (
    val name: String, // 닉네임
    val address: String, // 주소1
    val address2: String, // 주소2
    val profilePath: String, // 프로필 이미지 경로
    val salesHistory: ArrayList<DocumentReference>, // 판매 내역
    val purchaseHistory: ArrayList<DocumentReference>, // 구매 내역
    val attentionHistory: ArrayList<DocumentReference>, // 관심 목록
    val chattingRoomHistory: ArrayList<DocumentReference>, // 채팅방 목록
    val regDate: Date, // 등록일
    val state: String // 상태( 0 : 탈퇴, 1 : 활성화, 2 : 비활성화, 3 : 휴면)
) : Serializable