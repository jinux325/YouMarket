package com.u.marketapp.setting

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.u.marketapp.MainActivity
import com.u.marketapp.R
import com.u.marketapp.adapter.addrSubString
import com.u.marketapp.signup.AddressActivity
import com.u.marketapp.signup.ProfileActivity
import com.u.marketapp.vo.UserEntity
import kotlinx.android.synthetic.main.activity_location_setting.*
import kotlinx.android.synthetic.main.activity_profile.*
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

        //getAddressData()

       /* db.collection("Users").document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    Log.d(" userEntity address2 :  ", " [${userEntity?.address2}]")
                    if (userEntity?.address2.equals(null)) {
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
            }*/

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

        /*db.collection("Users").document(uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    val user = hashMapOf(
                        "name" to userEntity?.name,
                        "address" to userEntity?.address,
                        "address2" to FieldValue.delete(),
                        "registDate" to userEntity?.registDate,
                        "imgPath" to userEntity?.imgPath,
                        "chatting" to userEntity?.chatting,
                        "token" to userEntity?.token
                    )
                    db.collection("Users").document(uid).delete()
                    db.collection("Users").document(uid).set(user)
                    addr2.visibility = View.GONE
                    addr2_add.visibility = View.VISIBLE
                } else {
                    Log.d(TAG, "Error getting ChatRoom", task.exception)
                }
            }*/
    }

    @SuppressLint("LongLogTag")
    fun getAddressData(){
        db.collection(resources.getString(R.string.db_user)).document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    if (userEntity?.address2.equals(null)) {
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

   /* fun addressDialog(context: Context, lAddr:String, phoneNumber: String, location:String){
        val dialog = AlertDialog.Builder(context)
        dialog.setMessage("주소가 '$lAddr' 이(가) 맞습니까?").setCancelable(false)

        val db = FirebaseFirestore.getInstance()
        fun pos(){
            if (location != "" && location == "update1") {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    db.collection(context.resources.getString(R.string.db_user)).document(uid).update("address", addrSubString(lAddr))
                    val intent = Intent(context, LocationSettingActivity::class.java)
                    context.startActivity(intent)
                }
            } else if (location != "" && location == "update2") {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    db.collection(context.resources.getString(R.string.db_user)).document(uid).update("address2", addrSubString(lAddr))
                    val intent = Intent(context, LocationSettingActivity::class.java)
                    context.startActivity(intent)
                }
            } else {
                val intent = Intent(context, ProfileActivity::class.java)
                //Toast.makeText(context," $lAddr  ",Toast.LENGTH_SHORT).show()
                intent.putExtra("phoneNumber", phoneNumber)
                intent.putExtra("address",  addrSubString(lAddr))
                context.startActivity(intent)
            }

        }
        val dialog_listener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> pos()
                }
            }
        }
        dialog.setPositiveButton("확인",dialog_listener)
        dialog.setNegativeButton("취소",dialog_listener)
        dialog.show()

    }
*/
    override fun onResume() {
        super.onResume()
        getAddressData()
    }


}
