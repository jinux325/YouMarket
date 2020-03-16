package com.u.marketapp.entity

import java.io.Serializable
import java.util.*

data class ChattingRoomEntity (
    val product: String?, // 상품 문서
    val buyer: String?, // 구매자
    val seller: String?, // 판매자
    val recentMessage: String?, // 최근 메시지
    val regDate: Date // 채팅방 생성일
) : Serializable {
    constructor() : this(null, null, null, null, Date())
}