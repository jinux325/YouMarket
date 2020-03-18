package com.u.marketapp.entity

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class UserEntity (
    val name: String?, // 닉네임
    val address: String?, // 주소1
    val address2: String?, // 주소2
    val token: String?,
    val imgPath: String?, // 프로필 이미지 경로
    var salesArray: ArrayList<String>?, // 판매 내역
    var purchaseArray: ArrayList<String>?, // 구매 내역
    var attentionArray: ArrayList<String>?, // 관심 목록
    val chatting: ArrayList<String>?, // 채팅방 목록
    val regDate: Date?, // 등록일
    val state: String? // 상태( 0 : 탈퇴, 1 : 활성화, 2 : 비활성화, 3 : 휴면)
) : Serializable {
    constructor() : this ("", "", "", "", "", ArrayList(), ArrayList(), ArrayList(), ArrayList(), Date(), "active")
}