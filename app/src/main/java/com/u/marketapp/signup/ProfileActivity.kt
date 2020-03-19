package com.u.marketapp.signup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.common.io.Files.getFileExtension
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.u.marketapp.MainActivity
import com.u.marketapp.R
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_profile.*
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"
    private var profileImage : Uri? =null
    private lateinit var phoneNumber:String
    lateinit var address:String
    var uid: String? = null

    private val db = FirebaseFirestore.getInstance()
    private var mStorageRef: StorageReference? = FirebaseStorage.getInstance().getReference("Profile")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        Glide.with(this).load(R.drawable.ic_profile)
            .apply(RequestOptions.bitmapTransform(CircleCrop())).into(proflie_imageView)

        proflie_imageView.setOnClickListener {
            Toast.makeText(this," 이미지 선택",Toast.LENGTH_SHORT).show()
            permission()
        }

        text_inpur_edit.isCounterEnabled = true
        text_inpur_edit.counterMaxLength = 10


        profile_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                when {
                    s.isEmpty() -> {
                        text_inpur_edit.error = null
                    }
                    s.length > 10 -> {
                        text_inpur_edit.error = "10자 이하로 적어주세요."
                    }
                    else -> {
                        text_inpur_edit.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })


    }

    // toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_profile, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.join -> {
                val name = profile_name.text.toString()
                if (name.replace(" ", "").isEmpty()) {
                    Toast.makeText(this@ProfileActivity, "닉네임을 입력해주세요", Toast.LENGTH_LONG).show()
                } else if (name.length >= 11) {
                    Toast.makeText(this@ProfileActivity, "10자 이하로 적어주세요.", Toast.LENGTH_LONG).show()
                } else if (name.length in 1..10) {
                    if (profileImage == null || profileImage.toString().isEmpty()) {
                        profileImage = Uri.parse("android.resource://com.u.marketapp/drawable/ic_default")
                    }
                    try {
                        join(name)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
                true
            }
            else -> {
                Toast.makeText(applicationContext, "나머지 버튼 클릭됨", Toast.LENGTH_LONG).show()
                super.onOptionsItemSelected(item)
            }
        }
    }

    // 퍼미션 권한 설정
    private fun permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("TAG", "권한 설정 완료")
                TedImagePicker.with(this)
                    .start { uriList -> getImageList(uriList) }
            } else {
                Log.d("TAG", "권한 설정 요청")
                ActivityCompat.requestPermissions( this, arrayOf(
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET), 1 )
            }
        }else{
            TedImagePicker.with(this)
                .start { uriList -> getImageList(uriList) }
        }

    }

    private fun getImageList(image: Uri){
        profileImage = image
        Glide.with(this).load(image)
            .apply(RequestOptions.bitmapTransform(CircleCrop())).into(proflie_imageView)

    }


    private fun join(name:String) {
        val intentItems = intent
        phoneNumber = intentItems.getStringExtra("phoneNumber")
        address = intentItems.getStringExtra("address")
        Log.d("@adapter@ activity ", " $phoneNumber  $address")

        val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
        val edit = prefs.edit()

        uid = FirebaseAuth.getInstance().currentUser?.uid
        val fileReference: StorageReference = mStorageRef!!.child(uid!!)
            .child(System.currentTimeMillis().toString() + "." + getFileExtension(profileImage.toString()))
        fileReference.putFile(profileImage!!).continueWithTask { task ->
            if (!task.isSuccessful) {
                throw task.exception!!
            }
            fileReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result

                val user = hashMapOf(
                   // "uid" to uid,
                    "name" to name,
                    "address" to address,
                    "registDate" to Date(System.currentTimeMillis()),
                    "imgPath" to downloadUri.toString(),
                    "token" to FirebaseInstanceId.getInstance().token
                )

                db.collection(resources.getString(R.string.db_user)).document(uid!!)
                    .set(user)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                edit.putString("uid", uid)
                edit.putString("name", name)
                edit.putString("address", address)
                edit.putString("imgPath", downloadUri.toString())
                edit.putString("phoneNumber", phoneNumber)

                //edit.apply(); //비동기 처리
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                edit.putString("log", "IN")
                edit.apply()

                startActivity(intent)

            } else {
                Toast.makeText(
                    this@ProfileActivity,
                    "upload failed: " + task.exception!!.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

/*
    private fun uploadFile() {
        if (imgPath != null) {
            uid = FirebaseAuth.getInstance().currentUser!!.uid
            Log.e("@@$TAG", uid)
            val fileReference = mStorageRef!!.child(uid!!).child(
                System.currentTimeMillis().toString() + "." + getFileExtension(imgPath)
            )
            Log.d("@@ image ", imgPath.toString())
            if (imgPath != null) {
                fileReference.putFile(imgPath).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        throw task.exception!!
                    }
                    fileReference.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val prefs =
                            getSharedPreferences("User", Context.MODE_PRIVATE)
                        val edit = prefs.edit()
                        val log = prefs.getString("log", "")
                        val token = prefs.getString("token", "")
                        *//*      if(log.equals("OUT")){
                                                        Log.d(TAG, "pref: OUT");
                                                        edit.putString("log","IN");
                                                        edit.apply();

                                                        Intent intent = new Intent(VerifyPhoneActivity.this, MainActivity.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                    }else {
                        *//*Log.d(TAG, "pref: X")
                        val downloadUri = task.result
                        Log.e(TAG, "then: " + downloadUri.toString())
                        val user: MutableMap<String, Any> =
                            HashMap()
                        user["name"] = name
                        user["address"] = address
                        user["registDate"] = Date(System.currentTimeMillis())
                        user["imgPath"] = downloadUri.toString()
                        user["token"] = FirebaseInstanceId.getInstance().getToken()
                        db.collection("Users").document(uid!!)
                            .set(user)
                        edit.putString("uid", uid)
                        edit.putString("name", name)
                        edit.putString("address", address)
                        edit.putString("imgPath", downloadUri.toString())
                        Log.d(
                            "@@ output result : ",
                            uid + " " + name + " " + downloadUri.toString() + " "
                        )
                        //edit.apply(); //비동기 처리
                        val intent =
                            Intent(this@VerifyPhoneActivity, MainActivity::class.java)
                        Log.d("@@ Verify", "mobile_name: $name")
                        Log.d("@@ Verify", "mobile_imgPath: $imgPath")
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        edit.putString("log", "IN")
                        edit.apply()
                        Log.e("@@@@@@@@@@@@@@@@", "222222222222")
                        startActivity(intent)
                        // edit.putString("mobile", mobile);
                        *//* Intent intent = new Intent(VerifyPhoneActivity.this, AddressActivity.class);
                                                                    intent.putExtra("mobile", mobile);
                                                                    Log.d("@@ Verify", "mobile_name: " + name);
                                                                    Log.d("@@ Verify", "mobile_imgPath: " + imgPath);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                                    startActivity(intent);*//*
                        *//* }*//*
                    } else {
                        Toast.makeText(
                            this@VerifyPhoneActivity,
                            "upload failed: " + task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            val prefs =
                getSharedPreferences("User", Context.MODE_PRIVATE)
            val edit = prefs.edit()
            Log.d(TAG, "pref: OUT")
            edit.putString("log", "IN")
            edit.apply()
            val intent = Intent(this@VerifyPhoneActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }*/
}
