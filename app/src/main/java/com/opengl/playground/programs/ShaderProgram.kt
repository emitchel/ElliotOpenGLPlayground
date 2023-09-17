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
import android.opengl.GLES20.glUseProgram
import com.opengl.playground.util.ShaderHelper.buildProgram
import com.opengl.playground.util.TextResourceReader.readTextFileFromResource

abstract class ShaderProgram protected constructor(
    context: Context?, vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {
    // Shader program
    val program: Int

    init {
        // Compile the shaders and link the program.
        program = buildProgram(
            readTextFileFromResource(context!!, vertexShaderResourceId),
            readTextFileFromResource(context, fragmentShaderResourceId)
        )
    }

    fun useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program)
    }

    companion object {
        // Uniform constants
        const val U_MATRIX = "u_Matrix"
        const val U_COLOR = "u_Color"
        const val U_TEXTURE_UNIT = "u_TextureUnit"
        const val U_MASK_TEXTURE_UNIT= "u_MaskTexture"
        const val U_TIME = "u_Time"
        const val U_VECTOR_TO_LIGHT = "u_VectorToLight"
        const val U_MV_MATRIX = "u_MVMatrix"
        const val U_IT_MV_MATRIX = "u_IT_MVMatrix"
        const val U_MVP_MATRIX = "u_MVPMatrix"
        const val U_POINT_LIGHT_POSITIONS = "u_PointLightPositions"
        const val U_POINT_LIGHT_COLORS = "u_PointLightColors"

        // Attribute constants
        const val A_POSITION = "a_Position"
        const val A_COLOR = "a_Color"
        const val A_NORMAL = "a_Normal"
        const val A_TEXTURE_COORDINATES = "a_TextureCoordinates"
        const val A_DIRECTION_VECTOR = "a_DirectionVector"
        const val A_PARTICLE_START_TIME = "a_ParticleStartTime"
    }
}
