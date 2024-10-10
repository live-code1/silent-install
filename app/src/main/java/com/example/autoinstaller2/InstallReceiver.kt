package com.example.autoinstaller2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class InstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val apkUriString = intent.getStringExtra("apkUri") ?: return
        val apkUri = Uri.parse(apkUriString)

        Toast.makeText(context, "Installation Started", Toast.LENGTH_SHORT).show()
        installApk(context, apkUri)
    }

//    private fun installApk(context: Context, apkUri: Uri) {
//        val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
//            data = apkUri
//            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//            putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
//        }
//        context.startActivity(installIntent)
//    }

    private fun installApk(context: Context?, apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        context?.startActivity(intent)
    }
}
