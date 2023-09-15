/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 */
package com.opengl.playground.programs

import android.content.Context
import android.opengl.GLES20
import com.opengl.playground.R

open class TextureShaderProgram(
    context: Context,
    vertexShader: Int = R.raw.texture_vertex_shader,
    fragmentShader: Int = R.raw.texture_fragment_shader
) : ShaderProgram(
    context,
    vertexShader,
    fragmentShader
) {
    // Uniform locations
    fun getuMatrixLocation(): Int = GLES20.glGetUniformLocation(program, U_MATRIX)
    fun getuTextureUnitLocation(): Int = GLES20.glGetUniformLocation(program, U_TEXTURE_UNIT)

    // Attribute locations
    fun getPositionAttributeLocation(): Int = GLES20.glGetAttribLocation(program, A_POSITION)
    fun getTextureCoordinatesAttributeLocation(): Int =
        GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES)

    fun setUniforms(matrix: FloatArray?, textureId: Int) {
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(getuMatrixLocation(), 1, false, matrix, 0)

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        GLES20.glUniform1i(getuTextureUnitLocation(), 0)
    }
}