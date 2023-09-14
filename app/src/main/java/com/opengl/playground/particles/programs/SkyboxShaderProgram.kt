package com.opengl.playground.particles.programs

import android.content.Context
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import com.opengl.playground.R
import com.opengl.playground.programs.ShaderProgram

class SkyboxShaderProgram(context: Context) :
    ShaderProgram(
        context, R.raw.skybox_vertex_shader,
        R.raw.skybox_fragment_shader
    ) {
    private val uMatrixLocation: Int = glGetUniformLocation(program, U_MATRIX)
    private val uTextureUnitLocation: Int = glGetUniformLocation(program, U_TEXTURE_UNIT)
     val positionAttributeLocation: Int = glGetAttribLocation(program, A_POSITION)

    fun setUniforms(matrix: FloatArray?, textureId: Int) {
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureId)
        glUniform1i(uTextureUnitLocation, 0)
    }
}
