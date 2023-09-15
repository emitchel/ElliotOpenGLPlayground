package com.opengl.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.Surface
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.opengl.playground.R
import com.opengl.playground.airhockey.AirHockeyRenderer
import com.opengl.playground.objects.VertexArray
import com.opengl.playground.programs.TextureShaderProgram
import com.opengl.playground.util.TextureHelper
import com.opengl.playground.util.log

class CameraProgram(
    val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val glSurfaceView: GLSurfaceView
) :
    TextureShaderProgram(context, R.raw.camera_vertex_shader, R.raw.camera_fragment_shader),
    CameraActivity.CanvasRendererLayer {

    private var textureId = 0
    private var surfaceTexture: SurfaceTexture? = null
    override fun onSurfaceCreated() {
        /**
         * Results of linking program: ????
         * Results of compiling source:???
         */
        textureId = TextureHelper.createTexture()
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture?.setOnFrameAvailableListener {
            // TODO NOTE: this may not be needed if we do continuous rendering
            glSurfaceView.requestRender()
        }
        startCamera()
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
                Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()

            preview.setSurfaceProvider(
                ImmediateSurfaceProvider(
                    context,
                    surfaceTexture!!
                )
            )
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    /* lifecycleOwner = */ lifecycleOwner,
                    /* cameraSelector = */ cameraSelector,
                    /* ...useCases = */ preview
                )
            } catch (exc: Exception) {
                log("elliot!! CameraX use case binding failed $exc")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        // TODO("Not yet implemented")
    }

    class ImmediateSurfaceProvider(context: Context, private val surfaceTexture: SurfaceTexture) :
        SurfaceProvider {
        private val executor = ContextCompat.getMainExecutor(context)
        override fun onSurfaceRequested(request: SurfaceRequest) {
            surfaceTexture.setDefaultBufferSize(request.resolution.width, request.resolution.height)
            request.provideSurface(Surface(surfaceTexture), executor) {
                it.surface.release()
            }
        }
    }

    private
    val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

    private fun bindData() {
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
    }

    private val modelMatrix = FloatArray(16)

    // called onDrawFrame from GLSurfaceView.Renderer
    override fun onDrawFrame() {
        // set positioning... i think
        Matrix.setIdentityM(modelMatrix, 0)

        // setup the shaders to run
        useProgram()

        // bind the data to the shaders
        bindData()

        surfaceTexture?.updateTexImage()
        // Bind the camera texture
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(getuMatrixLocation(), 1, false, modelMatrix, 0)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        // NOTE: this is different than set uniforms in TextureShaderProgram on purpose
        // so don't call super.setUniforms
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        GLES20.glUniform1i(getuTextureUnitLocation(), 0)
    }

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private val STRIDE: Int = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * AirHockeyRenderer.BYTES_PER_FLOAT
        private val VERTEX_DATA = floatArrayOf(
            // Order of coordinates: X, Y, S, T
            // Triangle Fan
            // .9 and .1 used for T to clip the image instead of scaling it
            0f, 0f, 0.5f, 0.5f,
            -0.5f, -0.8f, 0f, 0.9f,
            0.5f, -0.8f, 1f, 0.9f,
            0.5f, 0.8f, 1f, 0.1f,
            -0.5f, 0.8f, 0f, 0.1f,
            -0.5f, -0.8f, 0f, 0.9f
        )
    }
}