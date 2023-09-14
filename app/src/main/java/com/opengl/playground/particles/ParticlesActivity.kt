package com.opengl.playground.particles

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.opengl.playground.R

class ParticlesActivity : AppCompatActivity() {

    private val glSurfaceView: GLSurfaceView by lazy {
        findViewById(R.id.glSurfaceView)
    }

    private var rendererSet = false

    private val renderer by lazy {
        ParticlesRenderer(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //request an opengl es 2.0 compatible context
        glSurfaceView.setEGLContextClientVersion(2)

        //assign our renderer
        glSurfaceView.setRenderer(renderer)
        rendererSet = true
    }

    override fun onPause() {
        super.onPause()
        if (rendererSet) {
            glSurfaceView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (rendererSet) {
            glSurfaceView.onResume()
        }
    }
}