package com.u.marketapp

import android.annotation.SuppressLint
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


    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)

        val log = prefs.getString("log", "")
        Log.e("log $TAG", log)

       /* val mUser = FirebaseAuth.getInstance().currentUser
        mUser!!.getIdToken(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val idToken = task.result!!.token
                    Log.e("spl token ", idToken)
                    Log.e("spl token ", FirebaseInstanceId.getInstance().token)
                    FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.w(TAG, "getInstanceId failed", task.exception)
                                return@OnCompleteListener
                            }

                            // Get new Instance ID token
                            val token = task.result?.token
                            Log.e("spl token ", token)

                        })


                    // Send token to your backend via HTTPS
                    // ...
                } else { // Handle error -> task.getException();
                }
            }*/

        /*  val mUser = FirebaseAuth.getInstance().currentUser
          mUser!!.getIdToken(true)
              .addOnCompleteListener { task ->
                  if (task.isSuccessful) {
                      val idToken = task.result!!.token
                      Log.e("spl TOken", " $idToken")
                      // Send token to your backend via HTTPS
                      // ...
                  } else { // Handle error -> task.getException();
                  }
              }*/

/*
        FirebaseAuth.getInstance().currentUser!!.delete().addOnCompleteListener { task ->
            if(task.isSuccessful){
                //로그아웃처리
                FirebaseAuth.getInstance().signOut()
                finish()
            }
        }*/

        val hd = Handler()

        hd.postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
                Log.d("유저 확인", number)
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }else{
                Log.d("로그 없음", number)
                val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("number",number)
                startActivity(intent)

            }
        }, 1000) // 1초 후 이미지를 닫습니다




    }


}
