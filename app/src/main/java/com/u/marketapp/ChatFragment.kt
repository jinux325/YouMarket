package com.u.marketapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.u.marketapp.adapter.ChatAdapter
import com.u.marketapp.vo.ChatRoomVO


class ChatFragment : Fragment() {

    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        (activity as AppCompatActivity).supportActionBar?.hide()
        //(activity as AppCompatActivity).setSupportActionBar(setting_toolbar)

        val chat_recyclerView = view.findViewById(R.id.chat_recyclerView) as RecyclerView
        var mainList = mutableListOf<ChatRoomVO>(ChatRoomVO("1","1", "","1","1","1","1","1"),ChatRoomVO("1","1", "","1","1","1","1","1"),ChatRoomVO("1","1", "","1","1","1","1","1"))
        chat_recyclerView.layoutManager = LinearLayoutManager(getContext())
        chat_recyclerView.adapter = ChatAdapter(getContext(), mainList)





        
        return view
    }


}
