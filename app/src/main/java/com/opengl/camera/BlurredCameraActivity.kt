package com.opengl.camera

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.opengl.camera.programs.BlurredCamera
import com.opengl.playground.R

class BlurredCameraActivity : AppCompatActivity() {

    // private val textureView: TextureView by lazy {
    //     findViewById(R.id.textureView)
    // }

    private var glSurfaceView: GLSurfaceView? = null

    private val mediaPlayer get() = MediaPlayer()

    private val blurredCamera by lazy {
        BlurredCamera(this, lifecycleScope, this, glSurfaceView!!)
    }

    private var rendererSet = false
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            //TODO fallback to failed permissions
            startRendering()
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_view)

        requestPermissions()
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private fun startRendering() {
        glSurfaceView = GLSurfaceView(this)
        //request an opengl es 2.0 compatible context
        glSurfaceView?.setEGLContextClientVersion(3)

        val renderer = RecordedCanvasRenderer {
            listOf(
                blurredCamera,
            )
        }
        glSurfaceView?.setRenderer(renderer)
        glSurfaceView?.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            glSurfaceView?.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            glSurfaceView?.onResume()
        }
    }
}