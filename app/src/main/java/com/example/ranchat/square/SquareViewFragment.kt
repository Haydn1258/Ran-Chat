package com.example.ranchat.square

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ranchat.MainActivity.Companion.getDiffTimeText
import com.example.ranchat.MessageActivity
import com.example.ranchat.R
import com.example.ranchat.SignUpActivity
import com.example.ranchat.chat.ChatViewFragment
import com.example.ranchat.model.User
import com.example.ranchat.model.Writing
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.card_list.view.*
import kotlinx.android.synthetic.main.card_user.*
import kotlinx.android.synthetic.main.card_user.view.*
import kotlinx.android.synthetic.main.dialog_chat_setting.view.*
import kotlinx.android.synthetic.main.fragment_square.view.*
import java.text.SimpleDateFormat
import java.util.*

class SquareViewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_square, container, false)
        view.square_recyclerView.adapter = SqueareRecyclerViewAdapter()
        view.square_recyclerView.layoutManager = LinearLayoutManager(activity)

        view.square_imgButton.setOnClickListener {
            val intent = Intent(activity, WritingActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    inner class SqueareRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var uid:String? = FirebaseAuth.getInstance().currentUser?.uid
        var writingList: ArrayList<Writing> = arrayListOf()

        init {
            FirebaseFirestore.getInstance().collection("list").orderBy("timeStamp",  Query.Direction.DESCENDING)
                .get().addOnSuccessListener {
                    for(snapshot in it.documents) {
                        var item = snapshot.toObject(Writing::class.java)
                        writingList.add(item!!)
                    }
                    notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.card_list, parent, false)

            return CustomViewHoler(view)
        }

        override fun getItemCount(): Int {
            return writingList.size
        }
        inner class CustomViewHoler(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var squareViewholder = (holder as CustomViewHoler).itemView

            if(writingList[position].uid == FirebaseAuth.getInstance().uid){
                squareViewholder.cardList_imgButtonDelete.visibility = View.VISIBLE
                squareViewholder.cardList_imgButtonChat.visibility = View.GONE
            }else{
                squareViewholder.cardList_imgButtonDelete.visibility = View.GONE
                squareViewholder.cardList_imgButtonChat.visibility = View.VISIBLE
            }

            if (writingList[position].userUri != null) {
                squareViewholder.cardList_imgv.setColorFilter(
                    Color.parseColor("#A4FBB5"),
                    PorterDuff.Mode.DST
                )
                Glide.with(holder.itemView.context).load(writingList[position].userUri)
                    .centerCrop()
                    .circleCrop()
                    .into(squareViewholder.cardList_imgv)
            }else {
                squareViewholder.cardList_imgv.setColorFilter(
                    Color.parseColor("#A4FBB5"),
                    PorterDuff.Mode.SRC_IN
                )
            }
            squareViewholder.cardList_txtvTitle.setText(writingList[position].userNickname)
            squareViewholder.cardList_txtvMessage.setText(writingList[position].message)
            squareViewholder.cardList_txtvTimeStamp.setText(getDiffTimeText(writingList[position].timeStamp!!))

            squareViewholder.cardList_imgButtonDelete.setOnClickListener {
                showDialog(it.context, writingList[position].uid!!, writingList[position].timeStamp.toString())
            }
            squareViewholder.cardList_imgButtonChat.setOnClickListener {
                val intent = Intent(activity, MessageActivity::class.java)
                intent.putExtra("destinationUid", writingList[position].uid)
                startActivity(intent)
            }

        }

    }


    fun showDialog(context: Context, uid:String, timeStmap:String){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()

        val layoutInterface = layoutInflater
        val view = layoutInterface.inflate(R.layout.dialog_chat_setting, null)
        view.dialogChatSetting_txtvTitle.setText("글삭제")
        view.dialogChatSetting_txtvGuide.setText("글을 삭제하시겠습니까?")
        view.dialogChatSetting_btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        view.dialogChatSetting_btnOK.setOnClickListener {
            FirebaseFirestore.getInstance().collection("list").document(timeStmap+uid).delete()
            dialog.dismiss()
        }
        dialog.setView(view)
        dialog.show()
    }



}