package com.opengl.playground

import android.content.Context
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_POINTS
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform4f
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
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
        private const val U_COLOR = "u_Color"
        private const val A_POSITION = "a_Position"

        // x,y coordinate components
        private const val POSITION_COMPONENT_COUNT = 2
        private const val BYTES_PER_FLOAT = 4
    }

    private var program: Int = 0
    private var uColorLocation: Int = 0
    private var aPositionLocation: Int = 0

    private val tableVertices = floatArrayOf(
        // TRIANGLE 1
        -0.5f, -0.5f,
        0.5f, 0.5f,
        -0.5f, 0.5f,

        // TRIANGLE 2
        -0.5f, -0.5f,
        0.5f, -0.5f,
        0.5f, 0.5f,

        // LINE 1
        -0.5f, 0f,
        0.5f, 0f,

        // Mallets
        0f, -0.25f,
        0f, 0.25f

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

        uColorLocation = glGetUniformLocation(program, U_COLOR).also {
            log("uColorLocation: $it")
        }

        aPositionLocation = glGetAttribLocation(program, A_POSITION).also {
            log("aPositionLocation: $it")
        }

        // important
        vertexData.position(0)
        // tell OpenGL to read in vertex data from vertexData
        glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT,
            GL10.GL_FLOAT,
            false,
            0,
            vertexData
        )

        // must enable the vertex attribute array
        glEnableVertexAttribArray(aPositionLocation)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        log("onSurfaceChanged, width: $width, height: $height")
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        // log("onDrawFrame")
        // clears all extra colors, resets to red only onSurfaceCreated
        glClear(GL_COLOR_BUFFER_BIT)

        // draw the WHITE table
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f)
        // draw triangle
        glDrawArrays(
            GL_TRIANGLES, // TRIANGLES
            0, // start at the beginning of the array
            6 // draw 6 vertices, which is two triangles in our data
        )

        // draw the RED dividing line
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        glDrawArrays(
            GL10.GL_LINES, // LINES
            6, // start at index 6 (after the first 6 vertices)
            2 // draw 2 vertices, which is one line in our data
        )

        // draw the first BLUE mallet
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f)
        glDrawArrays(GL_POINTS, 8, 1)//skip the first vertices, draw the next one

        //draw the second RED MALLET
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        glDrawArrays(GL_POINTS, 9, 1)//skip the first vertices, draw the next one
    }
}