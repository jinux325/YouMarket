package com.u.marketapp.signup

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.activity.MainActivity
import com.u.marketapp.R
import com.u.marketapp.activity.SplashActivity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.activity_sms.*
import java.util.concurrent.TimeUnit

class SmsActivity : AppCompatActivity() {

    private val tag = "SmsActivity"
    private val mAuth= FirebaseAuth.getInstance()
    var codeSent : String = ""
    private var phone=""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)

        buttonContinue.setOnClickListener {
            Toast.makeText(this,"잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show()
            buttonContinue.isEnabled = false
            sendVerification()
        }
        verifyButton.setOnClickListener {
            Toast.makeText(this,"잠시만 기다려 주세요.", Toast.LENGTH_SHORT).show()
            verifySignIn()
        }

    }

    private fun verifySignIn(){
        val code = verifyEditText.text.toString()
        val credential = PhoneAuthProvider.getCredential(codeSent, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid

                    val intentItems = intent
                    if(intentItems.getStringExtra("delete")!=null){
                        userDelete()
                    }

                    userExist(uid)
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this,"upload failed: " + task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun sendVerification(){
        phone = editTextMobile.text.toString()

        if(phone.isEmpty()){
            editTextMobile.error = "번호를 입력해주세요"
        }
        var phoneNumber=""
        if( phone.length != 11){
            Toast.makeText(this@SmsActivity, " - 를 제외한 11자리를 입력해주세요", Toast.LENGTH_LONG).show()
        }else {
            phoneNumber = phone.substring(1, 11)
        }
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+82$phoneNumber",
            60,
            TimeUnit.SECONDS,
            this,
            callbacks)

    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @SuppressLint("LongLogTag")
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            val code = p0.smsCode
            Log.d("onVerificationCompleted ", code)

            if (code != null) {
                verifyEditText.setText(code)
                verifyEditText.visibility = View.VISIBLE
                verifyButton.visibility = View.VISIBLE
                buttonContinue.isEnabled = true
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            //Toast.makeText(this@SmsActivity, p0.message, Toast.LENGTH_LONG).show()
            if (p0 is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this@SmsActivity, " error ", Toast.LENGTH_LONG).show()
            } else if (p0 is FirebaseTooManyRequestsException) {
                Toast.makeText(this@SmsActivity, " 인증 많이함. 나중에 다시 해주세요. ", Toast.LENGTH_LONG).show()
            }
            buttonContinue.isEnabled = true
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            Log.d("  onCodeSent ", "  $p0    $p1")
            codeSent = p0
        }

    }

    private fun userData(){
        val uid = mAuth.currentUser!!.uid
        db.collection(resources.getString(R.string.db_user)).document(uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(
                        UserEntity::class.java)
                    Log.d(tag, userEntity?.name+"  "+userEntity?.address)
                    val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
                    val edit = prefs.edit()
                    edit.putString("uid", uid)
                    edit.putString("name", userEntity?.name)
                    edit.putString("address", userEntity?.address)
                    edit.putString("address2", userEntity?.address2)
                    edit.putString("imgPath", userEntity?.imgPath)
                    edit.putString("phoneNumber", phone)
                    edit.apply()
                } else {
                    Log.d(tag, "Error getting Users", task.exception)
                }
            }
    }

    private fun userExist(uid:String){
        var count =0
        db.collection(resources.getString(R.string.db_user)).get().addOnSuccessListener { result ->
            for(document in result){
                count++
                if(document.id == uid){
                    // 로그인
                    Log.d("유저 확인", "true  로그인")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("phoneNumber", phone)
                    userData()
                    tokenUpdate()
                    startActivity(intent)
                    break
                }else if(count ==  result.size()){
                    Log.d("유저 확인", "false  가입")
                    val intent = Intent(this, AddressActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("phoneNumber", phone)
                    startActivity(intent)
                }

            }

        }


    }

    private fun userDelete(){
        val uid = mAuth.currentUser!!.uid
        FirebaseAuth.getInstance().currentUser!!.delete().addOnCompleteListener { task ->
            if(task.isSuccessful){
                FirebaseAuth.getInstance().signOut()
                db.collection(resources.getString(R.string.db_user)).document(uid).update("status", 0)
                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun tokenUpdate(){
        val pref = getSharedPreferences("user", Context.MODE_PRIVATE)
        val token = pref.getString("token", "")
        val uid = mAuth.currentUser!!.uid
        db.collection(resources.getString(R.string.db_user)).document(uid)
            .update("token", token)
    }

}
