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
       /* val edit = prefs.edit()

        val pref_uid = prefs.getString("uid", "")
        val pref_log = prefs.getString("log", "")
        Log.e("Main uid", pref_uid)
        Log.e("Main Log", pref_log)
*/
        val log = prefs.getString("log", "")




        Log.e("log $TAG", log)

        val hd = Handler()
       /* val intent = Intent(this@SplashActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
*/
       // FirebaseAuth.getInstance().signOut()


        //  3Nlu6jrJ0UcBC4cGjty17mOXoVj1
   /*     FirebaseFirestore.getInstance().collection("Users").document(
                "UAFqXo7Ug3VEO53REb8yP2hu4a83").get()
            .addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    Log.d("@@@@@@@@@@@"+TAG, userEntity?.name+"  "+userEntity?.address)
                    if(userEntity?.address == null){

                    }else{

                    }
                }
            })*/

        //FirebaseAuth.getInstance().signOut()

     /*   val intent = Intent(this@SplashActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
*/
  /*     val intent = Intent(this@SplashActivity, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)*/

        hd.postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                /*FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@SplashActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)*/
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
                /*val intent = Intent(this@SplashActivity, AddressActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                intent.putExtra("phoneNumber","01040166410")
                startActivity(intent)*/
            }
        }, 1000) // 1초 후 이미지를 닫습니다


            /*if(null == FirebaseAuth.getInstance().currentUser!!.phoneNumber!!) {

                Log.d("@@@@@@@@@@", FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
            }*/

            /*if(null != FirebaseAuth.getInstance().currentUser!!.phoneNumber!! ) {
                number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
                Log.d("@@@@@@@@@@", FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
            }*/
            //number = ""
            /*if (log == "IN") {
                Log.e(TAG, "상태 :$log")
                Log.d("로그 IN", number)
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else if (log == null || log == "") {
                Log.d("로그 없음", number)
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
                Log.d("로그 아웃", number)
                val intent = Intent(this@SplashActivity, SmsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                //number = FirebaseAuth.getInstance().currentUser!!.phoneNumber!!
                intent.putExtra("number",number)
                startActivity(intent)
            }
*/


    }


}
