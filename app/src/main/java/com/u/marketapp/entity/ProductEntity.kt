package com.u.marketapp.entity

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class ProductEntity (
    var seller: String?, // 판매자
    var category: String?, // 카테고리
    var title: String?, // 제목
    var contents: String?, // 내용
    var price: Int, // 가격
    var suggestion: Boolean, // 가격 제시 여부
    var address: String?, // 등록 주소
    var imageArray: ArrayList<String>?, // 이미지 저장소 경로
    var chattingRoom: ArrayList<String>?, // 채팅방 목록
    var lookup: ArrayList<String>?, // 조회 유저 목록
    var attention: ArrayList<String>?, // 관심 유저 목록
    var commentSize: Int, // 댓글 목록
    var regDate: Date, // 등록일
    var modDate: Date?, // 수정일
    var status: String // 상태( unactive : 비활성화, active : 활성화, done : 거래완료)
) : Serializable {
    constructor() : this("", "", "", "", 0, false, "", ArrayList(), ArrayList(), ArrayList(), ArrayList(), 0, Date(), Date(), "unactive")
}