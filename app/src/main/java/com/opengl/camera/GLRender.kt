package com.opengl.camera

import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES20
import kotlinx.coroutines.CoroutineScope

class GLRender(
    private val scope: CoroutineScope
) {
    private val vertexShaderCode = """
        attribute vec4 vPosition;
        attribute vec2 inputTextureCoordinate;
        varying vec2 textureCoordinate;
        void main() {
            gl_Position = vPosition;
            textureCoordinate = inputTextureCoordinate;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec2 textureCoordinate;
        uniform sampler2D sTexture;
        void main() {
            gl_FragColor = texture2D(sTexture, textureCoordinate);
        }
    """
    private var mEglDisplay: EGLDisplay? = null
    private var mEglConfig: EGLConfig? = null
    private var mEglContext: EGLContext? = null
    private var mEglSurface: EGLSurface? = null
    private val mProgram = 0

    init {
        initEGL()
    }

    private fun initEGL() {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)

        val version = IntArray(2)
        EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)

        val attribs = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 16,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            // TODO does it work on < api 26 devices?
            EGLExt.EGL_RECORDABLE_ANDROID, 1, // This makes the EGL context recordable
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfig = IntArray(1)
        EGL14.eglChooseConfig(mEglDisplay, attribs, 0, configs, 0, 1, numConfig, 0)
        mEglConfig = configs[0]!!

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        mEglContext =
            EGL14.eglCreateContext(mEglDisplay, mEglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
    }

    fun attachToTextureView(surfaceTexture: SurfaceTexture?) {
        val attribList = intArrayOf(
            EGL14.EGL_NONE
        )
        mEglSurface =
            EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surfaceTexture, attribList, 0)
        EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)

        // Setup OpenGL ES shaders, programs, etc.
        // Example: setupShadersAndPrograms();
    }

    // This is a placeholder; your shader setup will vary based on your requirements
    private fun setupShadersAndPrograms() {
        // Vertex and fragment shader creation...
        // Program creation and linking...
        // Use GLES20 methods: glCreateShader(), glShaderSource(), glCompileShader(), etc.
    }

    fun beginFrame() {
        // Use your program and render
        GLES20.glUseProgram(mProgram)
        // More rendering commands here...
    }

    fun endFrame() {
        EGL14.eglSwapBuffers(mEglDisplay, mEglSurface)
    }

    fun release() {
        EGL14.eglDestroySurface(mEglDisplay, mEglSurface)
        EGL14.eglDestroyContext(mEglDisplay, mEglContext)
        EGL14.eglTerminate(mEglDisplay)
    }
}
