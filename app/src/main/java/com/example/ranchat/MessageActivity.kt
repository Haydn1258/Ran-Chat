package com.example.ranchat

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ranchat.model.ChatUser
import com.example.ranchat.model.Comment
import com.example.ranchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.item_message.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MessageActivity : AppCompatActivity() {
    var destinationUid:String? = null
    var uid:String? = null
    var chatRoom = false
    var user = User()
    var destinationComment = Comment()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        uid = FirebaseAuth.getInstance().currentUser!!.uid
        destinationUid = intent.getStringExtra("destinationUid")
        message_recyclerview.adapter = MessageRecyclerViewAdapter()
        message_recyclerview.layoutManager = LinearLayoutManager(this)

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

        message_imgButton.setOnClickListener {
            finish()
        }

    }

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
                message_edt.setText("")
                message_btn.isEnabled = true
            }
        }


    }

    fun checkChatRoom(){
        FirebaseFirestore.getInstance().collection("chatRooms").document(uid!!).collection("chatUsers").whereEqualTo("uid",destinationUid).get().addOnSuccessListener {
            if (it.documents.isEmpty()){
                var chatUser = ChatUser()
                chatUser.uid = user.uid
                chatUser.timeStamp =  System.currentTimeMillis()
                chatUser.userNickname = user.userNickname
                FirebaseFirestore.getInstance().collection("chatRooms").document(uid!!).collection("chatUsers").document(destinationUid!!).set(chatUser).addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("user").whereEqualTo("uid",uid).get().addOnSuccessListener {
                        for(snapshot in it.documents){
                            var item = snapshot.toObject(User::class.java)
                            chatUser.userNickname = item!!.userNickname
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
            }
        }
    }

    inner class MessageRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comment: ArrayList<Comment> = arrayListOf()

        init {
            //contentDTOs.clear()
            //contentUidList.clear()
            FirebaseFirestore.getInstance().collection("user").whereEqualTo("uid",destinationUid).get().addOnSuccessListener {
                for(snapshot in it.documents){
                    var item = snapshot.toObject(User::class.java)
                    user = item!!
                }
                message_txtvName.text = user.userNickname
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
                if (user.userUri != null) {
                    Glide.with(holder.itemView.context).load(user.userUri)
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
                messageViewholder.messageItem_txtvName.setText(user.userNickname)
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
}

