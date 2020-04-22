package com.u.marketapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.u.marketapp.R
import com.u.marketapp.signup.SmsActivity


class SplashActivity : AppCompatActivity() {

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val hd = Handler()
        hd.postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                var count = 0
                FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user))
                    .get().addOnSuccessListener { result ->
                        for(document in result){
                            count++
                            Log.e("splash ", " $uid  ${document.id}")
                            Log.e("splash ", " $count  ${result.size()}")
                            if(uid == document.id){
                                Log.e("splash ", " 디비에 있음")
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
                    }
            }else{
                val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }, 1000) // 1초 후 이미지를 닫습니다




    }


}
