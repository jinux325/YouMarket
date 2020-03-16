package com.u.marketapp.entity

import java.io.Serializable
import java.util.*

data class AttentionEntity (
    val user: String?, // 유저 문서
    val isAttention: Boolean, // 관심 여부
    val regDate: Date // 작성일
) : Serializable {
    constructor() : this(null, false, Date())
}