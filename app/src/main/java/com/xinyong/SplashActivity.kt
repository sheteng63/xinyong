package com.xinyong

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast


class SplashActivity : AppCompatActivity() {

    var lau: Job? = null

    // 要申请的权限
    private var permissions = mutableListOf<String>(Manifest.permission.READ_CONTACTS,Manifest.permission.GET_ACCOUNTS,Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,Manifest.permission.READ_CALL_LOG,Manifest.permission.READ_PHONE_STATE,Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        permissions = inspectPermission()
        if (permissions.size == 0) {
            //todo计时
           goingOn()
        } else {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 321)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    fun goingOn(){
        lau = launch(CommonPool) {
            delay(2 * 1000L)
            startActivity<MainActivity>()
            finish()
        }
    }

    fun inspectPermission(): MutableList<String> {
        var permissionsDeni = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                var i = ContextCompat.checkSelfPermission(this, permission)
                if (i != PackageManager.PERMISSION_GRANTED) {
                    permissionsDeni.add(permission)
                }
            }
        }

        return permissionsDeni
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isGoing = true;
        if (requestCode === 321) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                for (permission in permissions) {
                    var i = ContextCompat.checkSelfPermission(this, permission)
                    if (i != PackageManager.PERMISSION_GRANTED) {
                        toast("权限缺失")
                        finish()
                        isGoing = false
                    }
                }
            }
        }

        if (isGoing){
           goingOn()
        }

    }


}
