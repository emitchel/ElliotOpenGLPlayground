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
    context: Context, vertexShaderResourceId: Int,
    fragmentShaderResourceId: Int
) {
    // Uniform constants
    protected val U_MATRIX = "u_Matrix"
    protected val U_COLOR = "u_Color"
    protected val U_TEXTURE_UNIT = "u_TextureUnit"
    protected val U_TIME = "u_Time"

    // Attribute constants
    protected val A_POSITION = "a_Position"
    protected val A_COLOR = "a_Color"
    protected val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

    protected val A_DIRECTION_VECTOR = "a_DirectionVector"
    protected val A_PARTICLE_START_TIME = "a_ParticleStartTime"

    // Shader program
    protected var program = 0

    init {
        // Compile the shaders and link the program.
        program = buildProgram(
            readTextFileFromResource(context, vertexShaderResourceId),
            readTextFileFromResource(context, fragmentShaderResourceId)
        )
    }

    open fun useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program)
    }
}