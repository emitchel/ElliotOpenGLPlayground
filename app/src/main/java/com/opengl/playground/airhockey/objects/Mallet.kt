/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 */
package com.opengl.playground.airhockey.objects

import com.opengl.playground.objects.VertexArray
import com.opengl.playground.programs.ColorShaderProgram
import com.opengl.playground.util.DrawCommand
import com.opengl.playground.util.ObjectBuilder
import com.opengl.playground.util.Point

class Mallet(val radius: Float, val height: Float, numPointsAroundPuck: Int) {
    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
    }

    private var vertexArray: VertexArray
    private var drawList: List<DrawCommand>

    init {
        val generatedObjectData = ObjectBuilder.createMallet(
            Point(0f, 0f, 0f),
            radius,
            height,
            numPointsAroundPuck
        )

        vertexArray = VertexArray(generatedObjectData.vertexData)
        drawList = generatedObjectData.drawList
    }

    fun bindData(colorShaderProgram: ColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            colorShaderProgram.positionAttributeLocation,
            POSITION_COMPONENT_COUNT,
            0
        )
    }

    fun draw() {
        for (drawCommand in drawList) {
            drawCommand.draw()
        }
    }
}