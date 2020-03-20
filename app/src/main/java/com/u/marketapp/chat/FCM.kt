package com.u.marketapp.chat

import android.util.Log
import kotlinx.android.synthetic.main.activity_chat.*
import org.json.JSONObject
import java.net.HttpURLConnection

class FCM(val token:String, val myName:String?, val comment:String?, val docId:String, val partnerName:String?) : Thread() {
    override fun run() {
        try {
            Log.e("@@ FCM class ", "token: $token  chatRoomUid: $docId")
            val serverKey =
                "AAAACA4EsA0:APA91bGdb7Oxa49X6z23tXjCn48DiosjzqYFZXM6G67I_gH5sFI_AKuoFJ6ayLyqBHGAmckEkMSO8UU5qD8XFesWRSlDKBVdx6zHI_cCEaz6xCTg4CbgWkKCNxVBzM3SupUJXio41w6a"
            val registrationToken: String = token
            val URL = "https://fcm.googleapis.com/fcm/send"
            val title = myName
            val body = comment
            val documentId = docId
            val partnerName = partnerName
            Log.e("@@ ChatActivity Thread", "$token  $title  $body")
            // FMC 메시지 생성 start
            val root = JSONObject()
            val data = JSONObject()
            data.put("title", title)
            data.put("body", body)
            data.put("click_action", "ChatActivity")
            data.put("documentId", documentId)
            data.put("partnerName", partnerName)
            //root.put("notification", notification);
            root.put("to", registrationToken)
            root.put("data", data)
            // FMC 메시지 생성 end
            Log.d("Main_ Thread", "@@@")
            val url = java.net.URL(URL)
            val conn =
                url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.doInput = true
            conn.addRequestProperty("Authorization", "key=$serverKey")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Content-type", "application/json")
            val os = conn.outputStream
            os.write(root.toString().toByteArray(charset("utf-8")))
            os.flush()
            conn.responseCode
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Main_ Thread", "@@")
        }
    }
}