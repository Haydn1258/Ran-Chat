package com.example.ranchat

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.ranchat.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {
    var destinationUid:String? = null
    var uid:String? = null
    var chatRoomUid:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        uid = FirebaseAuth.getInstance().currentUser!!.uid

        destinationUid = intent.getStringExtra("destinationUid")
        Log.d("Aa", destinationUid.toString())

        checkChatRoome()

        message_btn.setOnClickListener {
            var chat = Chat()
            chat.uid = FirebaseAuth.getInstance().uid
            chat.destinationUid = destinationUid
            val chatRoomId = chatRoomUid

            if (chatRoomUid == null){
                message_btn.isEnabled = false
                FirebaseFirestore.getInstance().collection("chatRooms").document().set(chat).addOnSuccessListener {
                    checkChatRoome()
                    val comment = Chat.Comment
                    comment.uid = uid
                    comment.message = message_edt.text.toString()
                    FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomId!!).collection("comments").add(comment)
                    message_btn.isEnabled = true
                }
            }else{
                val comment = Chat.Comment
                comment.uid = uid
                comment.message = message_edt.text.toString()
                FirebaseFirestore.getInstance().collection("chatRooms").document(chatRoomId!!).collection("comments").add(comment)
            }



        }

    }

    fun checkChatRoome(){
        FirebaseFirestore.getInstance().collection("chatRooms").whereEqualTo("uid", uid).get().
            addOnSuccessListener {
                if(it==null){
                    Log.d("dd", "aa")
                }else{
                    Log.d("aaa", "aa")
                    for (snapshot in it.documents) {
                        var item = snapshot.toObject(Chat::class.java)
                        Log.d("aaaa", item.toString())
                        if (item?.destinationUid ==destinationUid) {
                            chatRoomUid = snapshot.id
                            Log.d("aa", snapshot.id.toString())
                        }
                    }

                }

        }
    }
}
