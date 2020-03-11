package com.u.marketapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.u.marketapp.setting.SettingActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        private val TAG = MainActivity::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navigation_home -> {
                val homeFragment = HomeFragment()
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout, homeFragment).commit()
            }
            R.id.navigation_category -> {

            }
            R.id.navigation_edit -> {
                val intent = Intent(this, EditActivity::class.java)
                startActivity(intent)
                return false
            }
            R.id.navigation_chatting -> {
                val chatFragment = ChatFragment()
                supportFragmentManager.beginTransaction().replace(R.id.frame_layout, chatFragment).commit()
            }
            R.id.navigation_info -> {
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                return false
            }
        }
        return true
    }
}
