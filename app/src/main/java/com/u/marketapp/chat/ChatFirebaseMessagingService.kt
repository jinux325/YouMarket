package com.u.marketapp.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.u.marketapp.activity.CommentActivity
import com.u.marketapp.R
import com.u.marketapp.activity.ReplyActivity


class ChatFirebaseMessagingService : FirebaseMessagingService() {
    //var open: PendingIntent? = null
    private val db = FirebaseFirestore.getInstance()
   // private val TAG = "ChatFirebase"
    override fun onNewToken(s: String) {
        super.onNewToken(s)
       /// val token = s
        Log.e(" @@ token", " $s")

      // Log.e(" @@ token ", FirebaseInstanceId.getInstance().token)
        if (FirebaseAuth.getInstance().currentUser != null) {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            db.collection(resources.getString(R.string.db_user)).document(uid)
                .update("token", s)
        }else{
            val pref = getSharedPreferences("user", Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString("token", s).apply()
        }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("@@ onMessageReceived", "remoteMessage : "+ remoteMessage.data["title"]+"  "+remoteMessage.data["body"]+"  "+remoteMessage.data["documentId"])
        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)
        val prefReplySwitch = pref.getString("replySwitch", "")
        val prefChatttingSwitch = pref.getString("chattingSwitch", "")

        if(remoteMessage.data["partnerName"] != ""){
            if(prefChatttingSwitch=="true"){
                sendNotification(remoteMessage)
            }
        }else{
            if(prefReplySwitch=="true"){
                sendNotification(remoteMessage)
            }
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val intent:Intent
        when {
            remoteMessage.data["click_action"] != resources.getString(R.string.ChatActivity) -> {
                intent = Intent(this, ChatActivity::class.java).apply {
                    flags = FLAG_ACTIVITY_NO_HISTORY
                    putExtra("chatRoomUid", remoteMessage.data["documentId"])
                    putExtra("name", remoteMessage.data["partnerName"])
                }
            }
            remoteMessage.data["click_action"] != resources.getString(R.string.CommentActivity) -> {
                intent = Intent(this, CommentActivity::class.java).apply {
                    flags = FLAG_ACTIVITY_NO_HISTORY
                    putExtra("pid", remoteMessage.data["documentId"])
                }
            }
            else -> {
                intent = Intent(this, ReplyActivity::class.java).apply {
                    flags = FLAG_ACTIVITY_NO_HISTORY
                    putExtra("pid", remoteMessage.data["documentId"])
                }
            }
        }

        //val channelId = "Notification"
        val channelId:String
        val name :String
        val description :String

        val pref = getSharedPreferences("setting", Context.MODE_PRIVATE)
          val prefVibrationSwitch = pref.getString("vibrationSwitch", "")

         channelId = if(prefVibrationSwitch=="true"){
              Log.e("FCM ", " Vibration")
              "Vibration"
          }else{
              Log.e("FCM ", " no Vibration")
              "NoVibration"
          }

        val notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance:Int

            if(channelId ==  "NoVibration"){
                name = "No vibration channel"
                description = "No vibration channel"
                importance = NotificationManager.IMPORTANCE_HIGH
                Log.e("send no Vibration "," $channelId, $name, $importance")

                val channel = NotificationChannel(channelId, name, importance)

                channel.description = description
                channel.setShowBadge(false)
                channel.enableVibration(false)

                channel.setSound(null,null)

                notificationManager.createNotificationChannel(channel)
            }else{
                name = "Vibration channel"
                description = "Vibration channel"
                importance = NotificationManager.IMPORTANCE_HIGH
                Log.e("send Vibration "," $channelId, $name, $importance")

                val channel = NotificationChannel(channelId, name, importance)
                channel.description = description
                channel.enableVibration(true)
                channel.setShowBadge(false)

                notificationManager.createNotificationChannel(channel)

            }


        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.drawable.ic_tree)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["body"])
            .setContentIntent(pendingIntent)
            .setShowWhen(true)

        notificationManager.notify(0, notificationBuilder.build())
    }




}