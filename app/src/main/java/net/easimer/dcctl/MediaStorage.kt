package net.easimer.dcctl

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MediaStorage"

private fun getOutputMediaFile(type: Int): File? {
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    val mediaStorageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "DCCTL"
    )

    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    mediaStorageDir.apply {
        if (!exists()) {
            if (!mkdirs()) {
                Log.d("CameraController/IO", "failed to create directory")
                return null
            }
        }
    }

    // Create a media file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Date())

    val path = "${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg"

    Log.d(TAG, "Path: $path")

    return when (type) {
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
            File(path)
        }
        else -> null
    }
}

private fun savePictureToMediaStorageLegacy(data: ByteArray) {
    val pictureFile: File = getOutputMediaFile(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) ?: run {
        Log.d(TAG, ("Error creating media file, check storage permissions"))
        return
    }

    try {
        val fos = FileOutputStream(pictureFile)
        fos.write(data)
        fos.close()
        Log.d(TAG, "File saved")
    } catch (e: FileNotFoundException) {
        Log.d(TAG, "File not found: ${e.message}")
    } catch (e: IOException) {
        Log.d(TAG, "Error accessing file: ${e.message}")
    }
}

private fun savePictureToMediaStorageQ(ctx: Context, data: ByteArray) {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(Date())
    val relativeLocation = Environment.DIRECTORY_PICTURES + "/DCCTL"

    val contentValues = ContentValues()
    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_$timeStamp")
    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)

    val resolver: ContentResolver = ctx.getContentResolver()

    try {
        var contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var uri = resolver.insert(contentUri, contentValues)

        if (uri != null)
        {
            val stream = resolver.openOutputStream(uri)
            if(stream != null) {
                stream.write(data)
                stream.close()
                Log.d(TAG, "File saved")
            } else {
                Log.d(TAG, "Failed to open output stream")
            }
        } else {
            Log.d(TAG, "URI resolve failure")
        }
    } catch (e: FileNotFoundException) {
        Log.d(TAG, "File not found: ${e.message}")
    } catch (e: IOException) {
        Log.d(TAG, "Error accessing file: ${e.message}")
    }
}

fun savePictureToMediaStorage(ctx: Context, data: ByteArray) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        savePictureToMediaStorageQ(ctx, data)
    } else {
        savePictureToMediaStorageLegacy(data)
    }
}
