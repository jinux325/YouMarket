package com.u.marketapp.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.u.marketapp.R
import com.u.marketapp.SplashActivity
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
/*

    private var user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
*/



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val editor = pref.edit()

        setSupportActionBar(setting_toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        logout.setOnClickListener {
            Log.d("TAG", "로그아웃")

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@SettingActivity, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        val prefChatttingSwitch = pref.getString("chattingSwitch", "")
        chattingSwitch.isChecked = prefChatttingSwitch=="true"
        val prefReplySwitch = pref.getString("replySwitch", "")
        replySwitch.isChecked = prefReplySwitch=="true"

        Log.e("pref ", " $prefChatttingSwitch  $prefReplySwitch")

       // chattingSwitch.isChecked=true
        chattingSwitch.setOnCheckedChangeListener { _, isChecked ->
           Log.e("chattingSwitch ", isChecked.toString())
            editor.putString("chattingSwitch", isChecked.toString()).apply()
        }

        replySwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.e("replySwitch ", isChecked.toString())
            editor.putString("replySwitch", isChecked.toString()).apply()
        }

        withdrawal.setOnClickListener {
           // val myUid = FirebaseAuth.getInstance().currentUser!!.uid
          /*  Log.e("dfsf", myUid)
            FirebaseAuth.getInstance().currentUser!!.delete().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    FirebaseAuth.getInstance().signOut()
                    db.collection(resources.getString(R.string.db_user)).document(myUid).delete()

                    val intent = Intent(this@SettingActivity, SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }*/

        }

    }
}
