package com.u.marketapp.entity

import java.io.Serializable
import java.util.*

data class CommentEntity (
    var user: String, // 유저 문서
    var contents: String, // 내용
    var reply: Boolean, // 답글 여부
    val replySize: Int, // 답글 수
    val regDate: Date // 등록일
) : Serializable {
    constructor() : this("", "", false, 0, Date())
}