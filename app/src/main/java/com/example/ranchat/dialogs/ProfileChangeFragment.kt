package com.example.ranchat.dialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.ranchat.R
import com.example.ranchat.setting.ProfileActivity
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.dialog_profile_change.view.*

open class ProfileChangeFragment() : DialogFragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_profile_change, container, false)
        view.dialogProfielChange_Album.setOnClickListener {

          //  var photoPickerIntent = Intent(Intent.ACTION_PICK)
           // photoPickerIntent.type = "image/*"
            //startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
            //dismiss()


        }

        return view
    }

}