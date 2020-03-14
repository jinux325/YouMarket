package com.u.marketapp

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.adapter.ViewPagerAdapter
import com.u.marketapp.databinding.ActivityProductBinding
import com.u.marketapp.entity.ProductEntity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.activity_product.*

class ProductActivity : AppCompatActivity() {

    companion object {
        private val TAG = ProductActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product)

        if (intent.hasExtra("item")) {
            val item = intent.getSerializableExtra("item") as ProductEntity
            Log.i(TAG, item.toString())
            binding.setVariable(BR.product, item)
            getUserData(item.seller)
            setPagerAdater(item.imageArray)
        } else {
            Log.i(TAG, "Intent Not Signal!!")
        }

    }

    // 유저 정보 정의
    private fun getUserData(uid: String?) {
        val db = FirebaseFirestore.getInstance()
        uid?.let {
            db.collection("User").document(it).get().addOnCompleteListener { document ->
                if (document.isSuccessful) {
                    Log.i(TAG, "Successful! ${document.result!!.id}")
                    val user = document.result!!.toObject(UserEntity::class.java)
                    binding.setVariable(BR.user, user)
                }
            }
        }
    }

    // 이미지 페이저 정의
    private fun setPagerAdater(array: ArrayList<String>?) {
        array?.let {
            val viewPagerAdapter = ViewPagerAdapter()
            viewPagerAdapter.addImageList(it)
            view_pager.adapter = viewPagerAdapter
            viewPagerAdapter.itemClick = object : ViewPagerAdapter.ItemClick {
                override fun onClick(view: View, position: Int) {
                    Log.i(TAG, "image path : ${viewPagerAdapter.getImage(position)}")

                }
            }
        }
    }

}
