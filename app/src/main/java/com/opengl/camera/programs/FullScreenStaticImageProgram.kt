package com.opengl.camera.programs

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.Matrix
import com.opengl.camera.CameraActivity
import com.opengl.playground.R
import com.opengl.playground.airhockey.AirHockeyRenderer
import com.opengl.playground.objects.VertexArray
import com.opengl.playground.programs.TextureShaderProgram
import com.opengl.playground.util.TextureHelper

class FullScreenStaticImageProgram(
    val context: Context,
    private val drawableInt: Int
) : TextureShaderProgram(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader),
    CameraActivity.CanvasRendererLayer {

    private var textureId = 0
    private var width: Int = 0
    private var height: Int = 0

    private val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

    private val modelMatrix = FloatArray(16)
    override fun onSurfaceCreated() {
        textureId = TextureHelper.loadTexture(context, drawableInt)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun onDrawFrame() {
        // set positioning... i think
        positionContent()
        // setup the shaders to run
        GLES20.glUseProgram(program)

        // Bind the camera texture
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(getuMatrixLocation(), 1, false, modelMatrix, 0)
        // GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GL_TEXTURE_2D, textureId)

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        GLES20.glUniform1i(getuTextureUnitLocation(), 0)

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

    private fun positionContent() {
        Matrix.setIdentityM(modelMatrix, 0)

        // As far as I can tell, the camera preview always comes in sideways.
        // Every app who manages their own surface texture has to rotate it and set the scale.

        val cameraAspectRatio = 1080f / 1920f   // 0.5625
        val viewportAspectRatio = width.toFloat() / height.toFloat()
        // if (viewportAspectRatio > cameraAspectRatio) {
        //     // The viewport is wider than the camera feed.
        //     // We scale up in the Y direction to fill and crop.
        //     val scale = viewportAspectRatio / cameraAspectRatio
        //     Matrix.scaleM(modelMatrix, 0, 1f, scale, 1f)
        // } else {
        //     // The viewport is taller than the camera feed.
        //     // We scale up in the X direction to fill and crop.
        //     val scale = cameraAspectRatio / viewportAspectRatio
        //     Matrix.scaleM(modelMatrix, 0, scale, 1f, 1f)
        // }
        // Matrix.rotateM(modelMatrix, 0, 90f, 0f, 0f, 1f)
        // val mirrorMatrix = floatArrayOf(
        //     -1f, 0f, 0f, 0f,
        //     0f, 1f, 0f, 0f,
        //     0f, 0f, 1f, 0f,
        //     0f, 0f, 0f, 1f
        // )
        //
        // Matrix.multiplyMM(modelMatrix, 0, mirrorMatrix, 0, modelMatrix, 0)
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