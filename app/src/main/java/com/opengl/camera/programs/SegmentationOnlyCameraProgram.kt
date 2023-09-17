package com.opengl.camera.programs

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Size
import android.view.Surface
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

@ExperimentalGetImage
class SegmentationOnlyCameraProgram(
    val context: Context,
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
    private var maskTextureId = 0
    private var surfaceTexture: SurfaceTexture? = null
    private var width: Int = 0
    private var height: Int = 0
    private var latestSegmentationMask: SegmentationMask? = null

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

        val initialFloats = FloatArray((1080 * 1920)) { 0f }
        val initialByteBuffer = initialFloats.toByteBuffer()
        initialByteBuffer.position(0)
        maskTextureId =
            TextureHelper.createTextureFromByteBuffer(
                initialByteBuffer, 1080, 1920
            )
        error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            throw RuntimeException("OpenGL error1: $error")
        }

        log("elliot!! bytebuffer initial size = ${initialByteBuffer.capacity()}")
    }

    fun FloatArray.toByteBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(size * 4) // 4 bytes per float
        buffer.order(ByteOrder.nativeOrder()).asFloatBuffer().put(this)
        buffer.position(0)  // Reset position
        return buffer
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
                        segmentationMaskToTexture(result)
                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        log("elliot!! failed to segment image")
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
                log("elliot!! CameraX use case binding failed $exc")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun segmentationMaskToTexture(segmentationMask: SegmentationMask) {
        latestSegmentationMask = segmentationMask
        // log(
        //     "bytebuffer fresh float size size = ${
        //         segmentationMask.buffer.asFloatBuffer().capacity()
        //     }"
        // )
        // GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, maskTextureId)
        // GLES20.glTexSubImage2D(
        //     GLES20.GL_TEXTURE_2D,
        //     0,
        //     0,
        //     0,
        //     segmentationMask.width,
        //     segmentationMask.height,
        //     GLES20.GL_LUMINANCE,
        //     GLES20.GL_FLOAT,
        //     segmentationMask.buffer
        // )
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        startCamera()
    }

    private
    val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

    private val modelMatrix = FloatArray(16)

    // called onDrawFrame from GLSurfaceView.Renderer
    override fun onDrawFrame() {
        if (latestSegmentationMask == null) {
            // log("Not drawing yet because no segmentation!")
            return
        }
        // set positioning... i think
        positionFrameCorrectly()
        // setup the shaders to run
        GLES20.glUseProgram(program)

        surfaceTexture?.updateTexImage()
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

// Set the active texture unit to texture unit 1 for the segmentation mask.
        latestSegmentationMask!!.buffer.position(0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, maskTextureId)
        GLES20.glTexSubImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            0,
            0,
            latestSegmentationMask!!.width,
            latestSegmentationMask!!.height,
            GLES20.GL_LUMINANCE,
            GLES20.GL_UNSIGNED_BYTE,
            latestSegmentationMask!!.buffer
        )
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
    }

    private fun positionFrameCorrectly() {
        Matrix.setIdentityM(modelMatrix, 0)

        // As far as I can tell, the camera preview always comes in sideways.
        // Every app who manages their own surface texture has to rotate it and set the scale.

        val cameraAspectRatio = 1080f / 1920f   // 0.5625
        val viewportAspectRatio = width.toFloat() / height.toFloat()
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