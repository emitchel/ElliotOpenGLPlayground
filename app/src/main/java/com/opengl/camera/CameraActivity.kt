package com.opengl.camera

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.lifecycle.lifecycleScope
import com.opengl.camera.programs.ByteBufferMaskProgram
import com.opengl.camera.programs.CameraProgram
import com.opengl.camera.programs.FullScreenStaticImageProgram
import com.opengl.camera.programs.SegmentationOnlyCameraProgram
import com.opengl.playground.R
import java.nio.ByteBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalGetImage
class CameraActivity : AppCompatActivity() {

    // private val textureView: TextureView by lazy {
    //     findViewById(R.id.textureView)
    // }

    private var glSurfaceView: GLSurfaceView? = null
    private val byteBufferMaskProgram by lazy {
        ByteBufferMaskProgram(this)
    }
    private val cameraProgram by lazy {
        CameraProgram(this, this, glSurfaceView!!) {
            lifecycleScope.launch(Dispatchers.IO) {
                val mask = it.getBuffer()
                val maskWidth = it.getWidth()
                val maskHeight = it.getHeight()

                val byteBufferSize = maskWidth * maskHeight // one byte per pixel for LUMINANCE
                val resultBuffer = ByteBuffer.allocateDirect(byteBufferSize)

                for (y in 0 until maskHeight) {
                    for (x in 0 until maskWidth) {
                        val foregroundConfidence = mask.getFloat()
                        val byteValue = (foregroundConfidence * 255.0f).toInt().toByte()
                        resultBuffer.put(byteValue)
                    }
                }

                resultBuffer.rewind()
                byteBufferMaskProgram.updateMaskData(resultBuffer, maskWidth, maskHeight)
            }
        }
    }

    private val fullScreenStaticImageProgram by lazy {
        FullScreenStaticImageProgram(this, R.drawable.article)
    }

    private val segmentedCameraProgram by lazy {
        SegmentationOnlyCameraProgram(this, lifecycleScope, this, glSurfaceView!!)
    }

    private var segmentationOnlyCameraProgram: SegmentationOnlyCameraProgram? = null

    interface CanvasRendererLayer {
        fun onSurfaceCreated()
        fun onSurfaceChanged(width: Int, height: Int)
        fun onDrawFrame()
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

    var scaleGestureDetector:
        ScaleGestureDetector? = null
    var gestureDetector: GestureDetector? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_texture_view)

        textureViewApproach()

        activityResultLauncher.launch(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )


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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector?.onTouchEvent(event)
        gestureDetector?.onTouchEvent(event)
        return true
    }

    private fun startRendering() {
        glSurfaceView = GLSurfaceView(this)
        //request an opengl es 2.0 compatible context
        glSurfaceView?.setEGLContextClientVersion(2)
        // TODO this might make the surfaceTexture listener redundant
        // glSurfaceView.renderMode = RENDERMODE_CONTINUOUSLY
        //assign our renderer

        val renderer = RecordedCanvasRenderer {
            listOf(
                fullScreenStaticImageProgram,
                segmentedCameraProgram,
                // TODO this works... segmntation only
                // cameraProgram,
                // byteBufferMaskProgram
            )
        }
        glSurfaceView?.setRenderer(renderer)
        setContentView(glSurfaceView)
    }

    private fun textureViewApproach() {
        // TODO tabling the texture view approach for now, it needs all
        //  the overhead of concurrency and setup that GLSurfaceView has
        // val glRender = GLRender(lifecycleScope)
        // textureView.surfaceTextureListener = object : SurfaceTextureListener {
        //     override fun onSurfaceTextureAvailable(p0: SurfaceTexture, w: Int, h: Int) {
        //         log("onSurfaceTextureAvailable $w $h")
        //     }
        //
        //     override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, w: Int, h: Int) {
        //         log("onSurfaceTextureSizeChanged $w $h")
        //     }
        //
        //     override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        //         log("onSurfaceTextureDestroyed")
        //         return true
        //     }
        //
        //     override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
        //         log("onSurfaceTextureUpdated")
        //     }
        // }
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