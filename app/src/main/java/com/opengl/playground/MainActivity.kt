package com.opengl.playground

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val glSurfaceView: GLSurfaceView by lazy {
        findViewById(R.id.glSurfaceView)
    }

    private var rendererSet = false

    private val renderer by lazy {
        PlaygroundRenderer(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //request an opengl es 2.0 compatible context
        glSurfaceView.setEGLContextClientVersion(2)

        //assign our renderer
        glSurfaceView.setRenderer(renderer)

        glSurfaceView.setOnTouchListener { v, event ->
            if (event != null) {
                // Convert touch coordinates into normalized device
                // coordinates, keeping in mind that Android's Y
                // coordinates are inverted.
                val normalizedX = event.x / v.width.toFloat() * 2 - 1
                val normalizedY = -(event.y / v.height.toFloat() * 2 - 1)
                if (event.action == MotionEvent.ACTION_DOWN) {
                    glSurfaceView.queueEvent {
                        renderer.handleTouchPress(
                            normalizedX, normalizedY
                        )
                    }
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    glSurfaceView.queueEvent {
                        renderer.handleTouchDrag(
                            normalizedX, normalizedY
                        )
                    }
                }
                true
            } else {
                false
            }
        }
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