package com.u.marketapp.entity

import java.io.Serializable
import java.util.*

data class CommentEntity (
    val user: String?, // 유저 문서
    val contents: String?, // 내용
    val rating: Float, // 점수
    val regData: Date // 등록일
) : Serializable {
    constructor() : this(null, null, 0f, Date())
}