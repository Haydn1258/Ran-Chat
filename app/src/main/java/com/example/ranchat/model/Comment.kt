package com.example.ranchat.model

data class Comment (
    var uid:String? = null,
    var message:String? = null,
    var timeStamp:Long? = null,
    var pushToken:String? = null
)