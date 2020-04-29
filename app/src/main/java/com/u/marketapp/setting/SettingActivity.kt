package com.u.marketapp.setting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.u.marketapp.R
import com.u.marketapp.activity.SplashActivity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.utils.BaseApplication
import com.u.marketapp.utils.FireStoreUtils
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val editor = pref.edit()

        setSupportActionBar(setting_toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        logout.setOnClickListener {
            Log.d("TAG", "로그아웃")

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@SettingActivity, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        val prefChatttingSwitch = pref.getString("chattingSwitch", "")
        chattingSwitch.isChecked = prefChatttingSwitch=="true"
        val prefReplySwitch = pref.getString("replySwitch", "")
        replySwitch.isChecked = prefReplySwitch=="true"
        val prefVibrationSwitch = pref.getString("vibrationSwitch", "")
        vibrationSwitch.isChecked = prefVibrationSwitch=="true"

        Log.e("pref ", " $prefChatttingSwitch  $prefReplySwitch")

       // chattingSwitch.isChecked=true
        chattingSwitch.setOnCheckedChangeListener { _, isChecked ->
           Log.e("chattingSwitch ", isChecked.toString())
            editor.putString("chattingSwitch", isChecked.toString()).apply()
        }

        replySwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.e("replySwitch ", isChecked.toString())
            editor.putString("replySwitch", isChecked.toString()).apply()
        }
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->

            editor.putString("vibrationSwitch", isChecked.toString()).apply()
        }
        withdrawal.setOnClickListener {
            deleteDialog()
        }

    }

    private fun deleteDialog(){



        val uid = FirebaseAuth.getInstance().currentUser!!.uid

        FirebaseFirestore.getInstance().collection(resources.getString(R.string.db_user)).document(uid).get().addOnSuccessListener {
                Log.e(" 탈퇴하기 ", "탈퇴하기 1")
                // val product = FireStoreUtils()
            val userEntity: UserEntity? = it.toObject<UserEntity>(UserEntity::class.java)
            if (userEntity != null) {
                Log.e(" 탈퇴하기 ", "탈퇴하기 1-1  " + userEntity.imgPath)
                FirebaseStorage.getInstance().getReferenceFromUrl(userEntity.imgPath).delete().addOnSuccessListener {


//                    FireStoreUtils().allDeleteProduct(this)

                    Log.e(" 탈퇴하기 ", "탈퇴하기 2")
                    FirebaseFirestore.getInstance().collection("User").document(uid).update("status", 0)
                        .addOnSuccessListener {
                            Log.e(" 탈퇴하기 ", "탈퇴하기 3")

                            CoroutineScope(Dispatchers.Main).launch {
                                BaseApplication.instance.progressON(this@SettingActivity, resources.getString(R.string.loading))
                                val isRemove = FireStoreUtils.instance.getIsAllRemove(this@SettingActivity)
                                Log.i("SettingActivity", "상품 일괄 제거 : $isRemove")
                                if (isRemove) {
                                    Log.i("SettingActivity", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")

                                    FirebaseAuth.getInstance().currentUser!!.delete().addOnSuccessListener {
                                        Log.e(" 탈퇴하기 ", "탈퇴하기 4")
                                        FirebaseAuth.getInstance().signOut()
                                        Log.e(" 탈퇴하기 ", "탈퇴하기 5")
                                        BaseApplication.instance.progressOFF()
                                        val intent = Intent(this@SettingActivity, SplashActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }

                                } else {
                                    Log.i("SettingActivity", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
                                }
                            }



                           /* FirebaseFirestore.getInstance().collection("User").document(uid).update("status", 1)
                                .addOnSuccessListener {

                                    Log.e(" 탈퇴하기 ", "탈퇴하기 4")
                                }*/
                            /*FirebaseAuth.getInstance().currentUser!!.delete().addOnSuccessListener {
                                Log.e(" 탈퇴하기 ", "탈퇴하기 4")
                                FirebaseAuth.getInstance().signOut()
                                Log.e(" 탈퇴하기 ", "탈퇴하기 5")
                                val intent = Intent(this, SplashActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }*/
                          /*  val intent = Intent(this, SplashActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)*/

                            /*  FirebaseAuth.getInstance().currentUser!!.delete().addOnSuccessListener {
                                  Log.e(" 탈퇴하기 ", "탈퇴하기 4")
                                  FirebaseAuth.getInstance().signOut()
                                  Log.e(" 탈퇴하기 ", "탈퇴하기 5")

                                   val intent = Intent(activity, SplashActivity::class.java)
                                  intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                                  activity.startActivity(intent)

                              }
*/

                        }

                }
            }



            }




    /*    val dialog = AlertDialog.Builder(this)
        dialog.setMessage("탈퇴는 재인증이 필요합니다. 탈퇴하시겠습니까?").setCancelable(false)
        fun pos(){
            val intent = Intent(this, SmsActivity::class.java)
            intent.putExtra("delete","delete")
            startActivity(intent)
        }
        val dialogListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE -> pos()
                }
            }
        }
        dialog.setPositiveButton("확인",dialogListener)
        dialog.setNegativeButton("취소",dialogListener)
        dialog.show()
*/
    }

}
