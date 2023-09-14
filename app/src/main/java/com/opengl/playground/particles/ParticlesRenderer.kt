package com.opengl.playground.particles

import android.content.Context
import android.graphics.Color
import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_ONE
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix.multiplyMM
import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import com.opengl.playground.particles.objects.ParticleFireworksExplosion
import com.opengl.playground.particles.objects.ParticleShooter
import com.opengl.playground.particles.objects.ParticleSystem
import com.opengl.playground.particles.programs.ParticleShaderProgram
import com.opengl.playground.util.MatrixHelper.perspectiveM
import com.opengl.playground.util.Point
import com.opengl.playground.util.TextureHelper.loadTexture
import com.opengl.playground.util.Vector
import java.util.Random
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ParticlesRenderer(private val context: Context) : Renderer {
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)

    // Maximum saturation and value.
    private val hsv = FloatArray(3)
    private var particleProgram: ParticleShaderProgram? = null
    private var particleSystem: ParticleSystem? = null
    private var redParticleShooter: ParticleShooter? = null
    private var greenParticleShooter: ParticleShooter? = null
    private var blueParticleShooter: ParticleShooter? = null

    private lateinit var particleFireworksExplosion: ParticleFireworksExplosion;
    private lateinit var random: Random;
    private var globalStartTime: Long = 0
    private var texture = 0
    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        // Enable additive blending
        glEnable(GL_BLEND)
        glBlendFunc(GL_ONE, GL_ONE)
        particleProgram = ParticleShaderProgram(context)
        particleSystem = ParticleSystem(10000)
        globalStartTime = System.nanoTime()
        val particleDirection = Vector(0f, 0.5f, 0f)
        val angleVarianceInDegrees = 5f
        val speedVariance = 1f

        redParticleShooter = ParticleShooter(
            Point(-.5f, 0f, 0f),
            particleDirection,
            Color.rgb(255, 50, 5),
            angleVarianceInDegrees,
            speedVariance
        )
        greenParticleShooter = ParticleShooter(
            Point(0f, 0f, 0f),
            particleDirection,
            Color.rgb(25, 255, 25),
            angleVarianceInDegrees,
            speedVariance
        )
        blueParticleShooter = ParticleShooter(
            Point(.5f, 0f, 0f),
            particleDirection,
            Color.rgb(5, 50, 255),
            angleVarianceInDegrees,
            speedVariance
        )
        particleFireworksExplosion = ParticleFireworksExplosion();

        random = Random()

        texture = loadTexture(context, com.opengl.playground.R.drawable.particle_texture)
    }

    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        perspectiveM(projectionMatrix, 45f, width.toFloat() / height.toFloat(), 1f, 10f)
        setIdentityM(viewMatrix, 0)
        translateM(viewMatrix, 0, 0f, -1.5f, -5f)
        multiplyMM(
            viewProjectionMatrix, 0, projectionMatrix, 0,
            viewMatrix, 0
        )
    }

    override fun onDrawFrame(glUnused: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)
        val currentTime = (System.nanoTime() - globalStartTime) / 1_000_000_000f
        redParticleShooter!!.addParticles(particleSystem!!, currentTime, 5)
        greenParticleShooter!!.addParticles(particleSystem!!, currentTime, 5)
        blueParticleShooter!!.addParticles(particleSystem!!, currentTime, 5)
        // not sure how this isn't working
        // if (random.nextFloat() < 0.02f || true) {
        //     hsv[0] = random.nextInt(360).toFloat();
        //
        //     particleFireworksExplosion.addExplosion(
        //         particleSystem!!,
        //         Point(
        //             -1f + random.nextFloat() * 2f,
        //             3f + random.nextFloat() / 2f,
        //             -1f + random.nextFloat() * 2f
        //         ),
        //         Color.HSVToColor(hsv),
        //         globalStartTime
        //     )
        // }
        particleProgram!!.useProgram()
        /*
        particleProgram.setUniforms(viewProjectionMatrix, currentTime);
         */
        particleProgram!!.setUniforms(viewProjectionMatrix, currentTime, texture)
        particleSystem!!.bindData(particleProgram!!)
        particleSystem!!.draw()
    }
}