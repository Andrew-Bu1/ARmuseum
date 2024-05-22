package com.xperiencelabs.arapp

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.node.PlacementMode

import io.github.sceneview.math.Position

import io.github.sceneview.node.VideoNode

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    private lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var videoButton: ExtendedFloatingActionButton
    private lateinit var modelNode: ArModelNode
    private lateinit var videoNode: VideoNode
    private lateinit var mediaPlayer: MediaPlayer
    private var currentModelIndex = 0
    private val modelFiles = arrayOf(
        "models/plane1.glb",
        "models/plane2.glb",
        "models/plane3.glb"
    )

    private var isPlaying = false
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.videoplayback)

        placeButton = findViewById(R.id.place)
        videoButton = findViewById(R.id.playVideo)

        placeButton.setOnClickListener {
            placeModel()
        }
        videoButton.setOnClickListener {
            playVideo()
        }

        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = modelFiles[currentModelIndex],
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)
            ) {
                sceneView.planeRenderer.isVisible = true
            }
        }
        sceneView.addChild(
            AugmentedImageNode(
                sceneView.engine,
                imageName = "plane1",
                bitmap = assets.open("images/plane1.jpeg")
                    .use(BitmapFactory::decodeStream)
            ).apply {
                loadModelGlbAsync(
                    glbFileLocation = modelFiles[0],
                    scaleToUnits = 0.5f,
                    centerOrigin = Position(-0.2f)
                )
            }
        )
//        sceneView.addChild(
//            AugmentedImageNode(
//                sceneView.engine,
//                imageName = "plane2",
//                bitmap = assets.open("images/plane2.jpg")
//                    .use(BitmapFactory::decodeStream)
//            ).apply {
//                loadModelGlbAsync(
//                    glbFileLocation = modelFiles[1],
//                    scaleToUnits = 0.5f,
//                    centerOrigin = Position(-0.2f)
//                )
//            }
//        )
//        sceneView.addChild(
//            AugmentedImageNode(
//                sceneView.engine,
//                imageName = "plane1",
//                bitmap = assets.open("images/plane3.jpeg")
//                    .use(BitmapFactory::decodeStream)
//            ).apply {
//                loadModelGlbAsync(
//                    glbFileLocation = modelFiles[2],
//                    scaleToUnits = 0.5f,
//                    centerOrigin = Position(-0.2f)
//                )
//            }
//        )
        sceneView.addChild(modelNode)
    }

    private fun placeModel() {
        currentModelIndex = (currentModelIndex + 1) % modelFiles.size
        modelNode.loadModelGlbAsync(
            glbFileLocation = modelFiles[currentModelIndex],
            scaleToUnits = 1f,
            centerOrigin = Position(-0.5f)
        )
        modelNode.anchor()
        sceneView.planeRenderer.isVisible = false
    }

    private fun playVideo() {
        if (!isPlaying) {
            videoNode = VideoNode(sceneView.engine, scaleToUnits = 0.7f, centerOrigin = Position(y = -4f), glbFileLocation = "models/plane.glb", player = mediaPlayer, onLoaded = { _, _ ->
                mediaPlayer.start()
            }).apply {
                // Apply the flip transformation
                scale = Position(1f, -1f, 1f)
            }
            isPlaying = true
            modelNode.addChild(videoNode)
        } else {
            if (isPaused) {
                mediaPlayer.start()
            } else {
                mediaPlayer.pause()
            }
            isPaused = !isPaused
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPaused = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

}
