package com.example.ranchat.friend

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
import com.example.ranchat.R
import com.example.ranchat.model.Friend
import com.example.ranchat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.card_user.view.*
import kotlinx.android.synthetic.main.fragment_friend.view.*

class FriendViewFragment : Fragment() {
    var firestore : FirebaseFirestore? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            LayoutInflater.from(activity).inflate(R.layout.fragment_friend, container, false)
        view.friend_recyclerView.adapter = FriendRecyclerViewAdapter()
        view.friend_recyclerView.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class FriendRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var user : ArrayList<User> = arrayListOf()
        var mode = true
        init{
            firestore = FirebaseFirestore.getInstance()
            //contentDTOs.clear()
            //contentUidList.clear()
            firestore?.collection("user")?.document(FirebaseAuth.getInstance().currentUser?.email!!)?.collection("friend")?.
                addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(querySnapshot==null){
                    Log.d("snapshot","null")
                }else{
                    for(snapshot in querySnapshot!!.documents){
                        var item = snapshot.toObject(User::class.java)
                        user.add(item!!)

                    }
                    Log.d("aa", user.toString())
                }
                mode=false
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


            /*holder.itemView.setOnClickListener {
                MainActivity.homeStack.push(fragmentManager!!.findFragmentById(R.id.mainContent))
                val fragment: Fragment = DetailViewFragment() // Fragment 생성
                val bundle = Bundle(9) // 파라미터는 전달할 데이터 개수
                bundle.putString("uri", contentDTOs!![position].imageUri) // key , value
                bundle.putSerializable("alcoholname",contentDTOs!![position].alcoholname)
                bundle.putSerializable("rating",contentDTOs!![position].starRating)
                bundle.putSerializable("intro",contentDTOs!![position].intro)
                bundle.putSerializable("snack",contentDTOs!![position].snack)
                bundle.putSerializable("price",contentDTOs!![position].price)
                bundle.putString("writename", contentDTOs!![position].writeName)
                bundle.putSerializable("uid", contentDTOs!![position].uid)
                bundle.putSerializable("stack","home")
                fragment.arguments = bundle
                fragmentManager!!.beginTransaction().replace(R.id.mainContent, fragment).commit()
            }*/

        }

    }
}