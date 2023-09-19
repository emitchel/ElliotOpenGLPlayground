package com.opengl.camera

import android.opengl.GLES20
import android.opengl.GLSurfaceView.Renderer
import com.opengl.playground.util.log
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RecordedCanvasRenderer(
    private val getRenderersOnGlThread: () -> List<CanvasRendererLayer>
) :
    Renderer {

    private var renderers: List<CanvasRendererLayer> = emptyList()

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        renderers = getRenderersOnGlThread()
        renderers.forEach {
            it.onSurfaceCreated()
        }
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        log("surface changed $width $height")
        GLES20.glViewport(0, 0, width, height)
        renderers.forEach {
            it.onSurfaceChanged(width, height)
        }
    }

    override fun onDrawFrame(p0: GL10?) {
        // Clear the rendering surface.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        renderers.forEach {
            it.onDrawFrame()
        }
    }
}