package com.picture.crop

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_crop.*
import java.io.File
import java.io.FileOutputStream

class CropActivity : AppCompatActivity() {
    private val CROP_RESULT = "crop_result"

    companion object {
        val CROP_CODE = 0x0101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        crop_view.setBitmapResource(intent.data)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_crop, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.crop) {
            try {
                val croppedBitmap = crop_view.getCropBitmap()
                if (croppedBitmap == null) {
                    setResult(RESULT_CANCELED)
                    finish()
                    return true
                }
                val result = Intent()
                result.data = (Uri.fromFile(saveBitmap2File(croppedBitmap, CROP_RESULT)))
                setResult(Activity.RESULT_OK, result)
            } catch (e: Exception) {
                e.printStackTrace()
                setResult(Activity.RESULT_CANCELED)
            } finally {
                finish()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveBitmap2File(bitmap: Bitmap, path: String): File {
        Log.i("crop", "start save picture")
        val dstFile = File(Environment.getExternalStorageDirectory(), path)
        if (dstFile.exists()) {
            dstFile.delete()
        }
        dstFile.createNewFile()
        val outStream = FileOutputStream(dstFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
        outStream.flush()
        outStream.close()
        Log.i("crop", "end save picture")
        return dstFile
    }

}