package com.u.marketapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.setting.LocationSettingActivity
import com.u.marketapp.signup.ProfileActivity
import com.u.marketapp.vo.AddressVO
import kotlinx.android.synthetic.main.item_address.view.*

class AddressAdapter (val context: Context, private var addressList:MutableList<AddressVO>, private var phoneNumber:String, private var location:String):
    RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val zipNo = addressList[position].zipNo
        val rAddr = addressList[position].rAddr
        val lAddr = addressList[position].lAddr

        holder.addressZipNo.text = zipNo
        holder.addressRAddr.text = rAddr
        holder.addressLAddr.text = lAddr

        holder.addressCardView.setOnClickListener {
           // val locationSettingActivity = LocationSettingActivity()
            addressDialog(context, lAddr, phoneNumber,location)
            //addressDialog(context, lAddr, phoneNumber,location)
        }

    }
}

class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
    val addressZipNo = itemView.tv_zipNo!!
    val addressRAddr = itemView.tv_rnAddr!!
    val addressLAddr = itemView.tv_lnmAddr!!
    var addressCardView = itemView.cardview!!
}



fun addressDialog( context: Context, lAddr:String, phoneNumber: String,location:String){
    val dialog = AlertDialog.Builder(context)
    dialog.setMessage("주소가 '$lAddr' 이(가) 맞습니까?").setCancelable(false)

    val db = FirebaseFirestore.getInstance()
    fun pos(){
        if (location != "" && location == "update1") {
            if (FirebaseAuth.getInstance().currentUser != null) {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                db.collection(context.resources.getString(R.string.db_user)).document(uid).update("address", addrSubString(lAddr))

                val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
                val edit = prefs.edit()
                edit.putString("address", addrSubString(lAddr)).apply()

                val intent = Intent(context, LocationSettingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }
        } else if (location != "" && location == "update2") {
            if (FirebaseAuth.getInstance().currentUser != null) {
                val uid = FirebaseAuth.getInstance().currentUser!!.uid
                db.collection(context.resources.getString(R.string.db_user)).document(uid).update("address2", addrSubString(lAddr))

                val prefs = context.getSharedPreferences("User", Context.MODE_PRIVATE)
                val edit = prefs.edit()
                edit.putString("address", addrSubString(lAddr)).apply()

                val intent = Intent(context, LocationSettingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(intent)
            }
        } else {
            val intent = Intent(context, ProfileActivity::class.java)
            //Toast.makeText(context," $lAddr  ",Toast.LENGTH_SHORT).show()
            intent.putExtra("phoneNumber", phoneNumber)
            intent.putExtra("address",  addrSubString(lAddr))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }

    }
    val dialogListener = DialogInterface.OnClickListener { _, p1 ->
        when(p1){
            DialogInterface.BUTTON_POSITIVE -> pos()
        }
    }
    dialog.setPositiveButton("확인",dialogListener)
    dialog.setNegativeButton("취소",dialogListener)
    dialog.show()

}



fun addrSubString(lAddr:String):String{
    var addr=""
    if(lAddr.replace(" ","") != ""){
        val idxBf = lAddr.indexOf("(")
        val idxAf = lAddr.indexOf(")")
        addr = if (lAddr.contains(",")) {
            val idx = lAddr.indexOf(",")
            lAddr.substring(idxBf + 1, idx)
        } else {
            lAddr.substring(idxBf + 1, idxAf)
        }
    }
    return addr
}

