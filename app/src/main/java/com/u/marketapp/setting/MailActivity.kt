package com.u.marketapp.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import kotlinx.android.synthetic.main.activity_mail.*
import java.util.*

class MailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail)
        bt_send.setOnClickListener {
            when {
                et_message.text.isNullOrBlank() -> {
                    Toast.makeText(this,"내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                else -> {

                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val registDate = Date(System.currentTimeMillis())
                    val email = hashMapOf(
                        "uid" to uid,
                        "content" to et_message.text.toString(),
                        "registDate" to registDate
                    )
                    FirebaseFirestore.getInstance().collection("Email").document()
                        .set(email).addOnSuccessListener { et_message.text = null }

                }
            }
        }
    }

}
