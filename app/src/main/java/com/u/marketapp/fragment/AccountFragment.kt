package com.u.marketapp.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.activity.MainActivity
import com.u.marketapp.R
import com.u.marketapp.setting.AccountProfileActivity
import com.u.marketapp.setting.LocationSettingActivity
import com.u.marketapp.setting.SettingActivity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*


class AccountFragment : Fragment() {

    private lateinit var myData: UserEntity
    private val myUid = FirebaseAuth.getInstance().currentUser!!.uid
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
       /* Thread(){
            run(){
                lodding()
            }
        }*/
        view.account_profile.setOnClickListener {
            startActivity(Intent(activity, AccountProfileActivity::class.java).putExtra("name",myData.name).putExtra("imgPath",myData.imgPath))
        }
        view.account_setting.setOnClickListener { startActivity(Intent(activity, SettingActivity::class.java)) }
        view.location.setOnClickListener { startActivity(Intent(activity, LocationSettingActivity::class.java)) }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity?)!!.setSupportActionBar(account_toolbar)
        //loddingEnd()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_account, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {
                startActivity(Intent(activity, SettingActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        /*Thread(){
            run(){
                lodding()
            }
        }*/
        Log.d("AccountFragment ", "onResume")
        myData()

    }

    private fun myData(){
        db.collection(resources.getString(R.string.db_user)).document(myUid).get()
            .addOnSuccessListener { documentSnapshot ->
                val userEntity: UserEntity? = documentSnapshot.toObject<UserEntity>(
                    UserEntity::class.java)
                Glide.with(this).load(userEntity!!.imgPath)
                    .apply(RequestOptions.bitmapTransform(CircleCrop())).into(account_profile)
                account_name.text = userEntity.name

                val prefs = activity!!.getSharedPreferences("User", Context.MODE_PRIVATE)
                /*Log.e("선택 주소 getAddress : "," ${prefs.getString("address", "")}")
                selectAddr.text = "선택한 지역: ${prefs.getString("address", "")}"*/
                account_address.text = prefs.getString("address", "")
                myData = userEntity

            }
    }
/*
    private val dialog = AlertDialog.Builder(activity)
    private val ad = dialog.create()

    private fun lodding(){
        dialog.setMessage("잠시만 기다려주세요...").setCancelable(false)
        ad.show()
    }

    private fun loddingEnd(){
        ad.dismiss()
    }*/
}