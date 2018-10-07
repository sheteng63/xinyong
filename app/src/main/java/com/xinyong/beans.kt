package com.xinyong

import java.time.Duration

data class Contact(
        var name: String,
        var num: MutableList<String>
)

data class CallRecords(
        var num: String,
        var time: String,
        var duration: String
)

data class Sms(
        var address: String,
        var person: String?,
        var body: String,
        var date: String,
        var type: String
)

