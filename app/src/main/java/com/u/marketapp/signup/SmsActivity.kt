package com.u.marketapp.signup

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.MainActivity
import com.u.marketapp.R
import com.u.marketapp.vo.UserEntity
import kotlinx.android.synthetic.main.activity_sms.*
import java.util.concurrent.TimeUnit

class SmsActivity : AppCompatActivity() {

    private val TAG = "SmsActivity"
    lateinit var mAuth: FirebaseAuth
    lateinit var codeSent : String
    var phone=""
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)

        mAuth= FirebaseAuth.getInstance()

        buttonContinue.setOnClickListener {
            sendVerification()
        }
        verifyButton.setOnClickListener {
            verifySignIn()
        }
       /*
        바로 로그인 기능
       button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("phoneNumber", "01040166410")
            userData()
            startActivity(intent)
        }
         주소입력으로 가기
        addrButton.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            intent.putExtra("phoneNumber", "01040166410")
            startActivity(intent)
        }*/
    }

    fun verifySignIn(){
        val code = verifyEditText.text.toString()
        val credential = PhoneAuthProvider.getCredential(codeSent, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent_items = getIntent()
                    val firebaseNumber = intent_items.getStringExtra("number")
                   // var phoneNumber = phone.substring(1, 11)
                    if(firebaseNumber == "NoNumber"){
                        // 가입
                        val intent = Intent(this, AddressActivity::class.java)
                        intent.putExtra("phoneNumber", phone)
                        startActivity(intent)
                    }else{
                        // 로그인
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("phoneNumber", phone)
                        userData()
                        startActivity(intent)
                    }
                    //Toast.makeText(this, " 이동할 activity",Toast.LENGTH_SHORT).show()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this,"upload failed: " + task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    fun sendVerification(){
        phone = editTextMobile.text.toString()

        if(phone.isEmpty()){
            editTextMobile.setError("번호를 입력해주세요")
        }

        var phoneNumber = phone.substring(1, 11)

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+82$phoneNumber", // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks

    }

    var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @SuppressLint("LongLogTag")
        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            val code = p0.getSmsCode()
            Log.d("onVerificationCompleted ", code)

            if (code != null) {
                verifyEditText.setText(code)
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(this@SmsActivity, p0.message, Toast.LENGTH_LONG).show()
            Toast.makeText(this@SmsActivity, "잠시후에 다시 시도해주세요.", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            Log.d("  onCodeSent ", "  $p0    $p1")
            codeSent = p0
        }

    }

    fun userData(){
        var uid = FirebaseAuth.getInstance().currentUser!!.uid
        db.collection("Users").document(uid).get()
            .addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(UserEntity::class.java)
                    Log.d(TAG, userEntity?.name+"  "+userEntity?.address)
                    val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
                    val edit = prefs.edit()
                    edit.putString("uid", uid)
                    edit.putString("name", userEntity?.name)
                    edit.putString("address", userEntity?.address)
                    edit.putString("address2", userEntity?.address2)
                    edit.putString("imgPath", userEntity?.imgPath)
                    edit.putString("phoneNumber", phone)
                    edit.putString("log", "IN")
                    edit.apply()
                } else {
                    Log.d(TAG, "Error getting Users", task.exception)
                }
            })
    }


}
