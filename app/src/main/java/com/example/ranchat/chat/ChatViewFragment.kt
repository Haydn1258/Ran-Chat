package com.example.ranchat.chat

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ranchat.MessageActivity
import com.example.ranchat.R
import com.example.ranchat.model.ChatUser
import com.example.ranchat.model.Comment
import com.example.ranchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.card_chat.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.item_chat.view.*
import kotlinx.android.synthetic.main.item_message.view.*
import java.lang.reflect.Array

class ChatViewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_chat, container, false)
        view.chat_recyclerViewChatList.adapter = ChatListRecyclerViewAdapter()
        view.chat_recyclerViewChatList.layoutManager = LinearLayoutManager(activity)


        return view
    }
    inner class ChatListRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var chatUsers: ArrayList<ChatUser> = arrayListOf()
        var uid:String? = FirebaseAuth.getInstance().currentUser?.uid
        var destinationUsers:ArrayList<String> = arrayListOf()
        init {
            FirebaseFirestore.getInstance().collection("chatRooms").document(uid!!).collection("chatUsers")
                .addSnapshotListener {
                        querySnapshot, firebaseFirestoreException ->
                    chatUsers.clear()
                    if(querySnapshot==null){
                        Log.d("snapshot","null")
                    }else{
                        Log.d("aas",querySnapshot.toString())
                        for(snapshot in querySnapshot.documents){
                            var item = snapshot.toObject(ChatUser::class.java)
                            chatUsers.add(item!!)
                            destinationUsers.add(item.uid!!)
                        }
                    }
                    Log.d("aaaa",chatUsers.toString())
                    notifyDataSetChanged()
                }


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.card_chat, parent, false)
            return CustomViewHoler(view)
        }

        override fun getItemCount(): Int {
            return chatUsers.size
        }
        inner class CustomViewHoler(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var chatViewholder = (holder as CustomViewHoler).itemView
            var chatUser = User()
            FirebaseFirestore.getInstance().collection("user").whereEqualTo("uid",chatUsers[position].uid).get().addOnSuccessListener {
                for(snapshot in it.documents){
                    chatUser = snapshot.toObject(User::class.java)!!
                }
                if (chatUser.userUri != null) {
                    Glide.with(holder.itemView.context).load(chatUser.userUri)
                        .into(chatViewholder.cardChat_imgv)
                }else {
                    chatViewholder.cardChat_imgv.setColorFilter(
                        Color.parseColor("#A4FBB5"),
                        PorterDuff.Mode.SRC_IN
                    )
                }
            }
            chatViewholder.cardChat_txtvTitle.text = chatUsers[position].userNickname
            chatViewholder.cardChat_txtvMessage.text = chatUsers[position].lastMessage
            chatViewholder.cardChat_txtvTimeStamp.text = MessageActivity.dateFormat(chatUsers[position].timeStamp!!)

            holder.itemView.setOnClickListener {
                val intent = Intent(activity, MessageActivity::class.java)
                intent.putExtra("destinationUid", destinationUsers[position])
                startActivity(intent)
            }


        }

    }


}