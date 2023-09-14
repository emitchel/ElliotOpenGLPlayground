package com.opengl.playground.util

import android.opengl.GLES20
import com.opengl.playground.particles.objects.GeneratedObjectData

class ObjectBuilder(val sizeInVertices: Int) {

    companion object {
        const val FLOATS_PER_VERTEX = 3

        private fun sizeOfCircleInVertices(numPoints: Int): Int {
            return 1 + (numPoints + 1)
        }

        private fun sizeOfOpenCylinderInVertices(numPoints: Int): Int {
            return (numPoints + 1) * 2
        }

        fun createPuck(puck: Cylinder, numPoints: Int): GeneratedObjectData {
            val size = (sizeOfCircleInVertices(numPoints)
                + sizeOfOpenCylinderInVertices(numPoints))

            val builder = ObjectBuilder(size)

            val puckTop = Circle(
                puck.center.translateY(puck.height / 2f),
                puck.radius
            )

            builder.appendCircle(puckTop, numPoints)
            builder.appendOpenCylinder(puck, numPoints)

            return builder.build()
        }

        fun createMallet(center: Point, radius: Float, height: Float, numPoints: Int): GeneratedObjectData {

            val size = (sizeOfCircleInVertices(numPoints) * 2
                + sizeOfOpenCylinderInVertices(numPoints) * 2)

            val builder = ObjectBuilder(size)

            // First, generate the mallet base.
            val baseHeight = height * 0.25f

            val baseCircle = Circle(
                center.translateY(-baseHeight),
                radius
            )
            val baseCylinder = Cylinder(
                baseCircle.center.translateY(-baseHeight / 2f),
                radius, baseHeight
            )

            builder.appendCircle(baseCircle, numPoints)
            builder.appendOpenCylinder(baseCylinder, numPoints)

            // Now generate the mallet handle.
            val handleHeight = height * 0.75f
            val handleRadius = radius / 3f

            val handleCircle = Circle(
                center.translateY(height * 0.5f),
                handleRadius
            )
            val handleCylinder = Cylinder(
                handleCircle.center.translateY(-handleHeight / 2f),
                handleRadius, handleHeight
            )

            builder.appendCircle(handleCircle, numPoints)
            builder.appendOpenCylinder(handleCylinder, numPoints)

            return builder.build()
        }
    }

    private val drawList = ArrayList<DrawCommand>()
    private val vertexData = FloatArray(sizeInVertices * FLOATS_PER_VERTEX)
    private var offset = 0

    private fun appendCircle(circle: Circle, numPoints: Int) {
        val startVertex = offset / FLOATS_PER_VERTEX
        val numVertices = sizeOfOpenCylinderInVertices(numPoints)
        val center = circle.center
        val radius = circle.radius
        vertexData[offset++] = center.x
        vertexData[offset++] = center.y
        vertexData[offset++] = center.z
        for (i in 0..numPoints) {
            val angleInRadians = (i.toFloat() / numPoints.toFloat()) * (Math.PI * 2f)
            vertexData[offset++] = center.x + radius * Math.cos(angleInRadians).toFloat()
            vertexData[offset++] = center.y
            vertexData[offset++] = center.z + radius * Math.sin(angleInRadians).toFloat()
        }
        drawList.add(object : DrawCommand {
            override fun draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, startVertex, numVertices)
            }
        })
    }

    fun appendOpenCylinder(cylinder: Cylinder, numPoints: Int) {
        val startVertex = offset / FLOATS_PER_VERTEX
        val numVertices = sizeOfOpenCylinderInVertices(numPoints)
        val yStart = cylinder.center.y - (cylinder.height / 2f)
        val yEnd = cylinder.center.y + (cylinder.height / 2f)
        for (i in 0..numPoints) {
            val angleInRadians = (i.toFloat() / numPoints.toFloat()) * (Math.PI * 2f)
            val xPosition = cylinder.center.x + cylinder.radius * Math.cos(angleInRadians).toFloat()
            val zPosition = cylinder.center.z + cylinder.radius * Math.sin(angleInRadians).toFloat()
            vertexData[offset++] = xPosition
            vertexData[offset++] = yStart
            vertexData[offset++] = zPosition
            vertexData[offset++] = xPosition
            vertexData[offset++] = yEnd
            vertexData[offset++] = zPosition
        }
        drawList.add(object : DrawCommand {
            override fun draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, startVertex, numVertices)
            }
        })
    }

    private fun build(): GeneratedObjectData = GeneratedObjectData(vertexData, drawList)
}