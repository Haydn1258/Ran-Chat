package com.example.ranchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.ranchat.chat.ChatViewFragment
import com.example.ranchat.friend.FriendViewFragment
import com.example.ranchat.setting.SettingViewFragment
import com.example.ranchat.square.SquareViewFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
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

            R.id.action_friend->{
                if(lastSelect.equals("friend")){
                    friendStack.clear()
                    manager.beginTransaction().replace(R.id.main_frameContent, FriendViewFragment()).commit()
                }else{
                    stackPush(lastSelect)
                    if(friendStack.empty()){
                        manager.beginTransaction().replace(R.id.main_frameContent, FriendViewFragment()).commit()
                        lastSelect = "friend"
                    }else{
                        val lastFragmentStack = friendStack.pop()
                        manager.beginTransaction().replace(R.id.main_frameContent, lastFragmentStack).commit()
                        lastSelect = "friend"
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
                if(lastSelect.equals("setting")){
                    settingStack.clear()
                    manager.beginTransaction().replace(R.id.main_frameContent, SettingViewFragment()).commit()
                }else{
                    stackPush(lastSelect)
                    if(settingStack.empty()){
                        manager.beginTransaction().replace(R.id.main_frameContent, SettingViewFragment()).commit()
                        lastSelect = "setting"
                    }else{
                        val lastFragmentStack = settingStack.pop()
                        manager.beginTransaction().replace(R.id.main_frameContent, lastFragmentStack).commit()
                        lastSelect = "setting"
                    }
                }
                return true
            }
        }
        return false

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_bottomnavView.setOnNavigationItemSelectedListener(this)
        main_bottomnavView.selectedItemId = R.id.action_chat
    }

    fun stackPush(push:String){
        when(push){
            "chat" ->{
                chatStack.push(supportFragmentManager.findFragmentById(R.id.main_frameContent))
            }
            "friend" ->{
                friendStack.push(supportFragmentManager.findFragmentById(R.id.main_frameContent))
            }
            "square" ->{
                squareStack.push(supportFragmentManager.findFragmentById(R.id.main_frameContent))
            }
            "setting" ->{
                settingStack.push(supportFragmentManager.findFragmentById(R.id.main_frameContent))
            }
        }
    }

    companion object{
        var chatStack = Stack<Fragment>()
        var friendStack = Stack<Fragment>()
        var squareStack = Stack<Fragment>()
        var settingStack = Stack<Fragment>()
        var lastSelect = ""

    }

}
