package com.u.marketapp.entity

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class ProductEntity (
    var seller: String?, // 판매자
    var category: String?, // 카테고리
    var title: String?, // 제목
    var contents: String?, // 내용
    var price: Int, // 가격
    var suggestion: Boolean, // 가격 제시 여부
    var address: String?, // 등록 주소
    var imageArray: ArrayList<String>?, // 이미지 저장소 경로
    var lookupArray: ArrayList<AttentionEntity>?, // 관심, 조회 유저 목록
    var chattingRoomArray: ArrayList<ChattingRoomEntity>?, // 채팅방 목록
    var commentArray: ArrayList<CommentEntity>?, // 댓글 목록
    var attentionSize: Int, // 관심 수
    var regDate: Date, // 등록일
    var status: String // 상태( unactive : 비활성화, active : 활성화, done : 거래완료)
) : Serializable {
    constructor() : this(null, null, null, null, 0, false, null, null, null, null, null, 0, Date(), "active")
}