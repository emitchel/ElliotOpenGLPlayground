package com.opengl.playground.particles.objects

import android.opengl.Matrix.multiplyMV
import android.opengl.Matrix.setRotateEulerM
import com.opengl.playground.util.Point
import com.opengl.playground.util.Vector
import java.lang.Integer.max
import java.util.Random

class ParticleShooter(
    private val position: Point,
    private val direction: Vector,
    private val color: Int,
    private val angleVariance: Float,
    private val speedVariance: Float
) {
    private val random: Random = Random()
    private val rotationMatrix = FloatArray(16)
    private val directionVector = FloatArray(4)
    private val resultVector = FloatArray(4)

    /*

    public ParticleShooter(Point position, Vector direction, int color) {
     */
    init {
        directionVector[0] = direction.x
        directionVector[1] = direction.y
        directionVector[2] = direction.z
    }

    fun addParticles(
        particleSystem: ParticleSystem,
        currentTime: Float,
        count: Int,
        trailingCount: Int = 2,  // number of trailing points
        distanceBetween: Float = 0.05f  // distance between each trailing point
    ) {
        for (i in 0 until count) {
            setRotateEulerM(
                rotationMatrix, 0,
                (random.nextFloat() - 0.5f) * angleVariance,
                (random.nextFloat() - 0.5f) * angleVariance,
                (random.nextFloat() - 0.5f) * angleVariance
            )
            multiplyMV(
                resultVector, 0,
                rotationMatrix, 0,
                directionVector, 0
            )
            val speedAdjustment: Float = 1f + random.nextFloat() * speedVariance
            val thisDirection = Vector(
                resultVector[0] * speedAdjustment,
                resultVector[1] * speedAdjustment,
                resultVector[2] * speedAdjustment
            )
            particleSystem.addParticle(position, color, thisDirection, currentTime)
            // TODO failed attempt to add trailing particles
            // add trailling particles here
            val magnitude = Math.sqrt((thisDirection.x * thisDirection.x + thisDirection.y * thisDirection.y + thisDirection.z * thisDirection.z).toDouble()).toFloat()
            val normalizedDirection = Vector(thisDirection.x / magnitude, thisDirection.y / magnitude, thisDirection.z / magnitude)
            val scaledDirection = Vector(normalizedDirection.x * distanceBetween, normalizedDirection.y * distanceBetween, normalizedDirection.z * distanceBetween)
            var currentTrailPosition = position

            for (j in 0 until trailingCount) {
                currentTrailPosition = Point(
                    currentTrailPosition.x - scaledDirection.x,
                    currentTrailPosition.y - scaledDirection.y,
                    currentTrailPosition.z - scaledDirection.z
                )

                particleSystem.addParticle(currentTrailPosition, color, thisDirection, currentTime)
            }
        }
    }
}