package com.u.marketapp.entity

import java.io.Serializable
import java.util.*

data class CommentEntity (
    val user: String?, // 유저 문서
    val contents: String?, // 내용
    val isReply: Boolean, // 답글 여부
    val replySize: Int, // 답글 수
    val regData: Date // 등록일
) : Serializable {
    constructor() : this(null, null, false, 0, Date())
}