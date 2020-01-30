package com.example.ranchat

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ranchat.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_message.*

class MessageActivity : AppCompatActivity() {
    var destinationUid:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        destinationUid = intent.getStringExtra("destinationUid")

        message_btn.setOnClickListener {
            var chat = Chat()
            chat.uid = FirebaseAuth.getInstance().currentUser?.uid
            chat.destinationUid = destinationUid

            FirebaseFirestore.getInstance().collection("chatrooms").document().set(chat)

        }

    }
}
