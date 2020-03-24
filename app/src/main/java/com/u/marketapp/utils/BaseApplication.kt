package com.u.marketapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.u.marketapp.R

@SuppressLint("Registered")
class BaseApplication : Application() {

    companion object {
        private val TAG = BaseApplication::class.java.simpleName
        val instance = BaseApplication()
    }

    private var progressDialog: AppCompatDialog? = null

    // 로딩 활성화
    fun progressON(activity: Activity?, message: String) {
        if (activity == null || activity.isFinishing) return

        if (progressDialog != null && progressDialog!!.isShowing) {
            progressSET(message)
        } else {
            progressDialog = AppCompatDialog(activity)
            progressDialog?.let {
                it.setCancelable(false)
                it.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.setContentView(R.layout.layout_loading)
                it.show()
            }
            progressSET(message)
        }
    }

    // 로딩 정의
    private fun progressSET(message: String) {
        Log.i(TAG, "progress message : $message")

        if (progressDialog == null || !progressDialog!!.isShowing) return

        val textViewMessage = progressDialog!!.findViewById(R.id.text_view_message) as TextView?
        if (!TextUtils.isEmpty(message)) textViewMessage!!.text = message
    }

    // 로딩 비활성화
    fun progressOFF() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

}