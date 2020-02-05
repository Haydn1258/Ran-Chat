package com.example.ranchat

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ranchat.chat.ChatViewFragment
import com.example.ranchat.model.User
import com.example.ranchat.setting.ProfileActivity
import com.example.ranchat.setting.SettingViewFragment
import com.example.ranchat.square.SquareViewFragment
import com.example.ranchat.users.UsersViewFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    val manager = supportFragmentManager


    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId){
            R.id.action_chat->{
                if(lastSelect.equals("chat")){
                    chatStack.clear()
                    manager.beginTransaction().replace(R.id.main_frameContent, ChatViewFragment()).commit()
                }else{
                    stackPush(lastSelect)
                    if(chatStack.empty()){
                        manager.beginTransaction().replace(R.id.main_frameContent, ChatViewFragment()).commit()
                        lastSelect = "chat"
                    }else{
                        val lastFragmentStack = chatStack.pop()
                        manager.beginTransaction().replace(R.id.main_frameContent, lastFragmentStack).commit()
                        lastSelect = "chat"
                    }
                }
                return true

            }

            R.id.action_users->{
                if(lastSelect.equals("users")){
                    usersStack.clear()
                    manager.beginTransaction().replace(R.id.main_frameContent, UsersViewFragment()).commit()
                }else{
                    stackPush(lastSelect)
                    if(usersStack.empty()){
                        manager.beginTransaction().replace(R.id.main_frameContent, UsersViewFragment()).commit()
                        lastSelect = "users"
                    }else{
                        val lastFragmentStack = usersStack.pop()
                        manager.beginTransaction().replace(R.id.main_frameContent, lastFragmentStack).commit()
                        lastSelect = "users"
                    }
                }
                return true

            }

            R.id.action_square ->{
                if(lastSelect.equals("square")){
                    squareStack.clear()
                    manager.beginTransaction().replace(R.id.main_frameContent, SquareViewFragment()).commit()
                }else{
                    stackPush(lastSelect)
                    if(squareStack.empty()){
                        manager.beginTransaction().replace(R.id.main_frameContent, SquareViewFragment()).commit()
                        lastSelect = "square"
                    }else{
                        val lastFragmentStack = squareStack.pop()
                        manager.beginTransaction().replace(R.id.main_frameContent, lastFragmentStack).commit()
                        lastSelect = "square"
                    }
                }
                return true
            }

            R.id.action_setting ->{
                manager.beginTransaction().replace(R.id.main_frameContent, SettingViewFragment()).commit()
                lastSelect = "setting"
                /*if(lastSelect.equals("setting")){
                    settingStack.clear()

                }else{
                    stackPush(lastSelect)
                    if(settingStack.empty()){
                        manager.beginTransaction().replace(R.id.main_frameContent, SettingViewFragment()).commit()

                    }else{
                        val lastFragmentStack = settingStack.pop()
                        Log.d("aafff", lastFragmentStack.toString())
                        manager.beginTransaction().replace(R.id.main_frameContent, lastFragmentStack).commit()
                        lastSelect = "setting"
                    }
                }*/
                return true
            }
        }
        return false

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_bottomnavView.setOnNavigationItemSelectedListener(this)
        main_bottomnavView.selectedItemId = R.id.action_users
        settingBoolean = true


        passPushTokenToServer()


    }

    fun stackPush(push:String){
        when(push){
            "chat" ->{
                chatStack.push(supportFragmentManager.findFragmentById(R.id.main_frameContent))
            }
            "users" ->{
                usersStack.push(supportFragmentManager.findFragmentById(R.id.main_frameContent))
            }
            "square" ->{
                squareStack.push(supportFragmentManager.findFragmentById(R.id.main_frameContent))
            }
            "setting" ->{
               // settingStack.push(supportFragmentManager.findFragmentById(R.id.main_frameContent))
            }
        }
    }

    companion object{

        var chatStack = Stack<Fragment>()
        var usersStack = Stack<Fragment>()
        var squareStack = Stack<Fragment>()
        //var settingStack = Stack<Fragment>()
        var settingBoolean = true
        var lastSelect = ""


    }
    fun passPushTokenToServer(){
        var uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("aa","aaaaaaaa")

        Log.d("aaff", FirebaseInstanceId.getInstance().instanceId.toString())
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("aaff", "aaa")
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token
                var map:MutableMap<String,Any> = mutableMapOf("pushToken" to token!!)
                FirebaseFirestore.getInstance().collection("user").document(uid!!).update(map)
                Log.d("aaff", "seee")
            })

        var firestore : FirebaseFirestore? = FirebaseFirestore.getInstance()


        firestore?.collection("user")?.document(FirebaseAuth.getInstance().currentUser?.uid!!)?.get()?.
            addOnCompleteListener{
                if(it==null){
                    Log.d("snapshot","null")
                }else{
                    var user = it.result?.toObject(User::class.java)
                    user?.timeStamp = System.currentTimeMillis()
                    firestore.collection("currentUser").document(FirebaseAuth.getInstance().currentUser?.uid!!).get().
                        addOnCompleteListener{
                            var currentUser = it.result?.toObject(User::class.java)
                            if(it==null){
                                Log.d("snapshot","null")
                            }else{
                                if (currentUser?.uid!=null){
                                    firestore.collection("currentUser").document(user?.uid!!).delete()
                                    firestore.collection("currentUser").document(user?.uid!!).set(user)

                                }else{
                                    firestore.collection("currentUser").document(user?.uid!!).set(user)
                                }

                            }
                        }
                }
            }




    }


}
