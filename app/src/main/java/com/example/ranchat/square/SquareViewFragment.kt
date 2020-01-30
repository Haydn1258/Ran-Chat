package com.example.ranchat.square

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ranchat.MessageActivity
import com.example.ranchat.R
import com.example.ranchat.SignUpActivity
import com.example.ranchat.chat.ChatViewFragment
import com.example.ranchat.model.Friend
import com.example.ranchat.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.card_user.*
import kotlinx.android.synthetic.main.card_user.view.*
import kotlinx.android.synthetic.main.fragment_friend.view.*
import kotlinx.android.synthetic.main.fragment_square.view.*
import java.text.SimpleDateFormat
import java.util.*

class SquareViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_square, container, false)

        view.square_recyclerView.adapter = SquareRecyclerViewAdapter()
        view.square_recyclerView.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class SquareRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var user : ArrayList<User> = arrayListOf()
        init{
            firestore = FirebaseFirestore.getInstance()
            //contentDTOs.clear()
            //contentUidList.clear()
            firestore?.collection("currentUser")?.orderBy("timeStamp",
                Query.Direction.DESCENDING)?.get()?.addOnCompleteListener{
                    Log.d("aa", "1")
                    if(it==null){

                        Log.d("aa", "2")
                    }else{
                        for(snapshot in it.result!!.documents){
                            var item = snapshot.toObject(User::class.java)
                            if(item?.uid != FirebaseAuth.getInstance().currentUser!!.uid){
                                user.add(item!!)
                            }
                        }
                    }
                    notifyDataSetChanged()
                }

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.card_user, parent, false)
            return CustomViewHoler(view)
        }

        inner class CustomViewHoler(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return user.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHoler).itemView

            //UserNickname
            viewholder.cardUser_txtvName.text = user!![position].userNickname
            if(user[position].userUri!=null){
                Glide.with(holder.itemView.context).load(user!![position].userUri).into(viewholder.cardUser_imgv)
            }else{
                viewholder.cardUser_imgv.setColorFilter(Color.parseColor("#A4FBB5"), PorterDuff.Mode.SRC_IN)
            }
            holder.itemView.setOnClickListener {
                val intent = Intent(activity, MessageActivity::class.java)
                intent.putExtra("userNickname",user[position].userNickname)
                intent.putExtra("userUri", user[position].userUri)
                intent.putExtra("destinationUid", user[position].uid)
                startActivity(intent)
            }

        }

    }

}