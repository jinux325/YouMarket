package com.u.marketapp.activity

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.fragment.AccountFragment
import com.u.marketapp.fragment.ChatFragment
import com.u.marketapp.fragment.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*


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
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        getUserInfo()
        fragmentHOME = HomeFragment()
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, fragmentHOME).commit()
    }

    private fun getUserInfo() {
        val db = FirebaseFirestore.getInstance()
        db.collection(resources.getString(R.string.db_user)).document(FirebaseAuth.getInstance().currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(UserEntity::class.java)!!
                setSharedAddress(user.address)
            }
            .addOnFailureListener { e ->
                Log.i(TAG, e.toString())
            }
    }

    private fun setSharedAddress(address: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val edit = prefs.edit()
        edit.putString("address", address)
        edit.apply()
        Log.i(TAG, "주소 변경 : ${prefs.getString("address", "세류동")}")
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
            bottom_navigation.selectedItemId =
                R.id.navigation_home
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

}
