package com.example.ranchat.setting

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.ranchat.LoginActivity
import com.example.ranchat.MainActivity
import com.example.ranchat.R
import com.example.ranchat.dialogs.ProfileChangeFragment
import com.example.ranchat.model.User
import com.example.ranchat.square.WritingActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.dialog_profile_change.*
import kotlinx.android.synthetic.main.dialog_profile_change.view.*
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ProfileActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0;
    var storage : FirebaseStorage? = null
    val READ_EXTERNAL_STORAGE_PERMISSION = 100

    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    var photoUri:Uri? = null
    var userUri:String? = null

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
        profile_imgButton.setOnClickListener {
            finish()
        }

        firestore?.collection("user")?.document(FirebaseAuth.getInstance().currentUser?.uid!!)?.
            get()?.addOnSuccessListener {
                if(it == null){
                    Log.d("snapshot","null")
                }else{
                    var user = it.toObject(User::class.java)
                    userUri = user?.userUri
                    Log.d("aaff", user?.userUri.toString())

                    profile_edtNickName.setText(user?.userNickname.toString())
                    if (userUri!=null){
                        profile_imgv.setColorFilter(
                            Color.parseColor("#A4FBB5"),
                            PorterDuff.Mode.DST
                        )
                        Glide.with(this)
                            .load(userUri)
                            .override(100,100)
                            .centerCrop()
                            .into(profile_imgv)
                    }else{
                        profile_imgv.setImageResource(R.drawable.baseline_supervised_user_circle_black_48dp2)
                        profile_imgv.setColorFilter(
                            Color.parseColor("#A4FBB5"),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                }
            }

        profile_imgv.setOnClickListener {
            showDialog(it.context)
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
                Glide.with(this)
                    .load(photoUri)
                    .override(100,100)
                    .centerCrop()
                    .into(profile_imgv)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    var photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
                    profile_imgv.setColorFilter(
                        Color.parseColor("#A4FBB5"),
                        PorterDuff.Mode.DST
                    )
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun showDialog(context: Context){
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()

        val layoutInterface = layoutInflater
        val view = layoutInterface.inflate(R.layout.dialog_profile_change, null)
        view.dialogProfielChange_Album.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE_PERMISSION)
            dialog.dismiss()
        }
        view.dialogProfielChange_basic.setOnClickListener {
            userUri = null
            photoUri = null
            profile_imgv.setImageResource(R.drawable.baseline_supervised_user_circle_black_48dp2)
            profile_imgv.setColorFilter(
                Color.parseColor("#A4FBB5"),
                PorterDuff.Mode.SRC_IN
            )
            dialog.dismiss()
        }
        dialog.setView(view)
        dialog.show()
    }

    fun update(){
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE"+timestamp+"_.png"
        var userNickname :String = ""
        if (!profile_edtNickName.text.toString().replace(" ", "").equals("")){
            if(photoUri != null){
                var storageRef = storage?.reference?.child("image")?.child(imageFileName)
                storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                    return@continueWithTask storageRef.downloadUrl
                }?.addOnSuccessListener { uri ->
                    var userUpdate:MutableMap<String,Any> = mutableMapOf("userUri" to uri.toString(), "userNickname" to profile_edtNickName.text.toString())
                    firestore?.collection("user")?.document(auth?.currentUser?.uid!!)?.update(userUpdate)?.addOnSuccessListener {
                        firestore?.collection("currentUser")?.document(auth?.currentUser?.uid!!)?.update(userUpdate)?.addOnSuccessListener {
                            finish()
                            profile_btnChange.isEnabled=true
                        }
                    }
                    setResult(Activity.RESULT_OK)
                    Toast.makeText(this, "프로필 수정 완료", Toast.LENGTH_LONG).show()


                }

            }else{
                var userUpdate:MutableMap<String,Any?> = mutableMapOf("userNickname" to profile_edtNickName.text.toString(), "userUri" to userUri)
                firestore?.collection("user")?.document(auth?.currentUser?.uid!!)?.update(userUpdate)?.addOnSuccessListener {
                    firestore?.collection("currentUser")?.document(auth?.currentUser?.uid!!)?.update(userUpdate)?.addOnSuccessListener {
                        finish()
                        profile_btnChange.isEnabled=true
                    }
                }
            }
        }else{
            profile_edtNickName.setText("")
            Toast.makeText(applicationContext, "닉네임을 다시 입력해주세요", Toast.LENGTH_SHORT).show()
            profile_btnChange.isEnabled=true
        }

    }

}
