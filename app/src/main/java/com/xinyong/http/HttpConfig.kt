package com.xinyong.http


/**
 * Created by sheteng on 2017/12/8.
 */
open class HttpConfig {

    var heards: MutableMap<String, String> = mutableMapOf()
    var params: MutableMap<String, String> = mutableMapOf()

    var url: String = ""
    var onSuccess: (String) -> Unit = {

    }
    var onFail: ((String) -> Unit) = {

    }
    var onError: ((String) -> Unit) = {

    }

    var encode: String = "utf-8"

    var timeOut: Int = 5000
}