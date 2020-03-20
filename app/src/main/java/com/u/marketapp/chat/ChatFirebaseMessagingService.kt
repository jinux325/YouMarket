package com.u.marketapp.chat

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.u.marketapp.R
import com.u.marketapp.ReplyActivity

class ChatFirebaseMessagingService : FirebaseMessagingService() {
    //var open: PendingIntent? = null
    private val db = FirebaseFirestore.getInstance()
   // private val TAG = "ChatFirebase"
    override fun onNewToken(s: String) {
        super.onNewToken(s)
       /// val token = s
        Log.e("@@ token", "FirebaseInstanceServiceToken : $s")
        if (FirebaseAuth.getInstance().currentUser != null) {
            val uid = FirebaseAuth.getInstance().currentUser!!.uid
            db.collection(resources.getString(R.string.db_user)).document(uid)
                .update("token", s)
        }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("@@ onMessageReceived", "remoteMessage : "+ remoteMessage.data["title"]+"  "+remoteMessage.data["body"]+"  "+remoteMessage.data["documentId"])
        sendNotification(remoteMessage)
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val intent:Intent
        if(remoteMessage.data["click_action"]!=null){
            intent = Intent(this, ChatActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("chatRoomUid", remoteMessage.data["documentId"])
                putExtra("name", remoteMessage.data["partnerName"])
            }
        }else{
            intent = Intent(this, ReplyActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("pid", remoteMessage.data["documentId"])
            }
        }



        val channelId = "CollocNotification"
        val NAME = "CollocChannel"
        val description = "This is Colloc channel"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, NAME, importance)
            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.setShowBadge(false)
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.drawable.ic_tree)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["body"])
            .setAutoCancel(true)
            .setSound(notificationSound)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0, notificationBuilder.build())
    }




}