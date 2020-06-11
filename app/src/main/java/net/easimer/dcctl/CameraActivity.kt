package net.easimer.dcctl

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import java.util.*

class CameraActivity : AppCompatActivity() {
    private lateinit var camctl : ICameraController

    companion object {
        val EXTRA_ID = "Id"
        val EXTRA_CAMERA_VER = "CamVer"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val extras = intent.extras
        val id = extras?.getString(EXTRA_ID) ?: "uh oh"
        val camVersion = extras?.getInt(EXTRA_CAMERA_VER) ?: 1

        if(gCommandSourceStorage.containsKey(id)) {
            val cmdSrc = gCommandSourceStorage[id]
            gCommandSourceStorage.remove(id)

            if(cmdSrc != null) {
                // camctl = CameraController(this, cmdSrc)
                // camctl = createCameraController(this, cmdSrc, 1)
                try {
                    camctl = createCameraController(this, cmdSrc, camVersion)
                } catch(e: Exception) {
                    finish()
                }

//                preview = camctl.cam?.let {
//                    CameraPreview(this, it)
//                }
                val preview = camctl.makePreviewView(this)

                preview?.also {
                    val preview: FrameLayout = findViewById(R.id.cameraPreview)
                    preview.addView(it)
                }
            } else {
                throw Exception()
            }
        } else {
            throw Exception()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        camctl.interrupt()
    }
}

fun createCameraActivity(ctx: Context, cmdSrc: ICameraCommandSource, camVersion : Int) {
    // Egyedi kulcs
    val id = UUID.randomUUID().toString()
    // Berakod global hashmap-be
    gCommandSourceStorage[id] = cmdSrc

    // Activity Intent
    val intent = Intent(ctx, CameraActivity::class.java).apply {
        // Berakod az egyedi kulcsot mint extra
        // (aztan remenykedsz, hogy az activty majd onCreate-ben kiveszi
        // az entry-t a mapbol, mert kulonben leak lesz)
        putExtra(CameraActivity.EXTRA_ID, id)

        putExtra(CameraActivity.EXTRA_CAMERA_VER, camVersion)
    }
    ctx.startActivity(intent)
}