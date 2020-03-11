package com.u.marketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.u.marketapp.R
import com.u.marketapp.databinding.LayoutProductBinding
import com.u.marketapp.entity.ProductEntity

class ProductRVAdapter : RecyclerView.Adapter<ProductRVAdapter.ViewHolder>() {

    private val items : ArrayList<ProductEntity> = ArrayList()

    interface ItemClick { fun onClick(view: View, position: Int) }
    var itemClick: ItemClick? = null

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
            if (itemClick != null) {
                itemView.setOnClickListener {
                        v -> itemClick?.onClick(v, position)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // 리스트 데이터 추가
    fun addList(newList: List<ProductEntity>) {
        items.addAll(newList)
        notifyDataSetChanged()
    }

    // 단일 데이터 추가
    fun addItem(newItem: ProductEntity) {
        items.add(newItem)
        notifyDataSetChanged()
    }

    // 단일 데이터 제거
    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyDataSetChanged()
    }

    // 단일 데이터 수정
    fun updateItem(position: Int, newItem: ProductEntity) {
        items[position] = newItem
        notifyDataSetChanged()
    }

    // 단일 조회
    fun getItem(position: Int): ProductEntity {
        return items[position]
    }

    // 데이터 초기화
    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: LayoutProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(temp: ProductEntity) {
            binding.apply {
                item = temp
            }
        }

    }
}