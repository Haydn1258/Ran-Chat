package com.example.ranchat.square

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.ranchat.MainActivity
import com.example.ranchat.R
import com.example.ranchat.model.User
import com.example.ranchat.model.Writing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_writing.*

class WritingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_writing)

        writing_imgButtonBack.setOnClickListener {
            finish()
        }

        writing_btnComplete.setOnClickListener {
            writing_btnComplete.isEnabled =false
            if(writing_edt.text.toString().replace(" ", "").equals("")){
                Toast.makeText(applicationContext, "메세지를 다시 입력해주세요", Toast.LENGTH_SHORT).show()
                writing_btnComplete.isEnabled = true
            }else{
                writing()
            }
        }
    }

    fun writing(){
        FirebaseFirestore.getInstance().collection("user").document(FirebaseAuth.getInstance().currentUser?.uid!!).
            get().addOnSuccessListener {
            if(it == null){
                writing_btnComplete.isEnabled = true
            }else{
                var user = it.toObject(User::class.java)
                var writing = Writing()
                writing.uid = user?.uid
                writing.userNickname = user?.userNickname
                writing.userUri = user?.userUri
                writing.message = writing_edt.text.toString()
                writing.timeStamp =System.currentTimeMillis()
                FirebaseFirestore.getInstance().collection("list").document(writing.timeStamp.toString()+FirebaseAuth.getInstance().currentUser?.uid)
                    .set(writing).addOnSuccessListener {
                        writing_btnComplete.isEnabled = true
                        finish()
                }
            }
        }
    }


}
