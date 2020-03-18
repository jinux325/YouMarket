package com.u.marketapp.entity

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class UserEntity (
    var name: String?, // 닉네임
    var address: String?, // 주소1
    var profilePath: String?, // 프로필 이미지 경로
    var salesArray: ArrayList<String>?, // 판매 내역
    var purchaseArray: ArrayList<String>?, // 구매 내역
    var attentionArray: ArrayList<String>?, // 관심 목록
    var chattingRoomArray: ArrayList<String>?, // 채팅방 목록
    var token: String?, // 토큰
    var regDate: Date, // 등록일
    var status: String // 상태( leave : 탈퇴, active : 활성화, unactive : 비활성화, dormant : 휴면)
) : Serializable {
    constructor() : this ("", "", "", ArrayList(), ArrayList(), ArrayList(), ArrayList(), "", Date(), "active")
}