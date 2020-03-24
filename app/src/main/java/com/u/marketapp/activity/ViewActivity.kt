package com.u.marketapp.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.u.marketapp.R
import com.u.marketapp.adapter.ViewPagerAdapter2
import kotlinx.android.synthetic.main.activity_product.*

class ViewActivity : AppCompatActivity() {

    companion object {
        private val TAG = ViewActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val viewPagerAdapter = ViewPagerAdapter2()
        if (intent.hasExtra("imageArray")) {
            val array = intent.getStringArrayListExtra("imageArray")
            Log.i(TAG, array.toString())
            viewPagerAdapter.addImageList(array)
            view_pager.adapter = viewPagerAdapter
        }
        if (intent.hasExtra("position")) {
            val position = intent.getIntExtra("position", 0)
            Log.i(TAG, "current position : $position")
            view_pager.currentItem = position
        }
    }
}
