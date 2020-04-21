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
    private val tag = "LocationSettingAcitivity"
    private lateinit var myData: UserEntity

    @SuppressLint("LongLogTag", "CommitPrefEdits", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_setting)

        setSupportActionBar(setting_toolbar)

        addr1.setOnClickListener {
            Log.e("선택 주소: "," ${myData.address}")
            selectAddr1()
        }
        addr2.setOnClickListener {
            Log.e("선택 주소: "," ${myData.address2}")
            selectAddr2()
        }

        addr1_delete.setOnClickListener {
            delete("1")
        }

        addr2_delete.setOnClickListener {
            delete("2")
        }

        addr2_add.setOnClickListener {
            Log.e("addr2_add ", " 클릭 ${myData.address}  ${myData.address2}")
            val intent = Intent(this@LocationSettingActivity, AddressActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            if(myData.address == ""){
                intent.putExtra("update", "update1")
                startActivity(intent)
            }else if(myData.address2 == ""){
                intent.putExtra("update", "update2")
                startActivity(intent)
            }

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
    private fun delete(addrNum:String) {
        if(myData.address != "" && myData.address2 !=""){
            // 지움
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage(R.string.location_activity_addr2_delete).setCancelable(false)
            fun pos(){
                if(addrNum=="1"){
                    val delete = hashMapOf<String, Any>(
                        "address" to FieldValue.delete()
                    )
                    db.collection(resources.getString(R.string.db_user)).document(uid).update(delete)
                    addr1.visibility = View.GONE
                    selectAddr2()
                }else{
                    val delete = hashMapOf<String, Any>(
                        "address2" to FieldValue.delete()
                    )
                    db.collection(resources.getString(R.string.db_user)).document(uid).update(delete)
                    addr2.visibility = View.GONE
                    selectAddr1()
                }
                addr2_add.visibility = View.VISIBLE

                getAddressData()
            }
            val dialogListener = DialogInterface.OnClickListener { _, p1 ->
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> pos()
                }
            }
            dialog.setPositiveButton("확인",dialogListener)
            dialog.setNegativeButton("취소",dialogListener)
            dialog.show()
        }else{
            // 1개만 있음 못지움 >> 변경
            val dialog = AlertDialog.Builder(this)
            dialog.setMessage(R.string.location_activity_addr1_delete).setCancelable(false)
            fun pos(){
                val intent =
                    Intent(this@LocationSettingActivity, AddressActivity::class.java)
                intent.putExtra("update", "update$addrNum")
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                startActivity(intent)
            }
            val dialogListener = DialogInterface.OnClickListener { _, p1 ->
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> pos()
                }
            }
            dialog.setPositiveButton("확인",dialogListener)
            dialog.setNegativeButton("취소",dialogListener)
            dialog.show()
        }






    }

    @SuppressLint("LongLogTag", "SetTextI18n")
    fun getAddressData(){
        Log.e("getAddressData ", "")
        db.collection(resources.getString(R.string.db_user)).document(uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    myData = userEntity!!
                    if(userEntity.address == ""){
                        tv_addr2.text = userEntity.address2
                        addr2.visibility = View.VISIBLE
                        addr1.visibility = View.GONE
                        addr2_add.visibility = View.VISIBLE
                    }else{
                        addr1.visibility = View.VISIBLE
                        tv_addr1.text = userEntity.address

                        if(userEntity.address2 == ""){
                            addr2.visibility = View.GONE
                            addr2_add.visibility = View.VISIBLE
                        }else{
                            addr2.visibility = View.VISIBLE
                            tv_addr2.text = userEntity.address2
                            addr2_add.visibility = View.GONE
                        }
                    }

                    val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
                    Log.e("선택 주소 getAddress : "," ${prefs.getString("address", "")}  1 ${userEntity.address}   2 ${userEntity.address2} ")
                    selectAddr.text = "선택한 지역: ${prefs.getString("address", "")}"
                    if(prefs.getString("address", "") == userEntity.address ){
                        addr1.setBackgroundResource(R.drawable.button)
                        addr2.setBackgroundResource(R.drawable.no_select_button)
                    }else{
                        addr1.setBackgroundResource(R.drawable.no_select_button)
                        addr2.setBackgroundResource(R.drawable.button)
                    }


                } else {
                    Log.d(tag, "Error getting LocationSetting", task.exception)
                }
            }
    }

    @SuppressLint("SetTextI18n")
    fun selectAddr1(){
        val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putString("address", myData.address).apply()
        selectAddr.text = "선택한 지역: ${myData.address}"
        addr1.setBackgroundResource(R.drawable.button)
        addr2.setBackgroundResource(R.drawable.no_select_button)
    }

    @SuppressLint("SetTextI18n")
    fun selectAddr2(){
        val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
        val edit = prefs.edit()
        edit.putString("address", myData.address2).apply()
        selectAddr.text = "선택한 지역: ${myData.address2}"
        addr1.setBackgroundResource(R.drawable.no_select_button)
        addr2.setBackgroundResource(R.drawable.button)
    }

    override fun onResume() {
        super.onResume()
        getAddressData()
    }


}
