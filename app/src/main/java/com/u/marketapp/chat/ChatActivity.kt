package com.u.marketapp.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        iv_send.setOnClickListener {
            // kLIDekdZbCP0h99ZN8tIP3NhRct1
            //val uid = FirebaseAuth.getInstance().currentUser!!.uid
            val buyer = "3Nlu6jrJ0UcBC4cGjty17mOXoVj1"
            val seller = "kLIDekdZbCP0h99ZN8tIP3NhRct1"
            val pid = "E1vYCUvs3algrwWCVRXF"

            val comment ="아아나람ㄴ란ㅁ라ㅜㅁㄴ  "+ Date(System.currentTimeMillis())

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
        }


    }
}
