package com.opengl.camera.programs

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.Segmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions

class CameraSegmenter(context: Context) {

    private val segmenter: Segmenter

    init {
        val optionsBuilder = SelfieSegmenterOptions.Builder()
        optionsBuilder.setDetectorMode(
            SelfieSegmenterOptions.STREAM_MODE
        )
        val options = optionsBuilder.build()
        segmenter = Segmentation.getClient(options)
    }

    fun detectInImage(image: InputImage): Task<SegmentationMask> {
        return segmenter.process(image)
    }
}