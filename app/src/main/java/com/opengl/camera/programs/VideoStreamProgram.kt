package com.opengl.camera.programs

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.Surface
import androidx.annotation.RawRes
import com.opengl.camera.CanvasRendererLayer
import com.opengl.playground.R
import com.opengl.playground.airhockey.AirHockeyRenderer
import com.opengl.playground.objects.VertexArray
import com.opengl.playground.programs.TextureShaderProgram
import com.opengl.playground.util.TextureHelper
import com.opengl.playground.util.log

class VideoStreamProgram(
    val context: Context,
    private val glSurfaceView: GLSurfaceView,
    private val mediaPlayer: MediaPlayer
) : TextureShaderProgram(context, R.raw.video_vertex_shader, R.raw.video_fragment_shader),
    CanvasRendererLayer {

    private var textureId = 0
    private var width: Int = 0
    private var height: Int = 0

    var videoWidth = 0
    var videoHeight = 0
    var videoAspectRatio = 0f

    private val vertexArray: VertexArray = VertexArray(VERTEX_DATA)

    private var surfaceTexture: SurfaceTexture? = null

    private val modelMatrix = FloatArray(16)
    private var updateSurface = false
    override fun onSurfaceCreated() {
        textureId = TextureHelper.createExternalOesTexture()

        surfaceTexture = SurfaceTexture(textureId)
        val surface = Surface(surfaceTexture)
        surfaceTexture?.setOnFrameAvailableListener {
            updateSurface = true
            glSurfaceView.requestRender()
        }
        mediaPlayer.setSurface(surface)

        // REMOTE URL
        mediaPlayer.setDataSource("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")

        // LOCAL FILE
        // TODO might be too big!
        // val afd = context.resources.openRawResourceFd(rawVideoResource)
        // mediaPlayer.setDataSource(
        //     afd.fileDescriptor,
        //     afd.startOffset,
        //     afd.length
        // )
        // afd.close()
        mediaPlayer.prepareAsync()

        mediaPlayer.setOnPreparedListener {
            // mediaPlayer.isPlaying = true
            videoWidth = mediaPlayer.videoWidth
            videoHeight = mediaPlayer.videoHeight
            videoAspectRatio = videoWidth.toFloat() / videoHeight
            log("Media player prepared")
            mediaPlayer.start()
            mediaPlayer.isLooping = true
        }

        mediaPlayer.setOnErrorListener { mediaPlayer, i, i2 ->
            log("Media player Error: $i, $i2")
            true
        }
        mediaPlayer.setOnInfoListener { mp, what, extra ->
            log("Media player Info: $what, $extra")
            false
        }
        // Load the video
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun onDrawFrame() {
        synchronized(this) {
            if (updateSurface) {
                surfaceTexture?.updateTexImage()
                updateSurface = false
            }
        }
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        // set positioning... i think
        positionContent()
        // setup the shaders to run
        GLES20.glUseProgram(program)
        // log("Media player is playing: ${mediaPlayer.isPlaying}")

        // Bind the camera texture
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(getuMatrixLocation(), 1, false, modelMatrix, 0)
        // GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        // Set the active texture unit to texture unit 0 for camera feed.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(
            getuTextureUnitLocation(),
            0
        );

        // bind the triangle + texture data
        vertexArray.setVertexAttribPointer(
            0,
            getPositionAttributeLocation(),
            POSITION_COMPONENT_COUNT,
            STRIDE
        )
        vertexArray.setVertexAttribPointer(
            POSITION_COMPONENT_COUNT,
            getTextureCoordinatesAttributeLocation(),
            TEXTURE_COORDINATES_COMPONENT_COUNT,
            STRIDE
        )

        // lastly, draw the vertices...
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)
        GLES20.glGetError().also { if (it != GLES20.GL_NO_ERROR) log("!!! OpenGL Error: $it") }
    }

    private fun positionContent() {
        Matrix.setIdentityM(modelMatrix, 0)

        val viewportAspectRatio = width.toFloat() / height.toFloat()
        val scaleX: Float
        val scaleY: Float

        if (videoAspectRatio > viewportAspectRatio) {
            scaleX = 1f
            scaleY = viewportAspectRatio / videoAspectRatio
        } else {
            scaleX = videoAspectRatio / viewportAspectRatio
            scaleY = 1f
        }
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, scaleX, scaleY, 1f)

        // it loads the assets into full frame, so no modifications needed at the moment
        // could maintain it's aspect ratio given the content but we're not doing that now
    }

    companion object {
        private const val POSITION_COMPONENT_COUNT = 2
        private const val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private val STRIDE: Int = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * AirHockeyRenderer.BYTES_PER_FLOAT
        private val VERTEX_DATA = floatArrayOf(
            // Order of coordinates: X, Y, S, T
            -1f, -1f, 0f, 1f,     // Bottom-left
            1f, -1f, 1f, 1f,      // Bottom-right
            1f, 1f, 1f, 0f,       // Top-right
            -1f, 1f, 0f, 0f,      // Top-left
            -1f, -1f, 0f, 1f      // Bottom-left again to close the fan
        )
    }
}