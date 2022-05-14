package com.example.internalandexternalstorageproject

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.*
import java.nio.charset.Charset
import java.util.*


class MainActivity : AppCompatActivity() {
    var isPersistent:Boolean = false
    var isInternal: Boolean = false

    var readPermissionGranted = false
    var writePermissionGranted = false

    lateinit var saveInternalBtn:Button
    lateinit var readInternalBtn:Button
    lateinit var deleteInternalBtn:Button
    lateinit var saveExternalBtn: Button
    lateinit var readExternalBtn: Button
    lateinit var deleteExternalBtn: Button
    lateinit var saveToSD:Button
    lateinit var moveSettingsBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        createInternalFile()
        requestPermission()

        initViews()
    }

    private fun initViews() {
        saveInternalBtn = findViewById(R.id.saveInternalBtn)
        saveInternalBtn.setOnClickListener {
            saveInternal("PDP Academy")
        }

        readInternalBtn = findViewById(R.id.readInternalBtn)
        readInternalBtn.setOnClickListener {
            readInternalFile()
        }

        deleteInternalBtn = findViewById(R.id.deleteInternalBtn)
        deleteInternalBtn.setOnClickListener {
            deleteInternalFile()
        }


        saveExternalBtn = findViewById(R.id.saveExternalBtn)
        saveExternalBtn.setOnClickListener {
            saveExternalFile("Feruz Davlatov")
        }

        readExternalBtn = findViewById(R.id.readExternalBtn)
        readExternalBtn.setOnClickListener {
            readExternalFile()
        }

        deleteExternalBtn = findViewById(R.id.deleteExternalBtn)
        deleteExternalBtn.setOnClickListener {
            deleteExternalFile()
        }

        saveToSD = findViewById(R.id.takePhotoBtn)
        saveToSD.setOnClickListener {
            takePhoto.launch()
        }

        moveSettingsBtn = findViewById(R.id.moveSettingsBtn)
        moveSettingsBtn.setOnClickListener {
            moveSettings()
        }



    }

    private fun moveSettings(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    //Create Internal File
    private fun createInternalFile()    {
        val fileName = "pdp_internal.txt"
        val file:File
        file = if (isPersistent) {
            File(filesDir, fileName)
        }else {
            File(cacheDir, fileName)
        }

        if (!file.exists()) {
            try {
                file.createNewFile()
                Toast.makeText(this, "File has been created" , Toast.LENGTH_SHORT).show()
            } catch (e: IOException){
                Toast.makeText(this, "File creation failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "File has already created", Toast.LENGTH_SHORT).show()
        }
    }

    //Internal & External Paths
    fun checkStoragePaths() {
        val internal_n1 = getDir("custom", 0)
        val internal_n2 = filesDir

        val external_n1 = getExternalFilesDir(null)
        val external_n2 = externalCacheDir
        val external_n3 = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        Log.d("StorageActivity", internal_n1.absolutePath)
        Log.d("StorageActivity", internal_n2.absolutePath)
        Log.d("StorageActivity", external_n1!!.absolutePath)
        Log.d("StorageActivity", external_n2!!.absolutePath)
        Log.d("StorageActivity", external_n3!!.absolutePath)
    }

    //Save Internal File
        private fun saveInternal(data: String) {
        val fileName = "pdp_internal.txt"
        try {
            val fileOutputStream: FileOutputStream
            fileOutputStream = if (isPersistent){
                openFileOutput(fileName, MODE_PRIVATE)
            } else {
                val file = File(cacheDir, fileName)
                FileOutputStream(file)
            }
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, "text is successfully written", Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            Toast.makeText(this, "write to file is failed", Toast.LENGTH_SHORT).show()
        }
    }

    //Read Internal File
    private fun readInternalFile() {
        val fileName = "pdp_internal.txt"
        try {
            val fileInputStream: FileInputStream
            fileInputStream = if (isPersistent) {
                openFileInput(fileName)
            }else {
                val file = File(cacheDir, fileName)
                FileInputStream(file)
            }

            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines: MutableList<String?> = ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()
            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Toast.makeText(this, "Read from file is successfully", Toast.LENGTH_SHORT).show()
        }catch (e: Exception) {
            Toast.makeText(this, "Read from is failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteInternalFile(){
        val file = "pdp_internal.txt"
        val myFile = File(filesDir,file)
        val fileExists = myFile.exists()
        try {
            if (fileExists){
                myFile.delete()
            }
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        }catch (e: IOException){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }


    //Request Permission

    private fun requestPermission() {
        val hasReadPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()

        if (!readPermissionGranted) permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!writePermissionGranted) permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (!permissionsToRequest.isEmpty()) permissionLauncher.launch(permissionsToRequest.toTypedArray())

    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        readPermissionGranted = permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: readPermissionGranted
        writePermissionGranted = permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: writePermissionGranted

        if (readPermissionGranted) Toast.makeText(this, "READ_EXTERNALSTORAGE", Toast.LENGTH_SHORT).show()
        if (writePermissionGranted) Toast.makeText(this, "WRITE_EXTERNALSTORAGE", Toast.LENGTH_SHORT).show()

    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){ bitmap ->
            val filename = UUID.randomUUID().toString()
        val isPhotoSaved = if (isInternal) {
            savePhotoToInternalStorage(filename, bitmap!!)
        }else {
            if (writePermissionGranted) {
                savePhotoToExternalStorage(filename, bitmap!!)
            }else {
                false
            }
        }
        if (isPhotoSaved) {
            Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
        }else {
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return try {
            openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap")
                }
            }
            true
        }catch (e: IOException){
            e.printStackTrace()
            false
        }
    }

    private fun savePhotoToExternalStorage(filename: String, bmp: Bitmap): Boolean {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"$filename.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"$filename.jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }
        return try {
            contentResolver.insert(collection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)){
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        }catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    //Save External File
    private fun saveExternalFile(data: String) {
        val fileName = "pdp_external_text.txt"
        val file: File
        file = if (isPersistent) {
            File(getExternalFilesDir(null), fileName)
        }else {
            File(externalCacheDir, fileName)
        }
        try {
            val fileOutputStream =FileOutputStream(file)
            fileOutputStream.write(data.toByteArray(Charset.forName("UTF-8")))
            Toast.makeText(this, "Write to is successfully", Toast.LENGTH_SHORT).show()
        }catch (e:java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Write to file is failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteExternalFile(){
        val file = "pdp_external_text.txt"

        val externalCacheFile = File(externalCacheDir, file)
        val fileExist = externalCacheFile.exists()
        try {
            if (fileExist){
                externalCacheFile.delete()
            }
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
        }catch (e: IOException){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }


    //Read External File

    private fun readExternalFile() {
        val fileName = "pdp_external.txt"
        val file: File
        file = if (isPersistent){
            File(getExternalFilesDir(null), fileName)
        } else {
            File(externalCacheDir, fileName)
        }
        try {
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream, Charset.forName("UTF-8"))
            val lines: MutableList<String> = java.util.ArrayList()
            val reader = BufferedReader(inputStreamReader)
            var line = reader.readLine()

            while (line != null) {
                lines.add(line)
                line = reader.readLine()
            }
            val readText = TextUtils.join("\n", lines)
            Log.d("StorageActivity", readText)
            Toast.makeText(this, "Read from file is successfully", Toast.LENGTH_SHORT).show()
        }catch (e: java.lang.Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Read From file is failed", Toast.LENGTH_SHORT).show()
        }

    }


}