package com.u.marketapp.vo

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class UserEntity (
    val name: String="", // 닉네임
    val address: String="", // 주소1
    val address2: String?=null, // 주소2
    val token: String="",
    val imgPath: String="", // 프로필 이미지 경로
    val salesHistory: ArrayList<String>?=null, // 판매 내역
    val purchaseHistory: ArrayList<String>?=null, // 구매 내역
    val attentionHistory: ArrayList<String>?=null, // 관심 목록
    val chattingRoomHistory: ArrayList<String>?=null, // 채팅방 목록
    val registDate: Date?=null, // 등록일
    val state: String="" ,// 상태( 0 : 탈퇴, 1 : 활성화, 2 : 비활성화, 3 : 휴면)
    val chatting:ArrayList<String>?=null
) : Serializable