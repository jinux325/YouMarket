package com.u.marketapp.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.u.marketapp.R
import kotlinx.android.synthetic.main.activity_notice_read_page.*
import kotlinx.android.synthetic.main.activity_notice_read_page.notice_toolbar

class NoticeActivityReadPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice_read_page)

        setSupportActionBar(notice_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        notice_read_title.text = intent.getStringExtra("title")
        notice_read_date.text = intent.getStringExtra("date")
        notice_read_content.text = intent.getStringExtra("content")

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
