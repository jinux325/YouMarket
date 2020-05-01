package com.u.marketapp.setting

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.u.marketapp.R
import com.u.marketapp.activity.SplashActivity
import com.u.marketapp.signup.SmsActivity
import kotlinx.android.synthetic.main.activity_setting.*


class SettingActivity : AppCompatActivity() {

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
        val prefVibrationSwitch = pref.getString("vibrationSwitch", "")
        vibrationSwitch.isChecked = prefVibrationSwitch=="true"

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
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->

            editor.putString("vibrationSwitch", isChecked.toString()).apply()
        }
        withdrawal.setOnClickListener {
            deleteDialog()
        }

    }

    private fun deleteDialog(){
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("탈퇴는 재인증이 필요합니다. 탈퇴하시겠습니까?").setCancelable(false)
        fun pos(){
            Log.e("Setting ", " pos() ")
            val intent = Intent(this, SmsActivity::class.java)
            intent.putExtra("delete","delete")
            startActivity(intent)

        }
        val dialogListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> pos()
                }
            }
        }
        dialog.setPositiveButton("확인",dialogListener)
        dialog.setNegativeButton("취소",dialogListener)
        dialog.show()

    }

}
