package com.example.ranchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ranchat.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.item_message.view.*

class MessageActivity : AppCompatActivity() {
    var destinationUid:String? = null
    var uid:String? = null
    var chatRoomUid:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        destinationUid = intent.getStringExtra("destinationUid")
        chatRoomUid = uid+destinationUid
        message_recyclerview.adapter = MessageRecyclerViewAdapter()
        message_recyclerview.layoutManager = LinearLayoutManager(this)

        Log.d("Aa", destinationUid.toString())

        message_btn.setOnClickListener {
           /* var chat = Chat()
            chat.uid = FirebaseAuth.getInstance().uid
            chat.destinationUid = destinationUid
            val chatRoomId = chatRoomUid*/
            if(!message_edt.text.toString().equals("")){
                message_btn.isEnabled = false
                val comment = Comment()
                comment.uid = uid
                comment.message = message_edt.text.toString()
                comment.timeStamp = System.currentTimeMillis()
                FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomUid!!).collection("comments").add(comment).addOnSuccessListener {
                    message_edt.setText("")
                    message_btn.isEnabled = true
                }

            }




        }

    }

    inner class MessageRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comment: ArrayList<Comment> = arrayListOf()
        init {
            //contentDTOs.clear()
            //contentUidList.clear()
            Log.d("aaaaf",chatRoomUid)
            FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomUid!!).collection("comments")
                .addSnapshotListener {
                    querySnapshot, firebaseFirestoreException ->
                //comment.clear()
                if(querySnapshot==null){
                    Log.d("snapshot","null")
                }else{
                    Log.d("aas",querySnapshot.toString())
                    for(snapshot in querySnapshot.documents){
                        var item = snapshot.toObject(Comment::class.java)
                        comment.add(item!!)
                    }
                }
                Log.d("aaaa",comment.toString())
                notifyDataSetChanged()

            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return CustomViewHoler(view)
        }

        inner class CustomViewHoler(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comment.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHoler).itemView
            viewholder.messageItem_txtvMessage.text = comment[position].message.toString()

        }

    }


}

