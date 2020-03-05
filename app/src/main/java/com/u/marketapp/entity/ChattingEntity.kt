package com.wk.umarket.entity

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable
import java.util.*

class ChattingEntity (
    val user: DocumentReference, // 유저 문서
    val message: String, // 메시지
    val regDate: Date // 작성일
) : Serializable