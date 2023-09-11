package com.opengl.playground

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val glSurfaceView: GLSurfaceView by lazy {
        findViewById(R.id.glSurfaceView)
    }

    private var rendererSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //request an opengl es 2.0 compatible context
        glSurfaceView.setEGLContextClientVersion(2)

        //assign our renderer
        glSurfaceView.setRenderer(PlaygroundRenderer())

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