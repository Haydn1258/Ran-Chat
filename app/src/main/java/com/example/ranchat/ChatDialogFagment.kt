package com.example.ranchat

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.ranchat.model.ChatUser
import com.example.ranchat.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_drawer.*
import kotlinx.android.synthetic.main.activity_drawer.view.*
import kotlinx.android.synthetic.main.dialog_chat_setting.view.*

open class ChatDialogFagment() : DialogFragment(){


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_chat_setting, container, false)




        view.dialogChatSetting_btnOK.setOnClickListener {

            FirebaseFirestore.getInstance().collection("chatRooms").document(FirebaseAuth.getInstance().uid!!).collection("chatUsers")
                .document(arguments!!.getString("destinationUid")!!).delete().addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("chatRooms").document(FirebaseAuth.getInstance().uid!!).collection("chatUsers")
                        .document(arguments!!.getString("destinationUid")!!).collection("comments").get().addOnSuccessListener {
                            if(it==null){
                                Log.d("snapshot","null")
                            }else{
                                for(snapshot in it.documents){
                                    FirebaseFirestore.getInstance().collection("chatRooms").document(FirebaseAuth.getInstance().uid!!).collection("chatUsers")
                                        .document(arguments!!.getString("destinationUid")!!).collection("comments").document(snapshot.id).delete()
                                }
                            }
                        }
                }

            FirebaseFirestore.getInstance().collection("chatRooms").document(arguments!!.getString("destinationUid")!!)
                .collection("chatUsers").document(FirebaseAuth.getInstance().uid!!).delete().addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("chatRooms").document(arguments!!.getString("destinationUid")!!)
                        .collection("chatUsers").document(FirebaseAuth.getInstance().uid!!).collection("comments").get().addOnSuccessListener {
                            if(it==null){
                                Log.d("snapshot","null")
                            }else{
                                for(snapshot in it.documents){
                                    FirebaseFirestore.getInstance().collection("chatRooms").document(arguments!!.getString("destinationUid")!!)
                                        .collection("chatUsers").document(FirebaseAuth.getInstance().uid!!).collection("comments").document(snapshot.id).delete()
                                }
                                dialog.dismiss()
                                MessageActivity.messageActivity!!.finish()
                            }

                        }
                    /*FirebaseFirestore.getInstance().collection("chatRooms").document(FirebaseAuth.getInstance().uid!!).collection("chatUsers")
                        .document(arguments!!.getString("destinationUid")!!).collection("comments").document().delete()
                    FirebaseFirestore.getInstance().collection("chatRooms").document(arguments!!.getString("destinationUid")!!)
                        .collection("chatUsers").document(FirebaseAuth.getInstance().uid!!).collection("comments").document().delete().addOnSuccessListener {
                        }*/
                }

        }
        view.dialogChatSetting_btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    companion object{
        fun newInstance(destinationUid:String?):ChatDialogFagment{
            val myDialogFragment = ChatDialogFagment()
            val bundle = Bundle()
            bundle.putString("destinationUid", destinationUid)
            myDialogFragment.arguments = bundle
            return myDialogFragment
        }
    }
}