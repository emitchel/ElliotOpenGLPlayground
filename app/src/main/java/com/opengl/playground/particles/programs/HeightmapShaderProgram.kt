package com.opengl.playground.particles.programs

import android.content.Context
import android.opengl.GLES20
import com.opengl.playground.R
import com.opengl.playground.programs.ShaderProgram

class HeightmapShaderProgram(context: Context) :
    ShaderProgram(
        context, R.raw.heightmap_vertex_shader,
        R.raw.heightmap_fragment_shader
    ) {
    private val uMatrixLocation: Int = GLES20.glGetUniformLocation(program, U_MATRIX)
    val positionAttributeLocation: Int = GLES20.glGetAttribLocation(program, A_POSITION)

    fun setUniforms(matrix: FloatArray?) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }
}
