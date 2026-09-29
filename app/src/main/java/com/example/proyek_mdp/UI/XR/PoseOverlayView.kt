package com.example.proyek_mdp.UI.XR

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Overlay "scanner" yang menggambar kerangka pose (33 titik MediaPipe)
 * di atas tampilan kamera AR.
 *
 * Koordinat titik sudah dalam satuan pixel layar (dihitung di CameraXRFragment).
 */
class PoseOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** x,y per landmark (33 * 2). Kosong = tidak menggambar apa pun. */
    private var points: FloatArray = FloatArray(0)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    private val jointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF") // cyan
        style = Paint.Style.FILL
    }

    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF9100") // oranye
        style = Paint.Style.FILL
    }

    fun updatePoints(newPoints: FloatArray) {
        points = newPoints
        postInvalidateOnAnimation()
    }

    fun clear() {
        points = FloatArray(0)
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.size < LANDMARK_COUNT * 2) return

        // Garis penghubung
        for ((a, b) in CONNECTIONS) {
            val ax = points[a * 2]
            val ay = points[a * 2 + 1]
            val bx = points[b * 2]
            val by = points[b * 2 + 1]
            if (ax == 0f && ay == 0f) continue
            if (bx == 0f && by == 0f) continue
            canvas.drawLine(ax, ay, bx, by, linePaint)
        }

        // Titik sendi
        for (i in 0 until LANDMARK_COUNT) {
            val x = points[i * 2]
            val y = points[i * 2 + 1]
            if (x == 0f && y == 0f) continue
            val isHand = i == LEFT_WRIST || i == RIGHT_WRIST ||
                i == LEFT_INDEX || i == RIGHT_INDEX ||
                i == LEFT_PINKY || i == RIGHT_PINKY
            canvas.drawCircle(x, y, if (isHand) 12f else 9f, if (isHand) handPaint else jointPaint)
        }
    }

    companion object {
        const val LANDMARK_COUNT = 33

        private const val LEFT_WRIST = 15
        private const val RIGHT_WRIST = 16
        private const val LEFT_INDEX = 19
        private const val RIGHT_INDEX = 20
        private const val LEFT_PINKY = 17
        private const val RIGHT_PINKY = 18

        /** Koneksi standar MediaPipe Pose (33 titik). */
        val CONNECTIONS: List<Pair<Int, Int>> = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 7,
            0 to 4, 4 to 5, 5 to 6, 6 to 8,
            9 to 10,
            11 to 12,
            11 to 13, 13 to 15, 15 to 17, 15 to 19, 15 to 21, 17 to 19,
            12 to 14, 14 to 16, 16 to 18, 16 to 20, 16 to 22, 18 to 20,
            11 to 23, 12 to 24, 23 to 24,
            23 to 25, 24 to 26, 25 to 27, 26 to 28,
            27 to 29, 28 to 30, 29 to 31, 30 to 32,
            27 to 31, 28 to 32
        )
    }
}
