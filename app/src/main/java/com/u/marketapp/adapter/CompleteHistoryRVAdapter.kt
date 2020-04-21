package com.u.marketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.u.marketapp.R
import com.u.marketapp.databinding.LayoutProduct3Binding
import com.u.marketapp.entity.ProductEntity
import kotlinx.android.synthetic.main.layout_product2.view.image_view_more
import kotlinx.android.synthetic.main.layout_product3.view.*

class CompleteHistoryRVAdapter : RecyclerView.Adapter<CompleteHistoryRVAdapter.ViewHolder>() {

    private val items : ArrayList<DocumentSnapshot> = ArrayList()

    interface ItemClickListener { fun onClick(view: View, position: Int) }
    private lateinit var itemClickListener: ItemClickListener
    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    interface MoreClickListener { fun onClick(view: View, position: Int) }
    private lateinit var moreClickListener: MoreClickListener
    fun setMoreClickListener(moreClickListener: MoreClickListener) {
        this.moreClickListener = moreClickListener
    }

    interface ReviewClickListener { fun onClick(view: View, position: Int) }
    private lateinit var reviewClickListener: ReviewClickListener
    fun setReviewClickListener(reviewClickListener: ReviewClickListener) {
        this.reviewClickListener = reviewClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.layout_product3, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            bind(item)
            itemView.tag = item
            itemView.setOnClickListener { itemClickListener.onClick(it, position) }
            itemView.image_view_more.setOnClickListener { moreClickListener.onClick(it, position) }
            itemView.text_view_review.setOnClickListener { reviewClickListener.onClick(it, position) }
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

    class ViewHolder(private val binding: LayoutProduct3Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(temp: DocumentSnapshot) {
            binding.apply {
                item = temp.toObject(ProductEntity::class.java)
            }
        }

    }
}