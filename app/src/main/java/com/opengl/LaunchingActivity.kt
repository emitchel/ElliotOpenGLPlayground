package com.opengl

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.opengl.camera.BlurredCameraActivity
import com.opengl.camera.StaticImageCameraSegmentationActivity
import com.opengl.camera.StreamedVideoCameraActivity
import com.opengl.playground.airhockey.AirHockeyActivity
import com.opengl.playground.databinding.ActivityLaunchingBinding
import com.opengl.playground.particles.ParticlesActivity

class LaunchingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaunchingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLaunchingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.icehockey.setOnClickListener {
            // start CameraActivity
            startActivity(Intent(this, AirHockeyActivity::class.java))
        }

        binding.duetCamera.setOnClickListener {
            // start CameraActivity
            startActivity(Intent(this, StaticImageCameraSegmentationActivity::class.java))
        }

        binding.particles.setOnClickListener {
            // start CameraActivity
            startActivity(Intent(this, ParticlesActivity::class.java))
        }

        binding.streamedVideoCamera.setOnClickListener {
            startActivity(Intent(this, StreamedVideoCameraActivity::class.java))
        }

        binding.blurredCamera.setOnClickListener {
            startActivity(Intent(this, BlurredCameraActivity::class.java))
        }
    }
}