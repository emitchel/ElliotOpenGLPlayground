package com.opengl.playground

import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.util.Log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PlaygroundRenderer : Renderer {
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        log("onSurfaceCreated")
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        log("onSurfaceChanged, width: $width, height: $height")
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        // log("onDrawFrame")
        glClear(GL_COLOR_BUFFER_BIT)
    }
}