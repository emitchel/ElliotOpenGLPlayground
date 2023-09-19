package com.opengl.camera

interface CanvasRendererLayer {
    fun onSurfaceCreated()
    fun onSurfaceChanged(width: Int, height: Int)
    fun onDrawFrame()
}