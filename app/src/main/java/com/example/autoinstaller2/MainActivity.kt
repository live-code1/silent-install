package com.example.autoinstaller2

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.paperdb.Paper
import java.io.File
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    var testUri: Uri? = null

    private lateinit var apkRecyclerView: RecyclerView
    private lateinit var apkAdapter: ApkAdapter
    private lateinit var tvSelectedPath: TextView
    private var selectedPathUri: Uri? = null
    val documentFiles = mutableListOf<Uri>()
    private var apkIndex = 0
    private  val  REQUEST_CODE_INSTALL_APK = 2001

    private lateinit var selectedDateText: TextView
    private lateinit var selectDateButton: Button
    private lateinit var selectTimeButton: Button
    private lateinit var scheduleButton: Button

    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Paper.init(this)

        apkRecyclerView = findViewById(R.id.apkRecyclerView)
        tvSelectedPath = findViewById(R.id.tvSelectedPath)

        selectedDateText = findViewById(R.id.selectedDateText)
        selectDateButton = findViewById(R.id.selectDateButton)
        selectTimeButton = findViewById(R.id.selectTimeButton)
        scheduleButton = findViewById(R.id.scheduleButton)

        selectDateButton.setOnClickListener {
            showDatePicker()
        }

        selectTimeButton.setOnClickListener {
            showTimePicker()
        }

        scheduleButton.setOnClickListener {
//            scheduleNotification()
            scheduleInstallations(100)
        }

        // Set up RecyclerView
        apkRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up Select Path button
        val btnSelectPath = findViewById<Button>(R.id.btnSelectPath)
        btnSelectPath.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission if not granted
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                // Launch directory picker
                pickDirectory()
            }
        }
    }


    // Directory picker intent
    private fun pickDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        directoryPickerLauncher.launch(intent)
    }

    // Handle the result of directory selection
    private val directoryPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val treeUri = result.data?.data
                if (treeUri != null) {
                    // Save the selected path URI
                    selectedPathUri = treeUri

                    // Show the selected path in TextView
                    val documentId = DocumentsContract.getTreeDocumentId(treeUri)
                    tvSelectedPath.text = "Selected Path: $documentId"

                    // Grant permission to read from selected directory
                    contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    // Display APK files in RecyclerView
                    displayApkFiles(treeUri)
                }
            } else {
                Toast.makeText(this, "No directory selected", Toast.LENGTH_SHORT).show()
            }
        }

    // Function to display APK files from the selected directory
    private fun displayApkFiles(treeUri: Uri) {
        val apkFiles = getApkFiles(treeUri)
        if (apkFiles.isNotEmpty()) {
            Paper.book().write("app_list", documentFiles)
            apkAdapter = ApkAdapter(apkFiles)
            apkRecyclerView.adapter = apkAdapter
            apkAdapter.itemClickListener = { view, item, position ->
//                installApk(documentFiles[position].uri)
//                installNextApk()

//                testUri?.let { installApk(it) }

            }
        } else {
            Toast.makeText(this, "No APK files found in this directory", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to get APK files in the selected directory (using Uri)
    private fun getApkFiles(treeUri: Uri): List<File> {
        // This is a basic implementation. Depending on Android versions,
        // you may need to parse the Uri to retrieve File objects.
//        val apkFiles = mutableListOf<File>()
//        val directory = File(treeUri.path) // Convert URI to file path (might need further handling)
//        if (directory.exists() && directory.isDirectory) {
//            directory.listFiles { file -> file.extension == "apk" }?.let {
//                apkFiles.addAll(it)
//            }
//        }
        val apkFiles = mutableListOf<File>()
        val documentFile = DocumentFile.fromTreeUri(this, treeUri)
        if (documentFile!= null){
            if (documentFile.isDirectory) {
                for (child in documentFile.listFiles()) {
                    if (child.isFile && child.name!!.endsWith(".apk")) {
                        child.uri.path?.let { File(it) }?.let { apkFiles.add(it) }
                        documentFiles.add(child.uri)
                    }
                }
            }
        }

        return apkFiles
    }


    private fun installNextApk() {
        if (apkIndex < documentFiles.size) {
            val apkUri = documentFiles[apkIndex]

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(intent, REQUEST_CODE_INSTALL_APK)
        } else {
            Toast.makeText(this, "All APKs installed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INSTALL_APK) {
            // Move to the next APK
            apkIndex++
            installNextApk()
        }
    }

    private fun showDatePicker() {
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            updateDateInView()
        }, currentYear, currentMonth, currentDay)
        datePicker.show()
    }

    private fun showTimePicker() {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            updateDateInView()
        }, currentHour, currentMinute, true)
        timePicker.show()
    }

    private fun updateDateInView() {
        selectedDateText.text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", calendar)
    }

    private fun scheduleInstallations(startTimeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val installationIntervalMillis = 60_000L // 1 minute interval between each installation

        for (i in documentFiles.indices) {
            val apkUri = documentFiles[i]

            // Intent to trigger the installation via BroadcastReceiver
            val intent = Intent(this, InstallReceiver::class.java)
            intent.data = apkUri
            intent.putExtra("apkUri", apkUri.toString())

            // Create a unique PendingIntent for each APK
            val pendingIntent = PendingIntent.getBroadcast(
                this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the installation of each APK one by one with the specified time interval
            val scheduledTime = startTimeInMillis + (i * installationIntervalMillis)
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                scheduledTime,
                pendingIntent
            )

            Toast.makeText(this, "Installation scheduled for: ${selectedDateText.text}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("DATE_TIME", selectedDateText.text.toString())

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        Toast.makeText(this, "Installation scheduled for: ${selectedDateText.text}", Toast.LENGTH_SHORT).show()
    }


    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickDirectory()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}