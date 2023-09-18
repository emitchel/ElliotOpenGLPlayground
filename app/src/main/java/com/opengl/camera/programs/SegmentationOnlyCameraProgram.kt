package com.opengl.camera.programs

import android.content.Context
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Size
import android.view.Surface
import androidx.annotation.ColorInt
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.opengl.camera.CameraActivity
import com.opengl.playground.R
import com.opengl.playground.airhockey.AirHockeyRenderer
import com.opengl.playground.objects.VertexArray
import com.opengl.playground.programs.TextureShaderProgram
import com.opengl.playground.util.TextureHelper
import com.opengl.playground.util.log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@ExperimentalGetImage
class SegmentationOnlyCameraProgram(
    val context: Context,
    private val coroutineScope: CoroutineScope,
    private val lifecycleOwner: LifecycleOwner,
    private val glSurfaceView: GLSurfaceView
) :
    TextureShaderProgram(
        context,
        R.raw.camera_segmentation_vertex_shader,
        R.raw.camera_segmentation_fragment_shader
    ),
    CameraActivity.CanvasRendererLayer {

    private var cameraTextureId = 0
    private var maskTextureId = -1
    private var surfaceTexture: SurfaceTexture? = null
    private var viewPortWidth: Int = 0
    private var viewPortHeight: Int = 0

    private var byteBuffer: ByteBuffer? = null
    private var byteBufferWidth: Int = 0
    private var byteBufferHeight: Int = 0

    override fun onSurfaceCreated() {
        cameraTextureId = TextureHelper.createExternalOesTexture()
        var error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException("OpenGL error1: $error")
        }
        surfaceTexture = SurfaceTexture(cameraTextureId)
        surfaceTexture?.setOnFrameAvailableListener {
            // TODO NOTE: this may not be needed if we do continuous rendering
            glSurfaceView.requestRender()
        }
    }

    fun FloatArray.toByteBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(size * 4) // 4 bytes per float
        buffer.order(ByteOrder.nativeOrder()).asFloatBuffer().put(this)
        buffer.position(0)  // Reset position
        return buffer
    }

    fun segmentMaskCallback(it: SegmentationMask) {
        coroutineScope.launch(Dispatchers.IO) {
            val maskWidth = it.getWidth()
            val maskHeight = it.getHeight()

            // The size of the buffer remains the same

            val buffer = ByteBuffer.allocateDirect(maskWidth * maskHeight * 4) // 4 bytes for RGBA

            for (i in 0 until maskWidth * maskHeight) {
                val backgroundLikelihood = 1 - it.buffer.float
                @ColorInt val color: Int

                if (backgroundLikelihood > 0.9) {
                    color = Color.argb(128, 255, 0, 255)
                } else if (backgroundLikelihood > 0.2) {
                    val alpha = (182.9 * backgroundLikelihood - 36.6 + 0.5).toInt()
                    color = Color.argb(alpha, 255, 0, 255)
                } else {
                    color = Color.TRANSPARENT
                }
                buffer.putInt(color)
            }
            buffer.flip()
            updateMaskData(buffer, maskWidth, maskHeight)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val cameraSelector =
                CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()
            val preview =
                Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()

            preview.setSurfaceProvider { surfaceRequest ->
                val executor = ContextCompat.getMainExecutor(context)
                // This sets the resolution!!!
                surfaceTexture!!.setDefaultBufferSize(
                    // RESOLUTION, IF NOT SET IT'S DEFAULT TO 640x480
                    surfaceRequest.resolution.width,
                    surfaceRequest.resolution.height
                )
                surfaceRequest.provideSurface(Surface(surfaceTexture), executor) {
                    it.surface.release()
                }
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1080, 1920))
                .build()

            val segmenter = CameraSegmenter(context)
            imageAnalysis.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(context)
            ) { imageProxy: ImageProxy ->

                // val originalImage = BitmapUtils.getBitmap(imageProxy)
                segmenter.detectInImage(
                    InputImage.fromMediaImage(
                        imageProxy.image!!,
                        imageProxy.imageInfo.rotationDegrees
                    )
                )
                    .addOnSuccessListener { result ->
                        // log("result height = ${result.height} width = ${result.width}")
                        segmentMaskCallback(result)

                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        log("failed to segment image")
                        imageProxy.close()
                    }
            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (exc: Exception) {
                log("CameraX use case binding failed $exc")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private val lock = Any()
    fun updateMaskData(byteBuffer: ByteBuffer, width: Int, height: Int) {

        // synchronized(lock) {
            this.byteBuffer = byteBuffer
            this.byteBufferHeight = height
            this.byteBufferWidth = width
        // }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.viewPortWidth = width
        this.viewPortHeight = height
        startCamera()
    }

    private
    val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

    private val modelMatrix = FloatArray(16)

    override fun onDrawFrame() {

        // log("pulled latest mask, size: ${masks.size}")
        // set positioning... i think
        positionCameraCorrectly()
        // setup the shaders to run
        GLES20.glUseProgram(program)

        surfaceTexture?.updateTexImage()
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // 1. Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        // 2. Set the blend function
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // Bind the camera texture
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(getuMatrixLocation(), 1, false, modelMatrix, 0)
        // Set the active texture unit to texture unit 0 for camera feed.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId);
        GLES20.glUniform1i(
            getuTextureUnitLocation(),
            0
        );

        // bind the triangle + texture data
        vertexArray.setVertexAttribPointer(
            0,
            getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            STRIDE
        )
        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            getTextureCoordinatesAttributeLocation(),
            TEXTURE_COORDINATES_COMPONENT_COUNT,
            STRIDE
        )

        //Draw right away for the camera positioning
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)



        // Don't draw the segmentation mask if it hasn't been updated yet.
        if (byteBuffer == null) {
            return
        }
        // synchronized(lock) {
        if (maskTextureId == -1) {
            maskTextureId =
                TextureHelper.createTextureFromColors(
                    byteBuffer!!,
                    byteBufferWidth,
                    byteBufferHeight
                )
        }

        // TODO update the matrix before drawing the mask
//             // ... previous code ...
//
// // After drawing the first texture
//             GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)
//
// // Update the modelMatrix for the second texture
// // ... your logic to update modelMatrix ...
//
// // Pass the updated matrix into the shader program.
//             GLES20.glUniformMatrix4fv(getuMatrixLocation(), 1, false, modelMatrix, 0)
//
// // Bind your second texture and set it up
// // ... your code to bind and set up second texture ...
//
// // Draw the second texture
//             GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)
//
//             GLES20.glDisable(GLES20.GL_BLEND);

// Set the active texture unit to texture unit 1 for the segmentation mask.
        byteBuffer!!.flip()
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(
            GLES20.GL_TEXTURE_2D,
            maskTextureId
        )

        GLES20.glTexSubImage2D(
            GLES20.GL_TEXTURE_2D, 0, 0, 0,
            byteBufferWidth, byteBufferHeight, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, byteBuffer!!
        )
        // }
        GLES20.glUniform1i(getuMaskTextureUnitLocation(), 1)

        // bind the triangle + texture data
        vertexArray.setVertexAttribPointer(
            0,
            getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            STRIDE
        )
        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            getTextureCoordinatesAttributeLocation(),
            TEXTURE_COORDINATES_COMPONENT_COUNT,
            STRIDE
        )

        // lastly, draw the vertices...
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private fun positionCameraCorrectly() {
        Matrix.setIdentityM(modelMatrix, 0)
        // As far as I can tell, the camera preview always comes in sideways.
        // Every app who manages their own surface texture has to rotate it and set the scale.

        val cameraAspectRatio = 1080f / 1920f   // 0.5625
        val viewportAspectRatio = viewPortWidth.toFloat() / viewPortHeight.toFloat()
        if (viewportAspectRatio > cameraAspectRatio) {
            // The viewport is wider than the camera feed.
            // We scale up in the Y direction to fill and crop.
            val scale = viewportAspectRatio / cameraAspectRatio
            Matrix.scaleM(modelMatrix, 0, 1f, scale, 1f)
        } else {
            // The viewport is taller than the camera feed.
            // We scale up in the X direction to fill and crop.
            val scale = cameraAspectRatio / viewportAspectRatio
            Matrix.scaleM(modelMatrix, 0, scale, 1f, 1f)
        }
        Matrix.rotateM(modelMatrix, 0, 90f, 0f, 0f, 1f)
        val mirrorMatrix = floatArrayOf(
            -1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )

        Matrix.multiplyMM(modelMatrix, 0, mirrorMatrix, 0, modelMatrix, 0)

        // TODO use this to scale and move the camera !!!
        Matrix.scaleM(modelMatrix, 0, .5f, .5f, .5f)
    }

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private val STRIDE: Int = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * AirHockeyRenderer.BYTES_PER_FLOAT
        private val VERTEX_DATA = floatArrayOf(
            // Order of coordinates: X, Y, S, T
            -1f, -1f, 0f, 1f,     // Bottom-left
            1f, -1f, 1f, 1f,      // Bottom-right
            1f, 1f, 1f, 0f,       // Top-right
            -1f, 1f, 0f, 0f,      // Top-left
            -1f, -1f, 0f, 1f      // Bottom-left again to close the fan
        )
    }
}