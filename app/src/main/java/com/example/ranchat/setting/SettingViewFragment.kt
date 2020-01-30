package com.example.ranchat.setting

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ranchat.R
import com.example.ranchat.SignUpActivity
import com.example.ranchat.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingViewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_setting, container, false)
        val settingImageView =  view.findViewById<ImageView>(R.id.setting_imgv)
        var firestore : FirebaseFirestore? = FirebaseFirestore.getInstance()
        if(Build.VERSION.SDK_INT >= 21) {
            settingImageView.background = ShapeDrawable(OvalShape())
            settingImageView.clipToOutline = true
        }

        firestore?.collection("user")?.document(FirebaseAuth.getInstance().currentUser?.uid!!)?.
            addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(querySnapshot==null){
                    Log.d("snapshot","null")
                }else{
                    var user = querySnapshot.toObject(User::class.java)
                    if (user?.userUri!=null){
                        /*ref.getDownloadUrl().addOnCompleteListener(OnCompleteListener {
                            if(it.isSuccessful){
                                Glide.with(this)
                                    .load(it.getResult())
                                    .into(settingImageView);
                            }
                        })*/
                        Glide.with(this)
                            .load(user.userUri)
                            .into(settingImageView)
                    }else{
                        settingImageView.setImageResource(R.drawable.baseline_supervised_user_circle_black_48dp2)
                    }

                }
            }



        settingImageView.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            startActivity(intent)
        }



        return view
    }

}