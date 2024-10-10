package com.example.autoinstaller2


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import io.paperdb.Paper
import java.io.File

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val dateTime = intent?.getStringExtra("DATE_TIME")
        Toast.makeText(context, "Notification: $dateTime", Toast.LENGTH_LONG).show()
        openApp(context!!)

//        Paper.init(context!!)
//         val appList:  List<Uri> =
//            Paper.book().read("app_uri_list") ?: arrayListOf()

//        installApk(context, appList[0])
    }

//    private fun installApk(context: Context?, apkUri: Uri) {
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
//        context?.startActivity(intent)
//    }

    // This method will open the app
    private fun openApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Log.e("InstallReceiver", "Launch intent not found for the app")
        }
    }

}
