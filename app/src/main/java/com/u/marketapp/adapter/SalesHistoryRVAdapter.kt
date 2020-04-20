package com.u.marketapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.u.marketapp.R
import com.u.marketapp.databinding.LayoutProduct2Binding
import com.u.marketapp.entity.ProductEntity
import kotlinx.android.synthetic.main.layout_product2.view.*

class SalesHistoryRVAdapter : RecyclerView.Adapter<SalesHistoryRVAdapter.ViewHolder>() {

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

    interface TradeClickListener { fun onClick(view: View, position: Int) }
    private lateinit var tradeClickListener: TradeClickListener
    fun setTradeClickListener(tradeClickListener: TradeClickListener) {
        this.tradeClickListener = tradeClickListener
    }

    interface StateClickListener { fun onClick(view: View, position: Int) }
    private lateinit var stateClickListener: StateClickListener
    fun setStateClickListener(stateClickListener: StateClickListener) {
        this.stateClickListener = stateClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.layout_product2, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            bind(item)
            itemView.tag = item
            itemView.setOnClickListener { itemClickListener.onClick(it, position) }
            itemView.image_view_more.setOnClickListener { moreClickListener.onClick(it, position) }
            itemView.text_view_trade_change.setOnClickListener { tradeClickListener.onClick(it, position) }
            itemView.text_view_trade_complete.setOnClickListener { stateClickListener.onClick(it, position) }
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

    class ViewHolder(private val binding: LayoutProduct2Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(temp: DocumentSnapshot) {
            binding.apply {
                item = temp.toObject(ProductEntity::class.java)
            }
        }

    }
}