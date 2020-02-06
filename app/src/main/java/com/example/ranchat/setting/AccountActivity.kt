package com.example.ranchat.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.ranchat.LoginActivity
import com.example.ranchat.MainActivity
import com.example.ranchat.R
import com.example.ranchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_account.*
import kotlin.math.sign

class AccountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        account_imgButton.setOnClickListener {
            finish()
        }

        account_btnSignOut.setOnClickListener {
            signOut()
        }
    }


    fun signOut(){
        FirebaseFirestore.getInstance().collection("currentUser").document(FirebaseAuth.getInstance().currentUser?.uid!!).get().
            addOnCompleteListener{
                var currentUser = it.result?.toObject(User::class.java)
                if(it==null){
                    Log.d("snapshot","null")
                }else{
                    FirebaseFirestore.getInstance().collection("currentUser").document(FirebaseAuth.getInstance()?.uid!!).delete().addOnSuccessListener {
                        MainActivity.settingBoolean = false
                        var userToken:MutableMap<String,Any> = mutableMapOf("pushToken" to "")
                        FirebaseFirestore.getInstance().collection("user").document(FirebaseAuth.getInstance()?.uid!!).update(userToken)
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        FirebaseAuth.getInstance().signOut()
                    }
                }
            }
    }
}
