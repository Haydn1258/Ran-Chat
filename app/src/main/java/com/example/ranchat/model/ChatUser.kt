package com.example.ranchat.model

data class ChatUser (
    var userNickname:String? = null,
    var uid:String? = null,
    var timeStamp:Long? = null,
    var lastMessage:String? = null
)