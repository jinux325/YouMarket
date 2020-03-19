package com.u.marketapp.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.u.marketapp.R
import com.u.marketapp.SplashActivity
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setSupportActionBar(setting_toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        logout.setOnClickListener {
            Log.d("TAG", "로그아웃")
           /* val prefs = getSharedPreferences("User", Context.MODE_PRIVATE)
            val edit = prefs.edit()
            edit.putString("log", "OUT")
            edit.apply()*/
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@SettingActivity, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

     /*   version.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("Chatting", "ROOM")
            intent.putExtra("pid", "6iKkNnd91f7W6JqNCgtp")
            intent.putExtra("seller", "ijrvoWflm3ahqVyYoMRQu1INCEn1")
            intent.putExtra("name", "ㄷㄴㅅ6ㄹㅅㄷ")
            startActivity(intent)
        }*/
/*

        withdrawal.setOnClickListener{
            // kLIDekdZbCP0h99ZN8tIP3NhRct1
            //val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val buyer = "3Nlu6jrJ0UcBC4cGjty17mOXoVj1"
            val seller = "kLIDekdZbCP0h99ZN8tIP3NhRct1"
            val pid = "E1vYCUvs3algrwWCVRXF"

            val comment ="아아나람ㄴ란ㅁ라ㅜㅁㄴ  "+Date(System.currentTimeMillis())

            val chatRoom = hashMapOf(
                "pid" to pid,
                "buyer" to buyer,
                "seller" to seller,
                "comment" to comment ,
                "registDate" to Date(System.currentTimeMillis())
            )

            FirebaseFirestore.getInstance().collection("Users").document("3Nlu6jrJ0UcBC4cGjty17mOXoVj1").collection("Chatting").document()
                .set(chatRoom).addOnCompleteListener {
                    FirebaseFirestore.getInstance().collection("Users").document("3Nlu6jrJ0UcBC4cGjty17mOXoVj1").collection("Chatting")
                        .whereEqualTo("pid", "E1vYCUvs3algrwWCVRXF").whereEqualTo("buyer", "3Nlu6jrJ0UcBC4cGjty17mOXoVj1").get().addOnSuccessListener { result ->
                            for(document in result){
                                val chat = hashMapOf(
                                    "image" to "https://firebasestorage.googleapis.com/v0/b/umarket-d3048.appspot.com/o/Profile%2FkLIDekdZbCP0h99ZN8tIP3NhRct1%2F1567085889874.jpg?alt=media&token=dd4e7132-2285-45e0-8912-9478142e1074",
                                    "name" to "이름",
                                    "uid" to "kLIDekdZbCP0h99ZN8tIP3NhRct1",
                                    "message" to comment ,
                                    "registDate" to Date(System.currentTimeMillis())
                                )

                                FirebaseFirestore.getInstance().collection("Users").document("3Nlu6jrJ0UcBC4cGjty17mOXoVj1").collection("Chatting").document(document.id)
                                    .collection("comment").document().set(chat)
                            }
                        }
                }
            */


/*val chat = hashMapOf(
                                   "image" to "https://firebasestorage.googleapis.com/v0/b/umarket-d3048.appspot.com/o/Profile%2FkLIDekdZbCP0h99ZN8tIP3NhRct1%2F1567085889874.jpg?alt=media&token=dd4e7132-2285-45e0-8912-9478142e1074",
                                   "name" to "이름",
                                   "uid" to "kLIDekdZbCP0h99ZN8tIP3NhRct1",
                                   "message" to comment ,
                                   "registDate" to Date(System.currentTimeMillis())
                               )

                               FirebaseFirestore.getInstance().collection("Users").document("3Nlu6jrJ0UcBC4cGjty17mOXoVj1").collection("Chatting").document()
                                   .collection("comment").document().set(chat)*/



        /*

        }
*/

    }
}
