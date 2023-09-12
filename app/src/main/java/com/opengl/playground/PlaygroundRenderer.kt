package com.opengl.playground

import android.content.Context
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_POINTS
import android.opengl.GLES20.GL_TRIANGLE_FAN
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glLineWidth
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix.orthoM
import com.opengl.playground.util.ShaderHelper
import com.opengl.playground.util.TextResourceReader
import com.opengl.playground.util.log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PlaygroundRenderer(val context: Context) : Renderer {
    companion object {
        // x,y coordinate components
        private const val BYTES_PER_FLOAT = 4

        // 2 position components
        private const val POSITION_COMPONENT_COUNT = 4

        // 3 color components
        private const val COLOR_COMPONENT_COUNT = 3

        private const val A_POSITION = "a_Position"
        private const val A_COLOR = "a_Color"
        private const val U_MATRIX = "u_Matrix"
        private const val STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }

    private var program: Int = 0
    private var aColorLocation: Int = 0
    private var aPositionLocation: Int = 0
    private var uMatrixLocation: Int = 0

    private var projectionMatrix = FloatArray(16)

    private val tableVertices = floatArrayOf(
        // Order of coordinates: X, Y, Z, W, R, G, B

        // Triangle Fan
           0f,    0f, 0f, 1.5f,   1f,   1f,   1f,
        -0.5f, -0.8f, 0f,   1f, 0.7f, 0.7f, 0.7f, // left bottom
         0.5f, -0.8f, 0f,   1f, 0.7f, 0.7f, 0.7f, // right bottom
         0.5f,  0.8f, 0f,   2f, 0.7f, 0.7f, 0.7f, // right top
        -0.5f,  0.8f, 0f,   2f, 0.7f, 0.7f, 0.7f, // left top
        -0.5f, -0.8f, 0f,   1f, 0.7f, 0.7f, 0.7f, // left bottom

        // Line 1
        -0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f, // left middle
         0.5f, 0f, 0f, 1.5f, 1f, 0f, 0f, // right middle

        // Mallets
        0f, -0.4f, 0f, 1.25f, 0f, 0f, 1f, // blue mallet
        0f,  0.4f, 0f, 1.75f, 1f, 0f, 0f // red mallet
    )

    // Used to store data in native memory for use by OpenGL
    private val vertexData: FloatBuffer =
        ByteBuffer.allocateDirect(tableVertices.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(tableVertices)

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        log("onSurfaceCreated")
        //
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        val vertexShaderCode =
            TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader)
        val fragmentShaderCode =
            TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader)
        val vertexShader = ShaderHelper.compileVertexShader(vertexShaderCode)
        val fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderCode)
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader)
        ShaderHelper.validateProgram(program)
        glUseProgram(program)

        aColorLocation = glGetAttribLocation(program, A_COLOR).also {
            log("aColorLocation: $it")
        }

        aPositionLocation = glGetAttribLocation(program, A_POSITION).also {
            log("aPositionLocation: $it")
        }

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX).also {
            log("uMatrixLocation: $it")
        }

        // important
        vertexData.position(0)
        // tell OpenGL to read in vertex data from vertexData
        glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT,
            GL10.GL_FLOAT,
            false,
            STRIDE,
            vertexData
        )

        // must enable the vertex attribute array
        glEnableVertexAttribArray(aPositionLocation)

        // now updating position to point to the color data
        vertexData.position(POSITION_COMPONENT_COUNT)

        glVertexAttribPointer(
            aColorLocation,
            COLOR_COMPONENT_COUNT,
            GL10.GL_FLOAT,
            false,
            STRIDE,
            vertexData
        )
        glEnableVertexAttribArray(aColorLocation)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        log("onSurfaceChanged, width: $width, height: $height")
        glViewport(0, 0, width, height)
        val aspectRatio = if (width > height) {
            width.toFloat() / height.toFloat()
        } else {
            height.toFloat() / width.toFloat()
        }

        log("aspectRatio: $aspectRatio")

        // redfine our coordinate space so it's not -1,1 everywhere, it's -aspectRatio, aspectRatio
        if (width > height) {
            // landscape
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            // portrait
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }
    }

    override fun onDrawFrame(p0: GL10?) {
        // log("onDrawFrame")
        // clears all extra colors, resets to red only onSurfaceCreated
        glClear(GL_COLOR_BUFFER_BIT)

        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0)

        // draw the WHITE table
        glDrawArrays(
            GL_TRIANGLE_FAN, // TRIANGLES
            0, // start at the beginning of the array
            6 // draw 6 vertices, which is two triangles in our data
        )

        // draw the RED dividing line
        glDrawArrays(
            GL10.GL_LINES, // LINES
            6, // start at index 6 (after the first 6 vertices)
            2 // draw 2 vertices, which is one line in our data
        )
        glLineWidth(10.0f)

        // draw the first BLUE mallet
        glDrawArrays(GL_POINTS, 8, 1)//skip the first vertices, draw the next one

        //draw the second RED MALLET
        glDrawArrays(GL_POINTS, 9, 1)//skip the first vertices, draw the next one
    }
}