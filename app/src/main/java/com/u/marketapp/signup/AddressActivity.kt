package com.u.marketapp.signup

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.u.marketapp.R
import com.u.marketapp.adapter.AddressAdapter
import com.u.marketapp.vo.AddressVO
import kotlinx.android.synthetic.main.activity_address.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParser.TEXT
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStreamReader
import java.net.URL

class AddressActivity : AppCompatActivity() {

    val tag = "AddressActivity"
    var addressList : MutableList<AddressVO> = mutableListOf()
    var requestUrl: String? = null
    var page = 0
    var count = 0
    var isScrolling = false
    lateinit var phoneNumber:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.layoutManager = layoutManager


        bt.setOnClickListener {
            page = 1
            val async = AscynTask()
            async.execute()

        }

        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true
                    count = recyclerView.adapter!!.itemCount
                    val lastVisibleItemPosition =
                        (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                    val itemTotalCount = recyclerView.adapter!!.itemCount
                    Log.d(
                        tag,
                        "$lastVisibleItemPosition $itemTotalCount"
                    )
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                val itemTotalCount = recyclerView.adapter!!.itemCount
                if (isScrolling && lastVisibleItemPosition == itemTotalCount - 1) {
                    page += 1
                    isScrolling = false
                    val async = AscynTask()
                    async.execute()
                    Log.d(
                        tag,
                        "$lastVisibleItemPosition $itemTotalCount"
                    )
                }
            }
        })

    }


    @SuppressLint("StaticFieldLeak")
    inner class AscynTask : AsyncTask<Unit, Unit, String>() {
        override fun doInBackground(vararg params: Unit?): String? {
            requestUrl =
                "http://openapi.epost.go.kr/postal/retrieveNewAdressAreaCdSearchAllService/retrieveNewAdressAreaCdSearchAllService/getNewAddressListAreaCdSearchAll?" +
                        "ServiceKey=fj5YE2fY3AahDnTdIExq3xzd3T4CEs3NtkH5r%2F1HIeUBj7w%2B9Nvsi32mHXdFlQgeHKod9XnRTFYJYWc9nwr%2FtA%3D%3D&countPerPage=20&" +
                        "currentPage=" + page + "&" +
                        "srchwrd=" + txt.text
            try {
                var zipNo = false
                var rnAddr = false
                var lnmAddr = false
                var zipNoVal = ""
                var rnAddrVal = ""
                var lnmAddrVal = ""

                val url = URL(requestUrl)
                val `is` = url.openStream()
                val factory =
                    XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(InputStreamReader(`is`, "UTF-8"))

                var eventType = parser.eventType
                if (page == 1) addressList = mutableListOf()
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_DOCUMENT -> {
                        }
                        XmlPullParser.END_DOCUMENT -> {
                        }
                        XmlPullParser.END_TAG -> if (parser.name == "newAddressListAreaCdSearchAll") {
                            Log.d("2@@@ ", " $zipNoVal  $rnAddrVal   $lnmAddrVal")
                            addressList.add(AddressVO(zipNoVal,rnAddrVal,lnmAddrVal))
                            zipNoVal=""
                            rnAddrVal=""
                            lnmAddrVal=""
                        }
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "zipNo") zipNo = true
                            if (parser.name == "rnAdres") rnAddr = true
                            if (parser.name == "lnmAdres") lnmAddr = true
                        }
                        TEXT -> {
                            when {
                                zipNo -> {
                                    //item.setlAddr(parser.text)
                                    zipNoVal = parser.text
                                    zipNo = false
                                }
                                rnAddr -> {
                                    // item.setrAddr(parser.text)
                                    rnAddrVal = parser.text
                                    rnAddr = false
                                }
                                lnmAddr -> {
                                    //item.setZipNo(parser.text)
                                    lnmAddrVal = parser.text
                                    lnmAddr = false
                                }
                            }
                        }
                    }
                    eventType = parser.next()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //어답터 연결
            val intentItems = intent
            if (intentItems.getStringExtra("update") != null) {
                Log.e(tag, "location1: " + intentItems.getStringExtra("update"))
                val location = intentItems.getStringExtra("update")
                val addrAdapter =
                    AddressAdapter(this@AddressActivity, addressList, "", location)



                recycler_view.visibility = View.VISIBLE
                recycler_view.adapter = addrAdapter


            } else {
                val intentItem = intent
                phoneNumber = intentItem.getStringExtra("phoneNumber")
                Log.d("@adapter@ activity ", " $phoneNumber ")

                Log.e(tag, "location2: ")
                val adapter = AddressAdapter(this@AddressActivity, addressList, phoneNumber, "")



                this@AddressActivity.recycler_view.visibility = View.VISIBLE
                recycler_view.adapter = adapter
            }
            if (page > 1) {
                recycler_view.layoutManager!!.scrollToPosition(count - 3)
            }
        }
    }


}
