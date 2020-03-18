package com.u.marketapp

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.setting.AccountProfileActivity
import com.u.marketapp.setting.LocationSettingActivity
import com.u.marketapp.setting.SettingActivity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.view.*


class AccountFragment : Fragment() {

    private lateinit var myData: UserEntity
    private val myUid = FirebaseAuth.getInstance().currentUser!!.uid
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        //(activity as AppCompatActivity).supportActionBar?.hide()

      /*
        Log.d("myData ", " myData : ${myData.name}, ${myData.address}")
        */

      //  setHasOptionsMenu(true)
       // (activity as MainActivity?)!!.setSupportActionBar(account_toolbar) //액션바와 같게 만들어줌


        view.account_profile.setOnClickListener { startActivity(Intent(activity, AccountProfileActivity::class.java)) }
        view.account_setting.setOnClickListener { startActivity(Intent(activity, SettingActivity::class.java)) }
        view.location.setOnClickListener { startActivity(Intent(activity, LocationSettingActivity::class.java)) }

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
        myData()

    }

    fun myData(){
        db.collection(resources.getString(R.string.db_user)).document(myUid).get()
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val userEntity: UserEntity? = task.result!!.toObject<UserEntity>(
                        UserEntity::class.java)
                    Glide.with(this).load(userEntity!!.imgPath)
                        .apply(RequestOptions.bitmapTransform(CircleCrop())).into(account_profile)
                    account_name.text = userEntity.name
                    account_address.text = userEntity.address
                    myData = userEntity
                }
            }
    }


}