package com.u.marketapp.entity

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class UserEntity (
    var name: String, // 닉네임
    var address: String, // 주소1
    val address2: String, // 주소2
    var token: String,
    var imgPath: String, // 프로필 이미지 경로
    val salesArray: ArrayList<String>, // 판매 내역
    val purchaseArray: ArrayList<String>, // 구매 내역
    val attentionArray: ArrayList<String>, // 관심 목록
    val chatting: ArrayList<String>, // 채팅방 목록
    val regDate: Date, // 등록일
    val state: Int // 상태( 0 : 탈퇴, 1 : 활성화, 2 : 비활성화, 3 : 휴면)
) : Serializable {
    constructor() : this ("", "", "", "", "", ArrayList(), ArrayList(), ArrayList(), ArrayList(), Date(), 1)
}