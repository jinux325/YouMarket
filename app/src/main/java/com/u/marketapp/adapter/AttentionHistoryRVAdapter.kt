package com.u.marketapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.u.marketapp.R
import com.u.marketapp.databinding.LayoutProduct6Binding
import com.u.marketapp.entity.ProductEntity
import kotlinx.android.synthetic.main.layout_product6.view.*

class AttentionHistoryRVAdapter : RecyclerView.Adapter<AttentionHistoryRVAdapter.ViewHolder>() {

    companion object {
        private val TAG = AttentionHistoryRVAdapter::class.java.simpleName
    }

    private val items : ArrayList<DocumentSnapshot> = ArrayList()

    interface ItemClickListener { fun onClick(view: View, position: Int) }
    private lateinit var itemClickListener: ItemClickListener
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    interface CheckBoxClickListener { fun onClick(view: View, position: Int, isChecked: Boolean) }
    private lateinit var checkBoxClick: CheckBoxClickListener
    fun setCheckBoxClickListener(checkBoxClick: CheckBoxClickListener) {
        this.checkBoxClick = checkBoxClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.layout_product6, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            bind(item)
            itemView.tag = item
            itemView.setOnClickListener { itemClickListener.onClick(it, position) }
            itemView.check_box.isChecked = true
            itemView.check_box.setOnCheckedChangeListener { buttonView, isChecked ->
                checkBoxClick.onClick(buttonView, position, isChecked)
                buttonView.isChecked = isChecked
                Log.i(TAG, "CheckedState : ${buttonView.isChecked}")
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // 단일 데이터 삭제
    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyDataSetChanged()
    }

    // 단일 데이터 추가
    fun addItem(position: Int, newItem: DocumentSnapshot) {
        items.add(position, newItem)
        notifyDataSetChanged()
    }

    // 단일 데이터 추가
    fun addItem(newItem: DocumentSnapshot) {
        items.add(newItem)
        notifyDataSetChanged()
    }

    // 단일 조회
    fun getItem(position: Int): DocumentSnapshot {
        return items[position]
    }

    // 데이터 초기화
    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: LayoutProduct6Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(temp: DocumentSnapshot?) {
            binding.apply {
                item = temp?.toObject(ProductEntity::class.java)
            }
        }
    }
}