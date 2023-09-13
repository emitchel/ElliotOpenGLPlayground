package com.opengl.playground.objects

import com.opengl.playground.VertexArray
import com.opengl.playground.programs.ColorShaderProgram
import com.opengl.playground.util.Cylinder
import com.opengl.playground.util.Point

class Puck(val radius: Float, val height: Float, numPointsAroundPuck: Int) {
    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
    }

    private lateinit var vertexArray: VertexArray
    private lateinit var drawList: List<DrawCommand>

    init {
        val generatedObjectData = ObjectBuilder.createPuck(
            Cylinder(
                Point(0f, 0f, 0f),
                radius,
                height
            ),
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