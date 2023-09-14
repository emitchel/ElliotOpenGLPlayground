package com.opengl.playground.particles.programs

import android.content.Context
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1f
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import com.opengl.playground.R
import com.opengl.playground.programs.ShaderProgram

class ParticleShaderProgram(val context: Context) :
    ShaderProgram(context, R.raw.particle_vertex_shader, R.raw.particle_fragment_shader) {

    // Uniform locations
    var uMatrixLocation = 0
    var uTimeLocation = 0

    // Attribute locations
    var aPositionLocation = 0
    var aColorLocation = 0
    var aDirectionVectorLocation = 0
    var aParticleStartTimeLocation = 0
    var uTextureUnitLocation = 0

    init {

        // Retrieve uniform locations for the shader program.
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
        uTimeLocation = glGetUniformLocation(program, U_TIME)
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT)

        // Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION)
        aColorLocation = glGetAttribLocation(program, A_COLOR)
        aDirectionVectorLocation = glGetAttribLocation(program, A_DIRECTION_VECTOR)
        aParticleStartTimeLocation = glGetAttribLocation(program, A_PARTICLE_START_TIME)
    }

    /*
    public void setUniforms(float[] matrix, float elapsedTime) {
     */
    fun setUniforms(matrix: FloatArray?, elapsedTime: Float, textureId: Int) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glUniform1f(uTimeLocation, elapsedTime)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glUniform1i(uTextureUnitLocation, 0)
    }

    fun getPositionAttributeLocation(): Int {
        return aPositionLocation
    }

    fun getColorAttributeLocation(): Int {
        return aColorLocation
    }

    fun getDirectionVectorAttributeLocation(): Int {
        return aDirectionVectorLocation
    }

    fun getParticleStartTimeAttributeLocation(): Int {
        return aParticleStartTimeLocation
    }
}