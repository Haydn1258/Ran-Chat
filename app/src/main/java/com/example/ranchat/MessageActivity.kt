package com.example.ranchat

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ranchat.dialogs.ChatDialogFragment
import com.example.ranchat.model.ChatUser
import com.example.ranchat.model.Comment
import com.example.ranchat.model.NotificationModel
import com.example.ranchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.squareup.okhttp.*
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.activity_drawer.view.*
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.dialog_chat_setting.view.*
import kotlinx.android.synthetic.main.item_message.view.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageActivity : AppCompatActivity() {
    var destinationUid:String? = null
    var uid:String? = null
    var chatRoom = false
    var destinationUser = User()
    var userUri:String? = null
    var pushToken:String? = null
    var editBoolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        messageActivity = this

        uid = FirebaseAuth.getInstance().currentUser!!.uid
        destinationUid = intent.getStringExtra("destinationUid")
        message_recyclerview.adapter = MessageRecyclerViewAdapter()
        message_recyclerview.layoutManager = LinearLayoutManager(this)


        message_imgNav.setOnClickListener {

            if (destinationUser.userUri!=null){
                Glide.with(this)
                    .load(destinationUser.userUri)
                    .override(100,100)
                    .centerCrop()
                    .circleCrop()
                    .into(drawer_imgv)
            }else{
                drawer_imgv.setImageResource(R.drawable.baseline_supervised_user_circle_black_48dp2)
                drawer_imgv.setColorFilter(
                    Color.parseColor("#A4FBB5"),
                    PorterDuff.Mode.SRC_IN
                )
            }
            drawer_txtvTitle.text = destinationUser.userNickname.toString()
            drawer_layout.openDrawer(drawer)
        }



        Log.d("Aa", destinationUid.toString())
        message_btn.setOnClickListener {

            if(!message_edt.text.toString().equals("")){
                message_btn.isEnabled = false
                if (chatRoom != false){
                    sendMessage()
                }else{
                    checkChatRoom()

                }
            }
        }

        message_edt.setOnKeyListener { view, i, keyEvent ->
            when(i){
                KeyEvent.KEYCODE_ENTER ->{
                    if(editBoolean){
                        editBoolean = false
                        if(!message_edt.text.toString().equals("")){
                            message_btn.isEnabled = false
                            if (chatRoom != false){
                                sendMessage()
                            }else{
                                checkChatRoom()

                            }
                        }
                    }
                }
                KeyEvent.KEYCODE_BACK ->{
                    finish()
                }
            }
            return@setOnKeyListener true
        }

        drawer.setOnTouchListener { view, motionEvent ->
            return@setOnTouchListener true
        }

        drawer_btnExit.setOnClickListener {
            //showDialog(it.context)
            ChatDialogFragment.newInstance(destinationUid).show(supportFragmentManager, "dialog")
        }

        message_imgButton.setOnClickListener {
            finish()
        }


    }

    /*fun showDialog(context:Context){
       val builder = AlertDialog.Builder(context)

        val layoutInterface = layoutInflater
        val view = layoutInterface.inflate(R.layout.dialog_chat_setting, null)
        view.dialogChatSetting_btnOK.setOnClickListener {

        }
        builder.show()
    }*/


    fun sendMessage(){
        val comment = Comment()
        comment.uid = uid
        comment.message = message_edt.text.toString()
        comment.timeStamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("chatRooms").document(uid!!).collection("chatUsers").document(destinationUid!!).collection("comments").add(comment)
        FirebaseFirestore.getInstance().collection("chatRooms").document(destinationUid!!).collection("chatUsers").document(uid!!).collection("comments").add(comment).addOnSuccessListener {
            var userUpdate:MutableMap<String,Any> = mutableMapOf("timeStamp" to System.currentTimeMillis(), "lastMessage" to comment.message!!)
            var destinationUpdate:MutableMap<String,Any> = mutableMapOf("timeStamp" to System.currentTimeMillis(), "lastMessage" to comment.message!!)
            FirebaseFirestore.getInstance().collection("chatRooms").document(uid!!).collection("chatUsers").document(destinationUid!!).update(userUpdate)
            FirebaseFirestore.getInstance().collection("chatRooms").document(destinationUid!!).collection("chatUsers").document(uid!!).update(destinationUpdate).addOnSuccessListener {
                sendGcm()
                message_edt.setText("")
                message_btn.isEnabled = true
                editBoolean = true
            }
        }


    }

    fun checkChatRoom(){
        FirebaseFirestore.getInstance().collection("chatRooms").document(uid!!).collection("chatUsers").whereEqualTo("uid",destinationUid).get().addOnSuccessListener {
            if (it.documents.isEmpty()){
                var chatUser = ChatUser()
                chatUser.uid = destinationUser.uid
                chatUser.timeStamp =  System.currentTimeMillis()
                chatUser.userNickname = destinationUser.userNickname
                FirebaseFirestore.getInstance().collection("chatRooms").document(uid!!).collection("chatUsers").document(destinationUid!!).set(chatUser).addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("user").whereEqualTo("uid",uid).get().addOnSuccessListener {
                        for(snapshot in it.documents){
                            var item = snapshot.toObject(User::class.java)
                            chatUser.userNickname = item!!.userNickname
                            userUri = item.userUri
                        }
                        chatUser.uid = uid
                        FirebaseFirestore.getInstance().collection("chatRooms").document(destinationUid!!).collection("chatUsers").document(uid!!).set(chatUser).addOnSuccessListener {
                            chatRoom = true
                            sendMessage()
                        }
                    }

                }
            }else{
                chatRoom = true
                sendMessage()
            }
        }
    }

    fun sendGcm(){
        val gson = Gson()
        var userName = FirebaseAuth.getInstance().currentUser!!.displayName
        val notificationModel = NotificationModel()
        notificationModel.to = pushToken
        notificationModel.notification.title = userName
        notificationModel.notification.text = message_edt.text.toString()
        notificationModel.data.title = userName
        notificationModel.data.text = message_edt.text.toString()

        val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel))

        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .addHeader("Authorization","key=AIzaSyC3bEhsQTu4kyzFrbt0n6d5_Lp1qMXrI08")
            .url("https://fcm.googleapis.com/fcm/send")
            .post(requestBody)
            .build()

        val okHttpClient = OkHttpClient()
        okHttpClient.newCall(request).enqueue(object :Callback{
            override fun onFailure(request: Request?, e: IOException?) {
                Log.d("callback","실패")
            }

            override fun onResponse(response: Response?) {

            }

        })

    }

    inner class MessageRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comment: ArrayList<Comment> = arrayListOf()

        init {
            //contentDTOs.clear()
            //contentUidList.clear()
            FirebaseFirestore.getInstance().collection("user").whereEqualTo("uid",destinationUid).get().addOnSuccessListener {
                for(snapshot in it.documents){
                    var item = snapshot.toObject(User::class.java)
                    destinationUser = item!!
                    pushToken = item.pushToken
                }
                getMessageList()
            }



        }

        fun getMessageList(){
            FirebaseFirestore.getInstance().collection("chatRooms").document(uid!!).collection("chatUsers").document(destinationUid!!).collection("comments").orderBy("timeStamp")
                .addSnapshotListener {
                        querySnapshot, firebaseFirestoreException ->
                    comment.clear()
                    if(querySnapshot==null){
                        Log.d("snapshot","null")
                    }else{
                        Log.d("aas",querySnapshot.toString())
                        for(snapshot in querySnapshot.documents){
                            var item = snapshot.toObject(Comment::class.java)
                            comment.add(item!!)
                        }
                    }
                    message_txtvName.text = destinationUser.userNickname

                    Log.d("aaaa",comment.toString())
                    notifyDataSetChanged()

                    message_recyclerview.scrollToPosition(comment.size-1)

                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            if(Build.VERSION.SDK_INT >= 21) {
                view.messageItem_imgvProfile.background = ShapeDrawable(OvalShape())
                view.messageItem_imgvProfile.clipToOutline = true
            }
            return CustomViewHoler(view)
        }

        inner class CustomViewHoler(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comment.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var messageViewholder = (holder as CustomViewHoler).itemView

            //내가보낸 메세지
            if (comment[position].uid.equals(uid)){
                messageViewholder.messageItem_txtvMessage.text = comment[position].message.toString()
                messageViewholder.messageItem_txtvMessage.setBackgroundResource(R.drawable.rightbubble)
                messageViewholder.messageItem_linearlayoutDestination.visibility = View.INVISIBLE
                messageViewholder.messageItem_txtvMessage.setTextSize(25.toFloat())
                messageViewholder.messageItem_linearlayoutMain.gravity = Gravity.END

            //상대방이 보낸 메세지
            }else{
                if (destinationUser.userUri != null) {
                    Glide.with(holder.itemView.context).load(destinationUser.userUri)
                        .override(50,50)
                        .centerCrop()
                        .into(messageViewholder.messageItem_imgvProfile)
                }else {
                    messageViewholder.messageItem_imgvProfile.setImageResource(R.drawable.baseline_supervised_user_circle_black_48dp2)
                    messageViewholder.messageItem_imgvProfile.setColorFilter(
                        Color.parseColor("#A4FBB5"),
                        PorterDuff.Mode.SRC_IN
                    )
                }
                messageViewholder.messageItem_txtvName.setText(destinationUser.userNickname)
                messageViewholder.messageItem_linearlayoutDestination.visibility = View.VISIBLE
                messageViewholder.messageItem_txtvMessage.setBackgroundResource(R.drawable.leftbubble)
                messageViewholder.messageItem_txtvMessage.setText(comment[position].message)
                messageViewholder.messageItem_txtvMessage.setTextSize(25.toFloat())
                messageViewholder.messageItem_linearlayoutMain.gravity = Gravity.START
            }

            messageViewholder.messageItem_txtvTimeStamp.text = dateFormat(comment[position].timeStamp!!).toString()

        }

    }


    companion object{
        var messageActivity:Activity? = null
        fun dateFormat(timeStamp:Long):String{
            var dateString:String = ""
            var simpleHour = SimpleDateFormat("HH")
            var simpleMinute = SimpleDateFormat("mm")
            var unixTime = timeStamp
            var date = Date(unixTime)
            simpleHour.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            simpleMinute.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            var time = simpleHour.format(date)
            var minute = simpleMinute.format(date)

            if(time.toInt()>12){
                if (time.toInt()-12==0){
                    dateString = "오후 12:"+ minute
                }else{
                    dateString = "오후 "+(time.toInt()-12).toString()+":"+minute
                }
            }
            else{
                if (time.toInt()==0){
                    dateString = "오전 12:"+minute
                }
                else{
                    dateString = "오전 "+time+":"+minute
                }
            }
            return dateString
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}

