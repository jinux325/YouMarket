package com.wk.umarket.entity

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class CommentEntity (
    val user: DocumentReference, // 유저 문서
    val contents: String, // 내용
    val reCommentSize: Int, // 답글 수
    val reCommentArray: ArrayList<CommentEntity>, // 답글 목록
    val regData: Date // 등록일
) : Serializable