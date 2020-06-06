package net.easimer.dcctl

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import java.util.*

class CameraActivity : AppCompatActivity() {
    private lateinit var camctl : CameraController
    private var preview : CameraPreview? = null

    companion object {
        val EXTRA_ID = "Id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_activity)
        val extras = intent.extras
        val id = extras?.getString(EXTRA_ID) ?: "uh oh"

        if(gCommandSourceStorage.containsKey(id)) {
            val cmdSrc = gCommandSourceStorage[id]
            gCommandSourceStorage.remove(id)

            if(cmdSrc != null) {
                camctl = CameraController(this, cmdSrc)

                preview = camctl.cam?.let {
                    CameraPreview(this, it)
                }

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

        camctl.thread.interrupt()
    }
}

fun createCameraActivity(ctx: Context, cmdSrc: ICameraCommandSource) {
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
    }
    ctx.startActivity(intent)
}