package com.u.marketapp.activity

import android.content.Context
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
import com.u.marketapp.fragment.CategoryFragment
import com.u.marketapp.fragment.ChatFragment
import com.u.marketapp.fragment.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener, HomeFragment.OnFragmentInteractionListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var fragmentHOME: Fragment
    private lateinit var fragmentCATEGORY: Fragment
    private lateinit var fragmentCHATTING: Fragment
    private lateinit var fragmentINFO: Fragment
    private lateinit var address: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        address = getSharedAddress()
        Log.i(TAG, "주소 : $address")
        testLog()
        initView()
    }

    private fun testLog() {
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        Log.i(TAG, "UID : ${pref.getString("uid", "없어!")}")
        Log.i(TAG, "NAME : ${pref.getString("name", "없어!")}")
        Log.i(TAG, "IMAGE : ${pref.getString("imgPath", "없어!")}")
        Log.i(TAG, "PHONE : ${pref.getString("phoneNumber", "없어!")}")
        Log.i(TAG, "ADDRESS : ${pref.getString("address", "없어!")}")
    }

    private fun initView() {
        fragmentHOME = HomeFragment()
        fragmentCATEGORY = CategoryFragment()
        fragmentCHATTING = ChatFragment()
        fragmentINFO = AccountFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.frame_layout, fragmentHOME)
        transaction.add(R.id.frame_layout, fragmentCATEGORY)
        transaction.add(R.id.frame_layout, fragmentCHATTING)
        transaction.add(R.id.frame_layout, fragmentINFO)
        transaction.commit()
        changeFragment(1)
    }

    private fun getSharedAddress() : String {
        val pref = getSharedPreferences("User", Context.MODE_PRIVATE)
        return pref.getString("address", "내 동네 설정")!!
    }

    override fun onFragmentInteraction(address: String) {
        this.address = address
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navigation_home -> {
                changeFragment(1)
            }
            R.id.navigation_category -> {
                changeFragment(2)
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
                Log.i(TAG, "이전 주소 : $address -> 바뀐 주소 : ${getSharedAddress()}")
                if(address != getSharedAddress()) {
                    Log.i(TAG, "홍 새로 생성")
                    address = getSharedAddress()
                    fragmentHOME = HomeFragment()
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_layout, fragmentHOME).commit()
                } else {
                    Log.i(TAG, "홈 활성화")
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.show(fragmentHOME)
                    transaction.hide(fragmentCATEGORY)
                    transaction.hide(fragmentCHATTING)
                    transaction.hide(fragmentINFO)
                    transaction.commitNowAllowingStateLoss()
                }
            }
            2 -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.hide(fragmentHOME)
                transaction.show(fragmentCATEGORY)
                transaction.hide(fragmentCHATTING)
                transaction.hide(fragmentINFO)
                transaction.commitNowAllowingStateLoss()
            }
            4 -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.hide(fragmentHOME)
                transaction.hide(fragmentCATEGORY)
                transaction.show(fragmentCHATTING)
                transaction.hide(fragmentINFO)
                transaction.commitNowAllowingStateLoss()
            }
            5 -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.hide(fragmentHOME)
                transaction.hide(fragmentCATEGORY)
                transaction.hide(fragmentCHATTING)
                transaction.show(fragmentINFO)
                transaction.commitNowAllowingStateLoss()
            }
        }
    }

}
