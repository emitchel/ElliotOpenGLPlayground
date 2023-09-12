package com.opengl.playground.util

import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glGetProgramInfoLog
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderInfoLog
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glValidateProgram

object ShaderHelper {

    fun validateProgram(programObjectId: Int): Boolean {
        glValidateProgram(programObjectId)

        val validateStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_LINK_STATUS, validateStatus, 0)
        log("Results of validating program: ${validateStatus[0]}\nLog: ${glGetProgramInfoLog(programObjectId)}")
        return validateStatus[0] != 0
    }

    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val programObjectId = glCreateProgram()
        if (programObjectId == 0) {
            log("Could not create new program")
            return 0
        }

        glAttachShader(programObjectId, vertexShaderId)
        glAttachShader(programObjectId, fragmentShaderId)
        glLinkProgram(programObjectId)

        val linkStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)
        log("Results of linking program: \n${glGetProgramInfoLog(programObjectId)}")
        if (linkStatus.first() == 0) {
            glDeleteProgram(programObjectId)
            log("Linking of program failed")
            return 0
        }
        return programObjectId
    }

    fun compileVertexShader(shaderCode: String): Int {
        return compileShader(GL_VERTEX_SHADER, shaderCode)
    }

    fun compileFragmentShader(shaderCode: String): Int {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode)
    }

    private fun compileShader(type: Int, shaderCode: String): Int {
        // create a new gl object
        val shaderObjectId = glCreateShader(type)

        if (shaderObjectId == 0) {
            log("Could not create new shader")

            return 0
        }

        // upload the source code
        glShaderSource(shaderObjectId, shaderCode)

        // compile source code
        glCompileShader(shaderObjectId)

        // check error status
        val compileStatus = IntArray(1)
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)

        log("Results of compiling source: \n$shaderCode\n: ${glGetShaderInfoLog(shaderObjectId)}")

        if (compileStatus.first() == 0) {
            glDeleteShader(shaderObjectId)
            log("Compilation of shader failed")
            return 0
        }

        return shaderObjectId
    }
}