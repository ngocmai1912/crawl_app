package com.nmai.crawlnotification

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nmai.crawlnotification.repository.NotificationDatabase
import com.nmai.crawlnotification.service.CrawlNotificationService
import timber.log.Timber
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var tv : TextView
    private lateinit var recy : RecyclerView
    private lateinit var adapter : NotificationAdapter
    private val viewModel : NotificationViewModel by viewModels(){
        object : ViewModelProvider.Factory{
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return NotificationViewModel(application) as T
            }
        }
    }

    companion object{
        var context : Context? = null
        var PACKAGE_NAME_SMS : String? = null
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dao = NotificationDatabase.getInstance(application).notificationDao()

        //Post data with onClick notification
        val post = intent.getStringExtra("post_again_notification")
        if (post != null) {
            viewModel.postNotificationToServer(post)
            Toast.makeText(context,"Posting ...",Toast.LENGTH_SHORT).show()
        }

        PACKAGE_NAME_SMS = Settings.Secure.getString(
            contentResolver,
            "sms_default_application"
        )
        context = this

        val enabledListeners = Settings.Secure.getString(
            this.contentResolver,
            "enabled_notification_listeners"
        )
        var str = CrawlNotificationService().javaClass.toString()
        if (!enabledListeners.contains(str.subSequence(6, str.length)))
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));

        requestSmsPermission()

        viewModel.listNotification.observe(this, {
            adapter.submitList(it)
        })

        recy = findViewById(R.id.list_notification)
        adapter = NotificationAdapter(this)
        recy.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        adapter.onClick = { time ->
            viewModel.postNotificationToServer(time)
        }
    }


    override fun isDestroyed(): Boolean {
        Timber.d("destroy app")
        return super.isDestroyed()
    }

    // set permission
    private fun requestReadSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_SMS
            )
        ) {
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_SMS),
            1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestSmsPermission() {
        val permission = Manifest.permission.RECEIVE_SMS
        val grant = ContextCompat.checkSelfPermission(this, permission)
        if (grant != PackageManager.PERMISSION_GRANTED) {
            val permissionList = arrayOfNulls<String>(1)
            permissionList[0] = permission
            ActivityCompat.requestPermissions(this, permissionList, 1)
        }
    }
}