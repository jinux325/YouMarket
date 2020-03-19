package com.u.marketapp.setting

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.common.io.Files
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.u.marketapp.R
import com.u.marketapp.entity.UserEntity
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.android.synthetic.main.activity_profile.*

class AccountProfileActivity : AppCompatActivity() {

    private val TAG = "AccountProfileActivity"
    lateinit var dbImage:String
    var profileImage : Uri? = null
    lateinit var db_name : String
    lateinit var phoneNumber:String
    lateinit var address:String
    var uid: String? = null

    private lateinit var myData: UserEntity
    private val myUid = FirebaseAuth.getInstance().currentUser!!.uid
    private val db = FirebaseFirestore.getInstance()
    private var mStorageRef: StorageReference? = FirebaseStorage.getInstance().getReference("Profile")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        myData()

        text_inpur_edit.helperText = "현재: $db_name"


        proflie_imageView.setOnClickListener {
            Toast.makeText(this," 이미지 선택",Toast.LENGTH_SHORT).show()
            permission()
        }

        text_inpur_edit.setCounterEnabled(true)
        text_inpur_edit.setCounterMaxLength(10)

        profile_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int ) {
            }

            override fun onTextChanged( s: CharSequence, start: Int, before: Int, count: Int ) {

                if (s.length == 0) {
                    text_inpur_edit.setError(null)
                } else if (s.length > 10) {
                    text_inpur_edit.setError("10자 이하로 적어주세요.")
                } else {
                    text_inpur_edit.setError(null)
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
                Log.e("imgPath 1111 ", " $name  $profileImage")
               /* if (name.length <= 0) {
                    Toast.makeText(this, " $db_name 으로 하시겠어요?", Toast.LENGTH_LONG).show()
                } else */
                if (name.length >= 11) {
                    Toast.makeText(this, "10자 이하로 적어주세요.", Toast.LENGTH_LONG).show()
                } else if (name.length >= 0 && name.length <= 10) {
                    Log.e("imgPath 2222 ", " $name  $profileImage")
                  /*  if (profileImage == null || profileImage.toString().length == 0 ) {
                    } else {*/
                        try {
                            Log.e("imgPath 3333 ", " $name  $profileImage")
                            if(name.replace(" ","").length <= 0){
                                update(db_name, profileImage)
                            }else{
                                update(name, profileImage)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    //}
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
    fun permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("TAG", "권한 설정 완료");
                TedImagePicker.with(this)
                    .start { uriList -> getImageList(uriList) }
            } else {
                Log.d("TAG", "권한 설정 요청");
                ActivityCompat.requestPermissions( this, arrayOf(
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET), 1 )
            }
        }else{
            TedImagePicker.with(this)
                .start { uriList -> getImageList(uriList) }
        }

    }

    fun getImageList(image: Uri){
        profileImage = image
        Glide.with(this).load(image)
            .apply(RequestOptions.bitmapTransform(CircleCrop())).into(proflie_imageView)

    }


    private fun update(name:String, imgPath:Uri?) {
        Log.e("imgPath  ", "$name  $imgPath")
        if(imgPath == null){
            db.collection(resources.getString(R.string.db_user)).document(myUid)
                .update("name" , name)
                .addOnSuccessListener {
                    finish()
                }
        }else {
            val fileReference: StorageReference = mStorageRef!!.child(myUid!!)
                .child(System.currentTimeMillis().toString() + "." + Files.getFileExtension(profileImage.toString()))
            fileReference.putFile(profileImage!!).continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                fileReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result


                    db.collection(resources.getString(R.string.db_user)).document(myUid)
                        .update("imgPath", downloadUri.toString(), "name", name)
                        .addOnSuccessListener {
                            finish()
                        }


                } else {
                    Toast.makeText(
                        this,
                        "upload failed: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }


    override fun onResume() {
        super.onResume()
       // myData()
    }

    fun myData() {
        val intent_items = intent
        dbImage= intent_items.getStringExtra("imgPath")
        db_name = intent_items.getStringExtra("name")
        Glide.with(this).load(dbImage)
            .apply(RequestOptions.bitmapTransform(CircleCrop())).into(proflie_imageView)
        /*db.collection(resources.getString(R.string.db_user)).document(FirebaseAuth.getInstance().currentUser!!.uid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(
                        UserEntity::class.java)
                    Glide.with(this).load(userEntity!!.imgPath)
                        .apply(RequestOptions.bitmapTransform(CircleCrop())).into(proflie_imageView)
                    dbImage= userEntity.imgPath.toString()
                    name = userEntity.name.toString()

                }
            }*/
    }


}