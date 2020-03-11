package com.u.marketapp.setting

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.u.marketapp.R
import com.u.marketapp.signup.AddressActivity
import com.u.marketapp.vo.UserEntity
import kotlinx.android.synthetic.main.activity_location_setting.*
import java.util.*

class LocationSettingActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private val TAG = "LocationSettingAcitivity"

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_setting)

        setSupportActionBar(setting_toolbar)
        supportActionBar!!.setTitle("동네 설정")
        supportActionBar!!.setDisplayShowTitleEnabled(true)


        db.collection("Users").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    if (userEntity?.address2 == "") {
                        addr2.visibility = View.GONE
                        addr2_add.visibility = View.VISIBLE
                    } else {
                        tv_addr2.text = userEntity?.address2
                        addr2.visibility = View.VISIBLE
                        addr2_add.visibility = View.GONE
                    }
                    tv_addr1.text = userEntity?.address
                } else {
                    Log.d(TAG, "Error getting LocationSetting", task.exception)
                }
            }

        addr1_delete.setOnClickListener {
            val builder =
                AlertDialog.Builder(this@LocationSettingActivity)
            builder.setMessage("동네는 최소 1개이상 선택되어있어야 합니다. 현재 설정된 동네를 변경하시겠어요?")
                .setCancelable(false)
                .setNegativeButton(
                    "취소"
                ) { dialogInterface, i -> dialogInterface.cancel() }
                .setPositiveButton(
                    "예"
                ) { dialogInterface, i ->
                    val intent =
                        Intent(this@LocationSettingActivity, AddressActivity::class.java)
                    intent.putExtra("update", "update1")
                    startActivity(intent)
                }
            val dialog = builder.create()
            dialog.show()
        }

        addr2_add.setOnClickListener {
            val intent =
                Intent(this@LocationSettingActivity, AddressActivity::class.java)
            intent.putExtra("update", "update2")
            startActivity(intent)
        }

        addr2_delete.setOnClickListener {
            val builder =
                AlertDialog.Builder(this@LocationSettingActivity)
            builder.setMessage("선택한 지역을 삭제하시겠습니까?").setCancelable(false)
                .setNegativeButton(
                    "취소"
                ) { dialogInterface, i -> dialogInterface.cancel() }
                .setPositiveButton(
                    "예"
                ) { dialogInterface, i -> delete() }
            val dialog = builder.create()
            dialog.show()
        }

    }

    // toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.toolbar_profile, menu)
        return true
    }
    @SuppressLint("LongLogTag")
    private fun delete() {
        db.collection("Users").document(uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    val user = hashMapOf(
                        "name" to userEntity?.name,
                        "address" to userEntity?.address,
                        "registDate" to userEntity?.registDate,
                        "imgPath" to userEntity?.imgPath,
                        "token" to userEntity?.token
                    )
                    db.collection("Users").document(uid).delete()
                    db.collection("Users").document(uid).set(user)
                    addr2.visibility = View.GONE
                    addr2_add.visibility = View.VISIBLE
                } else {
                    Log.d(TAG, "Error getting ChatRoom", task.exception)
                }
            }
    }
}
