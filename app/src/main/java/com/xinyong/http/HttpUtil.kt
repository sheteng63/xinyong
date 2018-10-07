package com.xinyong.http

import android.text.TextUtils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


/**
 * Created by sheteng on 2017/12/8.
 */
object HttpUtil {

    fun post(f: HttpConfig.() -> Unit) {
        launch(CommonPool) {
            val httpConfig = HttpConfig()
            f(httpConfig)
            postNet(httpConfig)
        }
    }

    private fun postNet(http: HttpConfig) {
        try {
            val data = getRequestData(http.params, http.encode).toString().toByteArray()
            val url = URL(http.url)
            println("http.url = " + http.url)
            val urlConn = url.openConnection() as HttpURLConnection
            urlConn.connectTimeout = http.timeOut
            urlConn.doInput = true
            urlConn.doOutput = true
            urlConn.requestMethod = "POST"
            urlConn.useCaches = false
            for (maps in http.heards) {
                urlConn.setRequestProperty(maps.key, maps.value)
            }
            val out = urlConn.outputStream
            out.write(data)
            val response = urlConn.responseCode
            var result = ""
            if (response == HttpURLConnection.HTTP_OK) {
                val inputStream = urlConn.inputStream
                result = dealResponseResult(inputStream)
                inputStream.close()
                urlConn.disconnect()
            } else {
                http.onFail("网络错误")
                return
            }
            launch(UI) {
                if (!TextUtils.isEmpty(result)) {
                    http.onSuccess(result)
                } else {
                    http.onFail("返回数据为空")
                }
            }

        } catch (e: Exception) {
            http.onError(e.toString())

        }
    }

    fun dealResponseResult(inputStream: InputStream): String {
        var resultData: String? = null      //存储处理结果
        val byteArrayOutputStream = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var len = 0
        try {
            while (inputStream.read(data).apply { len = this } != -1) {
                byteArrayOutputStream.write(data, 0, len)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        resultData = String(byteArrayOutputStream.toByteArray())
        return resultData
    }


    fun get(f: HttpConfig.() -> HttpConfig) {
        launch(CommonPool) {
            val httpConfig = HttpConfig()
            f(httpConfig)
            getNet(httpConfig)
        }
    }

    private fun getNet(http: HttpConfig) {
        try {
            var strUrlPath = http.url
            val append_url = getRequestData(http.params, http.encode).toString()
            strUrlPath = strUrlPath + "?" + append_url
            val url = URL(strUrlPath)
            val urlConn = url.openConnection() as HttpURLConnection
            urlConn.connectTimeout = http.timeOut
            for (maps in http.heards) {
                urlConn.setRequestProperty(maps.key, maps.value)
            }
            val `in` = InputStreamReader(urlConn.inputStream)

            val buffer = BufferedReader(`in`)
            var inputLine: String? = null

            var result = ""

            while (buffer.readLine().apply { inputLine = this } != null) {
                result += inputLine!! + "\n"
            }

            `in`.close()
            urlConn.disconnect()
            launch(UI) {
                if (TextUtils.isEmpty(result)) {
                    http.onFail("返回数据为空")
                } else {
                    http.onSuccess(result)
                }
            }
        } catch (e: Exception) {
            http.onError(e.toString())
        }
    }


    private fun getRequestData(params: Map<String, String>, encode: String): StringBuffer {
        val stringBuffer = StringBuffer()
        if (params.size == 0) {
            return stringBuffer
        }
        try {
            for ((key, value) in params) {
                stringBuffer.append(key)
                        .append("=")
                        .append(URLEncoder.encode(value, encode))
                        .append("&")
            }
            stringBuffer.deleteCharAt(stringBuffer.length - 1)    //删除最后的一个"&"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return stringBuffer
    }

}