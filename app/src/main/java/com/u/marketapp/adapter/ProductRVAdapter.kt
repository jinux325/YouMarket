package com.u.marketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.u.marketapp.R
import com.u.marketapp.databinding.LayoutProductBinding
import com.u.marketapp.entity.ProductEntity

class ProductRVAdapter : RecyclerView.Adapter<ProductRVAdapter.ViewHolder>() {

    private val items : ArrayList<DocumentSnapshot> = ArrayList()

    interface ItemClickListener { fun onClick(view: View, position: Int) }
    private lateinit var itemClickListener: ItemClickListener

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.layout_product, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            bind(item)
            itemView.tag = item
            itemView.setOnClickListener { itemClickListener.onClick(it, position) }
        }
    }

    override fun getItemCount(): Int = items.size

    fun addAllItem(newList: List<DocumentSnapshot>) {
        items.addAll(newList)
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

    class ViewHolder(private val binding: LayoutProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(temp: DocumentSnapshot) {
            binding.apply {
                item = temp.toObject(ProductEntity::class.java)
            }
        }

    }
}