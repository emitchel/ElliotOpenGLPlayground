package com.opengl.camera

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.segmentation.SegmentationMask
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

    /**
     * TODO
     * 1. Must use SurfaceView with SurfaceHolder to initialize
     * 2. Camera must launch on SurfaceTexture (or find another way to launch onto it)
     * 3. Must be able to draw a texture from an image on the SurfaceView
     * 4. Must be able to take the SurfaceTexture and apply same frames on SurfaceView
     */

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
                SegmentationOnlyCameraProgram(this, lifecycleScope, this, glSurfaceView!!)
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