package com.u.marketapp.setting

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.signup.AddressActivity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.activity_location_setting.*

class LocationSettingActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser!!.uid
    private val TAG = "LocationSettingAcitivity"

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_setting)

        setSupportActionBar(setting_toolbar)

        addr1_delete.setOnClickListener {
            val builder =
                AlertDialog.Builder(this@LocationSettingActivity)
            builder.setMessage(R.string.location_activity_addr1_delete)
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
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    startActivity(intent)
                }
            val dialog = builder.create()
            dialog.show()
        }

        addr2_add.setOnClickListener {
            val intent = Intent(this@LocationSettingActivity, AddressActivity::class.java)
            intent.putExtra("update", "update2")
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }

        addr2_delete.setOnClickListener {
            val builder =
                AlertDialog.Builder(this@LocationSettingActivity)
            builder.setMessage(R.string.location_activity_addr2_delete).setCancelable(false)
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
        menuInflater.inflate(R.menu.toolbar_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.join -> {
                finish()
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun delete() {
        val delete = hashMapOf<String, Any>(
            "address2" to FieldValue.delete()
        )
        db.collection(resources.getString(R.string.db_user)).document(uid).update(delete)
        addr2.visibility = View.GONE
        addr2_add.visibility = View.VISIBLE
    }

    @SuppressLint("LongLogTag")
    fun getAddressData(){
        db.collection(resources.getString(R.string.db_user)).document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    if (userEntity?.address2.equals("")) {
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
    }


    override fun onResume() {
        super.onResume()
        getAddressData()
    }


}
