package com.u.marketapp.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.u.marketapp.R
import com.u.marketapp.adapter.NoticeAdapter
import com.u.marketapp.vo.ChatRoomVO
import com.u.marketapp.vo.NoticeVO
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_notice.*

class NoticeActivity : AppCompatActivity() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        //title = "공지사항"
    /*    val toolbar = findViewById(R.id.notice_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)*/
        setSupportActionBar(notice_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

       /* db.collection("Notice").orderBy("desc").get().addOnSuccessListener { result ->
            val list= mutableListOf<NoticeVO>()
            for(document in result){
                val noticeVO: NoticeVO = document.toObject(NoticeVO::class.java)
                Log.e(" "," ${noticeVO.title}  ${noticeVO.date}")
                list.add(noticeVO)
            }

            rv_notice_setting.layoutManager = LinearLayoutManager(this)
            rv_notice_setting.adapter = NoticeAdapter(this, list)

        }*/
        db.collection("Notice").orderBy("date",
            Query.Direction.DESCENDING).addSnapshotListener{ snapshot, _ ->
            val list= mutableListOf<NoticeVO>()
            for (doc in snapshot!!) {
                val noticeVO: NoticeVO = doc.toObject(NoticeVO::class.java)
                Log.e("notice  "," ${noticeVO.title}  ${noticeVO.date}")
                list.add(noticeVO)
            }

            rv_notice_setting.layoutManager = LinearLayoutManager(this)
            rv_notice_setting.adapter = NoticeAdapter(this, list)
        }


    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
