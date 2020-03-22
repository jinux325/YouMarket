package com.u.marketapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.u.marketapp.R
import com.u.marketapp.databinding.LayoutReplyBinding
import com.u.marketapp.entity.CommentEntity
import com.u.marketapp.entity.UserEntity
import kotlinx.android.synthetic.main.layout_reply.view.*

class CommentRVAdapter(val context: Context) : RecyclerView.Adapter<CommentRVAdapter.ViewHolder>() {

    companion object {
        private val TAG = CommentRVAdapter::class.java.simpleName
    }

    private val items : ArrayList<DocumentSnapshot> = ArrayList()

    interface MoreClickListener { fun onClick(view: View, position: Int) }
    interface ReplyClickListener { fun onClick(view: View, position: Int) }

    private lateinit var moreClickListener: MoreClickListener
    private lateinit var replyClickListener: ReplyClickListener

    fun setMoreClickListener(moreClickListener: MoreClickListener) {
        this.moreClickListener = moreClickListener
    }

    fun setReplyClickListener(replyClickListener: ReplyClickListener) {
        this.replyClickListener = replyClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.layout_reply, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i(TAG, "View Position : $position")
        val item = items[position]
        Log.i(TAG, item.toObject(CommentEntity::class.java).toString())
        holder.apply {
            getUserInfo(holder, position, item)
        }
    }

    override fun getItemCount(): Int = items.size

    // 유저 정보 조회
    private fun getUserInfo(holder: ViewHolder, position: Int, item: DocumentSnapshot) {
        val uid = item.toObject(CommentEntity::class.java)!!.user
        val db = FirebaseFirestore.getInstance()
        db.collection(context.resources.getString(R.string.db_user)).document(uid).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userItem = task.result!!.toObject(UserEntity::class.java)!!
                    onBind(holder, position, item, userItem)
                }
            }
    }

    private fun onBind(holder: ViewHolder, position: Int, item: DocumentSnapshot, userItem: UserEntity) {
        holder.apply {
            bind(item, userItem)
            itemView.tag = item
            itemView.text_view_add_reply.setOnClickListener { replyClickListener.onClick(it, position) }
            itemView.image_view_more.setOnClickListener { moreClickListener.onClick(it, position) }
        }
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

    // 단일 제거
    fun removeItem(documentSnapshot: DocumentSnapshot) {
        items.remove(documentSnapshot)
        notifyDataSetChanged()
    }

    // 초기화
    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: LayoutReplyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DocumentSnapshot, userItem: UserEntity) {
            binding.apply {
                comment = item.toObject(CommentEntity::class.java)
                user = userItem
            }
        }
    }

}