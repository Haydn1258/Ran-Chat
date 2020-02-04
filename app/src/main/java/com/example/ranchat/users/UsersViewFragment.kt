package com.example.ranchat.users

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ranchat.MessageActivity
import com.example.ranchat.R
import com.example.ranchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.card_user.view.*
import kotlinx.android.synthetic.main.fragment_users.view.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import java.util.*

class UsersViewFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var chatRoomUid:String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_users, container, false)
        view.users_recyclerView.adapter = UsersRecyclerViewAdapter()
        view.users_recyclerView.layoutManager = LinearLayoutManager(activity)


        return view
    }

    inner class UsersRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var user: ArrayList<User> = arrayListOf()

        init {
            firestore = FirebaseFirestore.getInstance()
            //contentDTOs.clear()
            //contentUidList.clear()
            firestore?.collection("currentUser")?.orderBy(
                "timeStamp",
                Query.Direction.DESCENDING
            )?.get()?.addOnSuccessListener {
                if (it == null) {
                } else {
                    for (snapshot in it.documents) {
                        var item = snapshot.toObject(User::class.java)
                        if (item?.uid == FirebaseAuth.getInstance().currentUser!!.uid) {
                            continue
                        }
                        user.add(item!!)
                    }
                }
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.card_user, parent, false)
            if(Build.VERSION.SDK_INT >= 21) {
                view.cardUser_imgv.background = ShapeDrawable(OvalShape())
                view.cardUser_imgv.clipToOutline = true
            }
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

            if (user[position].userUri != null) {
                Glide.with(holder.itemView.context).load(user!![position].userUri)
                    .override(50,50)
                    .centerCrop()
                    .into(viewholder.cardUser_imgv)
            }else {
                viewholder.cardUser_imgv.setColorFilter(
                    Color.parseColor("#A4FBB5"),
                    PorterDuff.Mode.SRC_IN
                )
            }
            viewholder.cardUser_txtvDateTime.text = getDiffTimeText(user[position].timeStamp!!)
            holder.itemView.setOnClickListener {
                val intent = Intent(activity, MessageActivity::class.java)
                intent.putExtra("userNickname", user[position].userNickname)
                intent.putExtra("userUri", user[position].userUri)
                intent.putExtra("destinationUid", user[position].uid)
                startActivity(intent)

            }


        }


    }


    fun getDiffTimeText(targetTime:Long):String{
        val curDateTime = DateTime()
        val targetDateTime = DateTime().withMillis(targetTime)
        val diffDay = Days.daysBetween(curDateTime, targetDateTime).days
        val diffHours = Hours.hoursBetween(targetDateTime, curDateTime).hours
        val diffMinutes = Minutes.minutesBetween(targetDateTime, curDateTime).minutes

        if(diffDay == 0){
            if(diffHours == 0 && diffMinutes == 0){
                return "방금전"
            }
            return if(diffHours > 0){
                ""+ diffHours + "시간전"
            }else "" + diffMinutes + "분전"
        }else{
            if (diffDay>-7){
                return ""+Days.daysBetween(targetDateTime,curDateTime).days+"일전"
            }else{
                val format = SimpleDateFormat("MM월 dd일")
                return format.format(Date(targetTime))
            }

        }
    }


}