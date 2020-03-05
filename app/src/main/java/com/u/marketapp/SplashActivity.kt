package com.u.marketapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.u.marketapp.signup.SmsActivity

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"
    private var number = "NoNumber"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
       /* val edit = prefs.edit()

        val pref_uid = prefs.getString("uid", "")
        val pref_log = prefs.getString("log", "")
        Log.e("Main uid", pref_uid)
        Log.e("Main Log", pref_log)
*/
        val log = prefs.getString("log", "")

        Log.e("log $TAG", log)

        val hd = Handler()
        hd.postDelayed({
            if(FirebaseAuth.getInstance().currentUser!!.phoneNumber!! != null) {
                number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
            }
            if (log == "IN") {
                Log.e(TAG, "상태 :$log")
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else if (log == null || log == "") {
                val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (FirebaseAuth.getInstance().currentUser == null) {
                    Log.e(TAG, "상태 :$log")
                } else {
                    Log.e("@@$TAG", number)
                }
                intent.putExtra("number",number)
                startActivity(intent)
            } else if (log == "OUT" && number != null) {
                Log.e(TAG, "상태 :$log")
                val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
                intent.putExtra("number",number)
                startActivity(intent)
            }
        }, 1000) // 1초 후 이미지를 닫습니다

    }
}
