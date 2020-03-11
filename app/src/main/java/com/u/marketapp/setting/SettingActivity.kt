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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setSupportActionBar(setting_toolbar)
        supportActionBar!!.setTitle("설정")
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        logout.setOnClickListener {
            Log.d("TAG", "로그아웃")
            val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
            val edit = prefs.edit()
            edit.putString("log", "OUT")
            edit.apply()
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@SettingActivity, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

        }

    }
}
