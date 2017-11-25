package com.picture.crop

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.startActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private val FROM_GALLERY = 0x0303
    private val FROM_CAMERA = 0x0202
    private val pictureTokenFile = File(Environment.getExternalStorageDirectory(), "camera_pick.jpeg")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        grantWriteAccess()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.from_camera -> {
                fromCamera()
                return true
            }

            R.id.from_gallery -> {
                fromGallery()
                return true
            }

            R.id.anko_layout -> {
                startActivity<AnkoLayoutActivity>()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            FROM_GALLERY -> {
                if (null != data) {
                    ToCrop(data.data)
                }
            }

            FROM_CAMERA -> {
                ToCrop(Uri.fromFile(pictureTokenFile))
            }

            CropActivity.CROP_CODE -> {
                cropped_image.setImageBitmap(BitmapFactory.decodeStream(contentResolver.openInputStream(data?.data)))
            }
        }
    }

    private fun ToCrop(uri: Uri) {
        Log.i("main", "uri: " + uri.toString())
        val cropPicture = Intent(this, CropActivity::class.java)
        cropPicture.data = uri
        startActivityForResult(cropPicture, CropActivity.CROP_CODE)
    }

    private fun fromGallery() {
        val fromGallery = Intent(Intent.ACTION_GET_CONTENT)
        fromGallery.type = "image/jpeg"
        startActivityForResult(fromGallery, FROM_GALLERY)
    }

    private fun fromCamera() {
        val fromCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        fromCamera.putExtra("output", Uri.fromFile(pictureTokenFile))
        startActivityForResult(fromCamera, FROM_CAMERA)
    }

    private fun grantWriteAccess() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }
}
