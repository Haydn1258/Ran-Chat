package com.example.ranchat.model

class NotificationModel {
    var to:String? = null
    var notification = Notification()
    var data = Data()

    inner class Notification{
        var title:String? = null
        var text:String? = null
    }
    inner class Data{
        var title:String? = null
        var text:String? = null
    }
}