package com.example.ranchat.setting

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.ranchat.LoginActivity
import com.example.ranchat.MainActivity
import com.example.ranchat.R
import com.example.ranchat.SignUpActivity
import com.example.ranchat.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_setting.*
import java.security.Key

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
        firestore?.collection("user")?.document(FirebaseAuth.getInstance().currentUser?.uid!!)?.addSnapshotListener{
                documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot==null){
                Log.d("snapshot","null")
            }else{
                if(isAdded()){
                    if (MainActivity.settingBoolean){
                        var user = documentSnapshot.toObject(User::class.java)
                        if (user?.userUri!=null){
                            Log.d("aaffd",user.userUri.toString())
                            /*ref.getDownloadUrl().addOnCompleteListener(OnCompleteListener {
                                   if(it.isSuccessful){
                                       Glide.with(this)
                                           .load(it.getResult())
                                           .into(settingImageView);
                                   }
                               })*/
                            Glide.with(this)
                                .load(user.userUri)
                                .override(100,100)
                                .centerCrop()
                                .into(settingImageView)
                        }else{
                            settingImageView.setImageResource(R.drawable.baseline_supervised_user_circle_black_48dp2)
                            settingImageView.setColorFilter(
                                Color.parseColor("#A4FBB5"),
                                PorterDuff.Mode.SRC_IN
                            )
                        }
                    }
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