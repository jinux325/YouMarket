package com.u.marketapp.entity

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class ProductEntity (
    var seller: String, // 판매자
    var buyer: String, // 구매자
    var category: String, // 카테고리
    var title: String, // 제목
    var contents: String, // 내용
    var price: Int, // 가격
    var suggestion: Boolean, // 가격 제시 여부
    var address: String, // 등록 주소
    val imageArray: ArrayList<String>, // 이미지 저장소 경로
    val chattingRoom: ArrayList<String>, // 채팅방 목록
    val lookup: ArrayList<String>, // 조회 유저 목록
    val attention: ArrayList<String>, // 관심 유저 목록
    val commentSize: Int, // 댓글 수
    val regDate: Date, // 등록일
    val modDate: Date, // 수정일
    val transactionStatus: Int, // 거래상태 ( 0: 거래중, 1: 예약중, 2: 거래완료 )
    val status: Boolean // 상태 ( true: 판매중, false: 숨김 )
) : Serializable {
    constructor() : this("", "", "", "", "", 0, false, "", ArrayList(), ArrayList(), ArrayList(), ArrayList(), 0, Date(), Date(), 0, false)
}