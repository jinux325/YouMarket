package com.u.marketapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kakao.util.helper.Utility.getPackageInfo
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var fragmentHOME: Fragment
    private lateinit var fragmentCATEGORY: Fragment
    private lateinit var fragmentCHATTING: Fragment
    private lateinit var fragmentINFO: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sign = getKeyHash(this)
        Log.i(TAG, "키 해시 : $sign")

        bottom_navigation.setOnNavigationItemSelectedListener(this)
        fragmentHOME = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, fragmentHOME).commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navigation_home -> {
                changeFragment(1)
            }
            R.id.navigation_category -> {
//                changeFragment(2)
            }
            R.id.navigation_edit -> {
                val intent = Intent(this, EditActivity::class.java)
                startActivity(intent)
                return false
            }
            R.id.navigation_chatting -> {
                changeFragment(4)
            }
            R.id.navigation_info -> {
                changeFragment(5)
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (fragmentHOME.isVisible) {
            super.onBackPressed()
        } else {
            bottom_navigation.selectedItemId = R.id.navigation_home
        }
    }

    // Fragment 변경
    private fun changeFragment(key: Int) {
        when (key) {
            1 -> {
                if (::fragmentHOME.isInitialized) supportFragmentManager.beginTransaction().show(fragmentHOME).commit()
                if (::fragmentCATEGORY.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentCATEGORY).commit()
                if (::fragmentCHATTING.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentCHATTING).commit()
                if (::fragmentINFO.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentINFO).commit()
            }
            2 -> {
                if (!(::fragmentCATEGORY.isInitialized)) {
//                    fragmentCATEGORY =
                    supportFragmentManager.beginTransaction().add(R.id.frame_layout, fragmentCATEGORY).commit()
                }
                if (::fragmentHOME.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentHOME).commit()
                if (::fragmentCATEGORY.isInitialized) supportFragmentManager.beginTransaction().show(fragmentCATEGORY).commit()
                if (::fragmentCHATTING.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentCHATTING).commit()
                if (::fragmentINFO.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentINFO).commit()
            }
            4 -> {
                if (!(::fragmentCHATTING.isInitialized)) {
                    fragmentCHATTING = ChatFragment()
                    supportFragmentManager.beginTransaction().add(R.id.frame_layout, fragmentCHATTING).commit()
                }
                if (::fragmentHOME.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentHOME).commit()
                if (::fragmentCATEGORY.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentCATEGORY).commit()
                if (::fragmentCHATTING.isInitialized) supportFragmentManager.beginTransaction().show(fragmentCHATTING).commit()
                if (::fragmentINFO.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentINFO).commit()
            }
            5 -> {
                if (!(::fragmentINFO.isInitialized)) {
                    fragmentINFO = AccountFragment()
                    supportFragmentManager.beginTransaction().add(R.id.frame_layout, fragmentINFO).commit()
                }
                if (::fragmentHOME.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentHOME).commit()
                if (::fragmentCATEGORY.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentCATEGORY).commit()
                if (::fragmentCHATTING.isInitialized) supportFragmentManager.beginTransaction().hide(fragmentCHATTING).commit()
                if (::fragmentINFO.isInitialized) supportFragmentManager.beginTransaction().show(fragmentINFO).commit()
            }
        }
    }

    // Key Hash 찾기
    private fun getKeyHash(context: Context?): String? {
        val packageInfo: PackageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES)
            ?: return null
        for (signature in packageInfo.signatures) {
            try {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP)
            } catch (e: NoSuchAlgorithmException) {
                Log.i(TAG, "Unable to get MessageDigest. signature=$signature", e)
            }
        }
        return null
    }

}
