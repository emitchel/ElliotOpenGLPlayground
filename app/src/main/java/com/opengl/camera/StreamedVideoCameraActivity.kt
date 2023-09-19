package com.opengl.camera

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.opengl.camera.programs.SegmentationOnlyCameraProgram
import com.opengl.camera.programs.VideoStreamProgram
import com.opengl.playground.R

class StreamedVideoCameraActivity : AppCompatActivity() {

    // private val textureView: TextureView by lazy {
    //     findViewById(R.id.textureView)
    // }

    private var glSurfaceView: GLSurfaceView? = null

    private val mediaPlayer get() = MediaPlayer()

    private val videoStreamProgram by lazy {
        VideoStreamProgram(this, glSurfaceView!!, mediaPlayer)
    }

    private val segmentedCameraProgram by lazy {
        SegmentationOnlyCameraProgram(this, lifecycleScope, this, glSurfaceView!!)
    }

    var scaleGestureDetector:
        ScaleGestureDetector? = null
    var gestureDetector: GestureDetector? = null
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

        setupTouchListeners()
    }

    private fun setupTouchListeners() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {

                glSurfaceView?.queueEvent {
                    segmentedCameraProgram.onDrag(distanceX, distanceY)
                }
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {

                glSurfaceView?.queueEvent {
                    segmentedCameraProgram.onShowSegmentationMask(!segmentedCameraProgram.showSegmentation)
                }
                return true
            }
        })

        scaleGestureDetector =
            ScaleGestureDetector(
                this,
                object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    override fun onScale(detector: ScaleGestureDetector): Boolean {
                        // Handle zoom (scale) here.
                        val scaleFactor = detector.scaleFactor
                        glSurfaceView?.queueEvent {
                            segmentedCameraProgram.onZoom(scaleFactor)
                        }
                        return true
                    }
                })
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
        glSurfaceView?.setEGLContextClientVersion(2)

        val renderer = RecordedCanvasRenderer {
            listOf(
                videoStreamProgram,
                segmentedCameraProgram,
            )
        }
        glSurfaceView?.setRenderer(renderer)
        glSurfaceView?.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
        setContentView(glSurfaceView)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector?.onTouchEvent(event)
        gestureDetector?.onTouchEvent(event)
        return true
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