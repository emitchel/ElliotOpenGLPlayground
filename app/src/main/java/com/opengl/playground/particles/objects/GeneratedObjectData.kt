package com.opengl.playground.particles.objects

import com.opengl.playground.util.DrawCommand

data class GeneratedObjectData(val vertexData: FloatArray, val drawList: List<DrawCommand>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeneratedObjectData

        if (!vertexData.contentEquals(other.vertexData)) return false
        if (drawList != other.drawList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertexData.contentHashCode()
        result = 31 * result + drawList.hashCode()
        return result
    }
}