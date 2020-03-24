package com.u.marketapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.signup.SmsActivity


class SplashActivity : AppCompatActivity() {

  //  private var number = "NoNumber"

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
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
              //  number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
               // Log.d("유저 확인", number)
               /* val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)*/
                var count = 0
                FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user))
                    .get().addOnSuccessListener { result ->
                        for(document in result){
                            count++
                            Log.e("splash ", " $uid  ${document.id}")
                            Log.e("splash ", " $count  ${result.size()}")
                            if(uid == document.id){
                                Log.e("splash ", " 디비에 잇음")
                                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                break
                            }else if(count == result.size()){
                                Log.e("splash ", " 디비에 없음")
                                FirebaseAuth.getInstance().signOut()
                                val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                break
                            }
                        }
                       // FirebaseAuth.getInstance().signOut()
                    }
            }else{
                val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
              ///  Log.d("로그 없음", number)
              /*  val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)*/

            }
        }, 1000) // 1초 후 이미지를 닫습니다




    }


}
