package com.opengl.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.opengl.playground.R
import com.opengl.playground.airhockey.AirHockeyRenderer
import com.opengl.playground.objects.VertexArray
import com.opengl.playground.programs.TextureShaderProgram
import com.opengl.playground.util.TextureHelper

class CameraProgram(val context: Context) :
    TextureShaderProgram(context, R.raw.camera_vertex_shader, R.raw.camera_fragment_shader) {

    private var textureId = 0
    private var surfaceTexture: SurfaceTexture? = null
    fun onSurfaceCreated(glSurfaceView: GLSurfaceView) {
        textureId = TextureHelper.createTexture()
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture?.setOnFrameAvailableListener {
            glSurfaceView.requestRender()
        }
    }

    private val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

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
    fun onDrawFrame() {
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