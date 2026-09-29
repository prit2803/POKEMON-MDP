package com.example.proyek_mdp.UI.XR

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * Wrapper MediaPipe Pose Landmarker untuk mendeteksi kerangka tubuh (33 titik).
 * Model: assets/pose_landmarker_lite.task
 */
class PoseTracker(context: Context) {

    private var poseLandmarker: PoseLandmarker? = null

    init {
        try {
            val baseOptions = BaseOptions.builder()
                .setDelegate(Delegate.CPU)
                .setModelAssetPath(MODEL_ASSET)
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumPoses(1)
                // Diturunkan agar lebih sensitif terhadap foto/gambar orang
                .setMinPoseDetectionConfidence(0.25f)
                .setMinPosePresenceConfidence(0.25f)
                .setMinTrackingConfidence(0.25f)
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            Log.e("PoseTracker", "Gagal membuat PoseLandmarker", e)
        }
    }

    val isReady: Boolean get() = poseLandmarker != null

    /** Deteksi pose dari Bitmap (sinkron). Kembalikan null jika gagal/tidak ada orang. */
    fun detect(bitmap: Bitmap): PoseLandmarkerResult? {
        val landmarker = poseLandmarker ?: return null
        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            landmarker.detect(mpImage)
        } catch (e: Exception) {
            Log.e("PoseTracker", "detect gagal", e)
            null
        }
    }

    fun close() {
        try {
            poseLandmarker?.close()
        } catch (_: Exception) {
        }
        poseLandmarker = null
    }

    companion object {
        private const val MODEL_ASSET = "pose_landmarker_lite.task"

        // Indeks landmark MediaPipe Pose (33 titik)
        const val NOSE = 0
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
        const val LEFT_HIP = 23
        const val RIGHT_HIP = 24
    }
}
