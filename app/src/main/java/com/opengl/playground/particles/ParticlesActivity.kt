package com.opengl.playground.particles

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
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


        glSurfaceView.setOnTouchListener(object : OnTouchListener {
            var previousX = 0f
            var previousY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                return run {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        previousX = event.x
                        previousY = event.y
                    } else if (event.action == MotionEvent.ACTION_MOVE) {
                        val deltaX = event.x - previousX
                        val deltaY = event.y - previousY
                        previousX = event.x
                        previousY = event.y
                        glSurfaceView.queueEvent {
                            renderer.handleTouchDrag(
                                deltaX, deltaY
                            )
                        }
                    }
                    true
                }
            }
        })
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