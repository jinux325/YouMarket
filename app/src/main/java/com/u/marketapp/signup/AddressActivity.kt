package com.u.marketapp.signup

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.u.marketapp.R
import com.u.marketapp.adapter.AddressAdapter
import com.u.marketapp.vo.AddressVO
import kotlinx.android.synthetic.main.activity_address.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStreamReader
import java.net.URL

class AddressActivity : AppCompatActivity() {

    val TAG = "AddressActivity"
    var addressList : MutableList<AddressVO> = mutableListOf()
    var requestUrl: String? = null
    var page = 0
    var count = 0
    var isScrolling = false
    lateinit var phoneNumber:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

       // addressList = mutableListOf(AddressVO("1","1","1"),AddressVO("2","2","2"),AddressVO("3","3","3"),AddressVO("4","4","4"))

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler_view.setLayoutManager(layoutManager)
       /* val addrAdapter = AddressAdapter(this, addressList)
        recycler_view.setAdapter(addrAdapter)*/

        bt.setOnClickListener(View.OnClickListener {
            page = 1
            val async = asyncTask()
            async.execute()
        })

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
                        TAG,
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
                    page = page + 1
                    isScrolling = false
                    /*if (loadData.isCancelled){

                    }*/
                    val async = asyncTask()
                    async.execute()
                    Log.d(
                        TAG,
                        "$lastVisibleItemPosition $itemTotalCount"
                    )
                }
            }
        })

    }


    inner class asyncTask : AsyncTask<Unit, Unit, String>() {
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
                            if (parser.name == "newAddressListAreaCdSearchAll") {
                               // item =
                            }
                            if (parser.name == "zipNo") zipNo = true
                            if (parser.name == "rnAdres") rnAddr = true
                            if (parser.name == "lnmAdres") lnmAddr = true
                        }
                        XmlPullParser.TEXT -> if (zipNo) {
                            //item.setlAddr(parser.text)
                            zipNoVal = parser.text
                            zipNo = false
                        } else if (rnAddr) {
                           // item.setrAddr(parser.text)
                            rnAddrVal = parser.text
                            rnAddr = false
                        } else if (lnmAddr) {
                            //item.setZipNo(parser.text)
                            lnmAddrVal = parser.text
                            lnmAddr = false
                        }
                    }
                    eventType = parser.next()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }

        override fun onPreExecute() {
            super.onPreExecute()

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            //어답터 연결
            val intent_items = intent
            if (intent_items.getStringExtra("update") != null) {
                Log.e(TAG, "location1: " + intent_items.getStringExtra("update"))
                val location = intent_items.getStringExtra("update")
                val addrAdapter =
                    AddressAdapter(this@AddressActivity, addressList, "", location)
                recycler_view.setAdapter(addrAdapter)
            } else {
                val intent_items = getIntent()
                phoneNumber = intent_items.getStringExtra("phoneNumber")
                Log.d("@adapter@ activity ", " $phoneNumber ")

                Log.e(TAG, "location2: ")
                val adapter = AddressAdapter(this@AddressActivity, addressList, phoneNumber, "")
                recycler_view.visibility = View.GONE
                recycler_view.setAdapter(adapter)
            }
            if (page > 1) {
                recycler_view.getLayoutManager()!!.scrollToPosition(count - 3)
            }
        }
    }

 /*   override fun onDestroy() {
        super.onDestroy()
        val intent_items = intent
        Log.d("@@ onDestroy  ", intent_items.getStringExtra("update"))
        if(intent_items.getStringExtra("update") == null){
            FirebaseAuth.getInstance().signOut()
        }
    }
*/


}
