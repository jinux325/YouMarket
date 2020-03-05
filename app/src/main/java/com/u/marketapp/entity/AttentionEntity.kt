package com.wk.umarket.entity

import com.google.firebase.firestore.DocumentReference
import java.io.Serializable

data class AttentionEntity (
    val user: DocumentReference, // 유저 문서
    val isAttention: Boolean // 관심 여부
) : Serializable