package com.opengl.camera.programs

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import com.opengl.camera.CanvasRendererLayer
import com.opengl.playground.R
import com.opengl.playground.airhockey.AirHockeyRenderer
import com.opengl.playground.objects.VertexArray
import com.opengl.playground.programs.TextureShaderProgram
import com.opengl.playground.util.TextureHelper
import java.nio.ByteBuffer

class ByteBufferMaskProgram(
    val context: Context
) : TextureShaderProgram(context, R.raw.bytebuffer_vertex_shader, R.raw.bytebuffer_fragment_shader),
    CanvasRendererLayer {

    private var textureId = -1
    private var width: Int = 0
    private var height: Int = 0

    private var byteBuffer: ByteBuffer? = null
    private var byteBufferWidth: Int = 0
    private var byteBufferHeight: Int = 0

    private val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

    private val modelMatrix = FloatArray(16)
    private val lock = Any()
    fun updateMaskData(byteBuffer: ByteBuffer, width: Int, height: Int) {

        synchronized(lock) {
            this.byteBuffer = byteBuffer
            this.byteBufferHeight = height
            this.byteBufferWidth = width
        }
    }

    override fun onSurfaceCreated() {
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun createCheckerboardTexture(size: Int, blockSize: Int): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(size * size) // One byte per pixel for LUMINANCE
        for (y in 0 until size) {
            for (x in 0 until size) {
                val blockX = x / blockSize
                val blockY = y / blockSize
                val isWhite = (blockX + blockY) % 2 == 0
                val color = if (isWhite) 255.toByte() else 0.toByte()
                buffer.put(color)
            }
        }
        buffer.flip() // Prepare the buffer to be read
        return buffer
    }

    val checkerboard = createCheckerboardTexture(256, 32)
    override fun onDrawFrame() {
        synchronized(lock) {
            if (byteBuffer == null) return

            // maskData!!.buffer.rewind()
            if (textureId == -1) {
                textureId = TextureHelper.createTextureFromByteBuffer2(
                    byteBuffer!!,
                    byteBufferWidth,
                    byteBufferHeight
                )

                // textureId = TextureHelper.createTextureFromByteBuffer2(
                //     checkerboard,
                //     256, // TODO these are really important
                //     256
                // )
            }
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
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            val width = byteBufferWidth //TODO this isn't really important 256
            val height = byteBufferHeight//TODO this isn't really important 256
            val buffer = byteBuffer // checkerboard
            GLES20.glTexSubImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                0, 0,
                width,
                height,
                GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE,
                buffer
            )

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
    }

    private fun positionContent() {
        Matrix.setIdentityM(modelMatrix, 0)
        // it loads the assets into full frame, so no modifications needed at the moment
        // could maintain it's aspect ratio given the content but we're not doing that now
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