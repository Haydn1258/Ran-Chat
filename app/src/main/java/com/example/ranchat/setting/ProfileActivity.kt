package com.example.ranchat.setting

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.ranchat.MainActivity
import com.example.ranchat.R
import com.example.ranchat.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_setting.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ProfileActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0;
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        if(Build.VERSION.SDK_INT >= 21) {
            profile_imgv.background = ShapeDrawable(OvalShape())
            profile_imgv.clipToOutline = true
        }
        firestore?.collection("user")?.document(FirebaseAuth.getInstance().currentUser?.uid!!)?.
            get()?.addOnSuccessListener {
                if(it == null){
                    Log.d("snapshot","null")
                }else{
                    var user = it.toObject(User::class.java)
                    if (user?.userUri!=null){
                        Glide.with(this)
                            .load(user.userUri)
                            .into(profile_imgv)
                    }else{
                        profile_imgv.setImageResource(R.drawable.baseline_supervised_user_circle_black_48dp2)
                    }

                }
            }


        profile_imgv.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }
        profile_btnChange.setOnClickListener {
            profile_btnChange.isEnabled=false
            update()


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                profile_imgv.setImageURI(photoUri)
            }
        }
    }
    fun update(){
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE"+timestamp+"_.png"
        var userNickname :String = ""

        if(photoUri != null){
            var storageRef = storage?.reference?.child("image")?.child(imageFileName)
            storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }?.addOnSuccessListener { uri ->
                var userUpdate:MutableMap<String,Any> = mutableMapOf("userUri" to uri.toString())
                firestore?.collection("user")?.document(auth?.currentUser?.uid!!)?.update(userUpdate)?.addOnSuccessListener {
                    firestore?.collection("currentUser")?.document(auth?.currentUser?.uid!!)?.update(userUpdate)?.addOnSuccessListener {
                        finish()
                        profile_btnChange.isEnabled=true
                    }
                }
                setResult(Activity.RESULT_OK)
                Toast.makeText(this, "업로드 완료", Toast.LENGTH_LONG).show()


            }

        }

    }
}
