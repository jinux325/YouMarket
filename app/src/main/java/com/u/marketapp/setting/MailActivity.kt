package com.u.marketapp.setting

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.vo.EmailVO
import kotlinx.android.synthetic.main.activity_mail.*
import kotlinx.coroutines.*
import java.util.*
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MailActivity : AppCompatActivity() {

    lateinit var userId:String
    lateinit var password:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail)
        bt_send.setOnClickListener {
            when {
                et_message.text.isNullOrBlank() -> {
                    Toast.makeText(this,"내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        FirebaseFirestore.getInstance().collection("Email").document("Email").get().addOnSuccessListener { result ->
                            val emailVO: EmailVO? = result.toObject<EmailVO>(EmailVO::class.java)
                            userId = emailVO!!.ID
                            password = emailVO.PW
                            GlobalScope.launch {
                                sendEmail(et_message.text.toString())
                            }
                        }
                    }
                }
            }
        }

    }

    private fun sendEmail(content:String)
    {
        // 보내는 메일 주소와 비밀번호
     /*   val username = resources.getString(R.string.mail_id)
        val password = resources.getString(R.string.mail_pw)*/

        et_message.text = null

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        // 비밀번호 인증으로 세션 생성
        val session = Session.getInstance(props,
            object: javax.mail.Authenticator() {
                override  fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(userId, password)
                }
            })

        // 메시지 객체 만들기
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(userId))
        // 수신자 설정, 여러명으로도 가능
        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(userId))
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
        val name = prefs.getString("name", "")
        message.subject = "$name ($uid)"
        message.setText(content)

        // 전송
        Transport.send(message)


    }
}
