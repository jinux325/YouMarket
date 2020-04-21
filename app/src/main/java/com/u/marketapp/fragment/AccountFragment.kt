package com.u.marketapp.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.activity.AttentionHistoryActivity
import com.u.marketapp.activity.MainActivity
import com.u.marketapp.activity.PurchaseHistoryActivity
import com.u.marketapp.activity.SalesHistoryActivity
import com.u.marketapp.entity.UserEntity
import com.u.marketapp.setting.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*


class AccountFragment : Fragment() {

    private lateinit var myData: UserEntity
    private val myUid = FirebaseAuth.getInstance().currentUser!!.uid
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        view.account_profile.setOnClickListener {
            startActivity(Intent(activity, AccountProfileActivity::class.java).putExtra("name",myData.name).putExtra("imgPath",myData.imgPath))
        }
        view.account_setting.setOnClickListener { startActivity(Intent(activity, SettingActivity::class.java)) }
        view.location.setOnClickListener { startActivity(Intent(activity, LocationSettingActivity::class.java)) }
        view.btn_profile.setOnClickListener {
            startActivity(Intent(activity, AccountProfileActivity::class.java).putExtra("name",myData.name).putExtra("imgPath",myData.imgPath))
        }
        view.location_verify.setOnClickListener { msgDialog() }
        view.notice_setting.setOnClickListener { startActivity(Intent(activity, NoticeActivity::class.java)) }
        view.sendEmail.setOnClickListener { startActivity(Intent(activity, MailActivity::class.java)) }
       // view.app_send.setOnClickListener{ msgDialog() }

        view.sell_list.setOnClickListener { startActivity(Intent(activity, SalesHistoryActivity::class.java)) }
        view.buy_list.setOnClickListener { startActivity(Intent(activity, PurchaseHistoryActivity::class.java)) }
        view.like_list.setOnClickListener { startActivity(Intent(activity, AttentionHistoryActivity::class.java)) }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity?)!!.setSupportActionBar(account_toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_account, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.setting -> {
                startActivity(Intent(activity, SettingActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("AccountFragment ", "onResume")
        myData()

    }

    private fun myData(){
        db.collection(resources.getString(R.string.db_user)).document(myUid).get()
            .addOnSuccessListener { documentSnapshot ->
                val userEntity: UserEntity? = documentSnapshot.toObject<UserEntity>(
                    UserEntity::class.java)
                Glide.with(this).load(userEntity!!.imgPath)
                    .apply(RequestOptions.bitmapTransform(CircleCrop())).into(account_profile)
                account_name.text = userEntity.name

                val prefs = activity!!.getSharedPreferences("User", Context.MODE_PRIVATE)

                account_address.text = prefs.getString("address", "")
                myData = userEntity

            }
    }

    private fun msgDialog(){
        val dialog = AlertDialog.Builder(context)
        dialog.setMessage("업데이트 예정입니다. ").setCancelable(false)
        val dialogListener = object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                when(p1){
                    DialogInterface.BUTTON_POSITIVE ->
                        dialog?.dismiss()
                }
            }
        }
        dialog.setPositiveButton("확인",dialogListener)
        dialog.setNegativeButton("취소",dialogListener)
        dialog.show()

    }

}