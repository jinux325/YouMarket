package com.u.marketapp.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.u.marketapp.R
import kotlinx.android.synthetic.main.activity_mail.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class MailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mail)
        bt_send.setOnClickListener {

            GlobalScope.launch {
                sendEmail()
            }
            //Send Mail
        }

    }

    fun sendEmail()
    {
        // 보내는 메일 주소와 비밀번호
        val username = "dnwlstlr9203@gmail.com";
        val password = "flare@920";

        val props = Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // 비밀번호 인증으로 세션 생성
        val session = Session.getInstance(props,
            object: javax.mail.Authenticator() {
                override  fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password);
                }
            })

        // 메시지 객체 만들기
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(username))
        // 수신자 설정, 여러명으로도 가능
        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(username))
        message.subject = et_title.text.toString()
        message.setText(et_message.text.toString())


        // 전송
        Transport.send(message)
    }
}
