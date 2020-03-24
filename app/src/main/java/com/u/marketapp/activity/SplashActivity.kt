package com.u.marketapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.u.marketapp.R
import com.u.marketapp.signup.SmsActivity


class SplashActivity : AppCompatActivity() {

    private var number = "NoNumber"

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /*val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)

        val log = prefs.getString("log", "")
        Log.e("log $TAG", log)
     */

        val hd = Handler()
        hd.postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
              //  number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
               // Log.d("유저 확인", number)
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }else{
              ///  Log.d("로그 없음", number)
                val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
             //   intent.putExtra("number",number)
                startActivity(intent)

            }
        }, 1000) // 1초 후 이미지를 닫습니다




    }


}
