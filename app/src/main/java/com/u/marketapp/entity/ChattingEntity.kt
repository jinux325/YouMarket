package com.u.marketapp.entity

import java.io.Serializable
import java.util.*

class ChattingEntity (
    val user: String?, // 유저 문서
    val message: String?, // 메시지
    val regDate: Date // 작성일
) : Serializable {
    constructor() : this(null, null, Date())
}