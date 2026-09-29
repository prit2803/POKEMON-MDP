package com.example.proyek_mdp.UI.XR

import android.animation.ValueAnimator
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokemonSelectionAdapter
import androidx.fragment.app.viewModels
import com.example.proyek_mdp.viewmodel.CameraViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.auth.SessionManager
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Pose
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.graphics.Color
import android.widget.ImageView
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

class CameraXRFragment : Fragment(R.layout.fragment_camera_xr) {

    // ===== VIEWS =====
    private lateinit var sceneView: ARSceneView
    private lateinit var btnSelectPokemon: Button
    private lateinit var btnRotate: Button
    private lateinit var btnScaleUp: Button
    private lateinit var btnScaleDown: Button
    private lateinit var btnReset: Button
    private lateinit var btnRemove: Button
    private lateinit var btnRemoveAll: Button
    private lateinit var btnCloseSelection: Button
    private lateinit var pokemonSelectionPanel: CardView
    private lateinit var recyclerViewPokemonSelection: RecyclerView
    private lateinit var tvSelectedPokemon: TextView
    private lateinit var tvModelCount: TextView
    private lateinit var tvTrackingStatus: TextView
    private lateinit var poseOverlay: PoseOverlayView

    // Mode & Capture UI
    private lateinit var btnModePlace: Button
    private lateinit var btnModeCapture: Button
    private lateinit var btnModeTracking: Button
    private lateinit var btnCapture: Button
    private lateinit var topControls: LinearLayout
    private lateinit var bottomControls: LinearLayout
    private lateinit var captureContainer: FrameLayout

    private enum class XrMode { PLACE, CAPTURE, TRACKING }
    private var currentMode = XrMode.PLACE

    // QR Code UI
    private lateinit var qrCodePanel: CardView
    private lateinit var ivQrCode: ImageView
    private lateinit var btnCloseQr: Button

    // ===== VARIABLES =====
    private lateinit var sessionManager: SessionManager
    private var selectionAdapter: PokemonSelectionAdapter? = null

    // Model nodes
    private val modelNodes = mutableListOf<ModelNode>()
    private val anchorNodes = mutableListOf<AnchorNode>()

    // SIMPAN ROTASI PER MODEL
    private val modelRotationAngles = mutableMapOf<ModelNode, Float>()

    // Scale settings
    private var currentScale = 0.15f
    private val minScale = 0.05f
    private val maxScale = 2.0f

    private var selectedPokemon: PokemonEntity? = null
    private var spawnCounter = 0

    // Track coroutine job
    private var loadPokemonJob: Job? = null

    // ===== POSE TRACKING =====
    private var poseTracker: PoseTracker? = null
    private var isDetectingPose = false
    private var lastPoseDetectTime = 0L
    private var followNode: ModelNode? = null
    private var rigController: RigPoseController? = null
    private var demoPoseApplied = false
    private val poseFollowDistance = 1.5f  // dekatkan model ke orang
    private val poseOffsetUp = 0.3f
    private val poseOffsetRight = 0.0f
    private val enablePokemonFollow = true

    // ===== Smoothing (low-pass) agar gerakan lebih halus =====
    private val smoothX = FloatArray(33)
    private val smoothY = FloatArray(33)
    private val smoothZ = FloatArray(33)
    private var smoothInit = false
    private val smoothAlpha = 0.35f
    private val posSmoothAlpha = 0.25f
    private var hasFollowTarget = false
    private var lastNodeX = 0f
    private var lastNodeY = 0f
    private var lastNodeZ = 0f
    private var smoothedYaw = 0f
    private var lastDepthMeters = -1f
    private var depthFrameCounter = 0
    private var lastUsedDistance = poseFollowDistance

    @Volatile
    private var viewDestroyed = false

    private val viewModel: CameraViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    // ===== LIFECYCLE =====
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewDestroyed = false

        sessionManager = SessionManager(requireContext())
        initViews(view)
        setupRecyclerView()
        setupARScene()
        setupListeners()
        updateUIState()
        
        viewModel.pokemonList.observe(viewLifecycleOwner) { pokemonList ->
            if (pokemonList.isEmpty()) {
                Toast.makeText(requireContext(), "Koleksi Pokemon kosong!", Toast.LENGTH_SHORT).show()
            } else {
                selectionAdapter?.updateData(pokemonList)
                pokemonSelectionPanel.visibility = View.VISIBLE
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===== INIT VIEWS =====
    private fun initViews(view: View) {
        sceneView = view.findViewById(R.id.sceneView)
        btnSelectPokemon = view.findViewById(R.id.btnSelectPokemon)
        btnRotate = view.findViewById(R.id.btnRotate)
        btnScaleUp = view.findViewById(R.id.btnScaleUp)
        btnScaleDown = view.findViewById(R.id.btnScaleDown)
        btnReset = view.findViewById(R.id.btnReset)
        btnRemove = view.findViewById(R.id.btnRemove)
        btnRemoveAll = view.findViewById(R.id.btnRemoveAll)
        btnCloseSelection = view.findViewById(R.id.btnCloseSelection)
        pokemonSelectionPanel = view.findViewById(R.id.pokemonSelectionPanel)
        recyclerViewPokemonSelection = view.findViewById(R.id.recyclerViewPokemonSelection)
        tvSelectedPokemon = view.findViewById(R.id.tvSelectedPokemon)
        tvModelCount = view.findViewById(R.id.tvModelCount)
        tvTrackingStatus = view.findViewById(R.id.tvTrackingStatus)
        poseOverlay = view.findViewById(R.id.poseOverlay)

        btnModePlace = view.findViewById(R.id.btnModePlace)
        btnModeCapture = view.findViewById(R.id.btnModeCapture)
        btnModeTracking = view.findViewById(R.id.btnModeTracking)
        btnCapture = view.findViewById(R.id.btnCapture)
        topControls = view.findViewById(R.id.topControls)
        bottomControls = view.findViewById(R.id.bottomControls)
        captureContainer = view.findViewById(R.id.captureContainer)
        
        qrCodePanel = view.findViewById(R.id.qrCodePanel)
        ivQrCode = view.findViewById(R.id.ivQrCode)
        btnCloseQr = view.findViewById(R.id.btnCloseQr)
    }

    // ===== RECYCLER VIEW =====
    private fun setupRecyclerView() {
        recyclerViewPokemonSelection.layoutManager = LinearLayoutManager(requireContext())
        selectionAdapter = PokemonSelectionAdapter(emptyList()) { pokemon ->
            onPokemonSelected(pokemon)
        }
        recyclerViewPokemonSelection.adapter = selectionAdapter
    }

    // ===== AR SCENE =====
    private fun setupARScene() {
        try {
            sceneView.planeRenderer.isEnabled = true
            sceneView.planeRenderer.isShadowReceiver = true
            sceneView.planeRenderer.isVisible = true // Tampilkan grid deteksi permukaan

            sceneView.configureSession { session, config ->
                config.lightEstimationMode = Config.LightEstimationMode.DISABLED // Selalu terang
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                config.depthMode = Config.DepthMode.AUTOMATIC
                config.focusMode = Config.FocusMode.AUTO
            }

            // Dipanggil tiap frame ARCore. Pose tracking hanya aktif di mode Tracking.
            sceneView.onSessionUpdated = { _, frame ->
                if (currentMode == XrMode.TRACKING && !isDetectingPose) {
                    val now = System.currentTimeMillis()
                    if (now - lastPoseDetectTime >= 100) { // ~10 fps
                        lastPoseDetectTime = now
                        handlePoseFrame(frame)
                    }
                }
            }
            
            // Membuat pencahayaan ambient (sekeliling) putih merata
            val sh = FloatArray(27) { 0f }
            sh[0] = 1f // R (Ambient dasar)
            sh[1] = 1f // G
            sh[2] = 1f // B
            
            val indirectLight = com.google.android.filament.IndirectLight.Builder()
                .irradiance(3, sh)
                .intensity(50000f) // Intensitas ambient
                .build(sceneView.engine)
                
            sceneView.indirectLight = indirectLight

            val gestureDetector = android.view.GestureDetector(requireContext(), object : android.view.GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: android.view.MotionEvent): Boolean {
                    val frame = sceneView.frame ?: return false
                    
                    val hitResults = frame.hitTest(e.x, e.y)
                    val hitResult = hitResults.firstOrNull { hit ->
                        val trackable = hit.trackable
                        trackable is com.google.ar.core.Plane && trackable.isPoseInPolygon(hit.hitPose)
                    }

                    if (hitResult != null) {
                        val pokemon = selectedPokemon
                        if (pokemon != null) {
                            spawnPokemonAtHitResult(hitResult)
                        } else {
                            Toast.makeText(requireContext(), "Pilih Pokemon terlebih dahulu!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Arahkan kamera hingga muncul grid putih, lalu ketuk grid tersebut!", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
            })

            sceneView.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                false // Tetap return false agar fungsi kamera lain (seperti rotasi) tetap bekerja
            }
        } catch (e: Exception) {
            Log.e("CameraXRFragment", "Error configuring session", e)
        }
    }

    // ===== LISTENERS =====
    private fun setupListeners() {
        btnSelectPokemon.setOnClickListener {
            showPokemonSelectionPanel()
        }

        btnCloseSelection.setOnClickListener {
            pokemonSelectionPanel.visibility = View.GONE
        }

        btnRotate.setOnClickListener {
            rotateAllModels()
        }

        btnScaleUp.setOnClickListener {
            if (modelNodes.isNotEmpty() || followNode != null) {
                currentScale = (currentScale * 1.2f).coerceIn(minScale, maxScale)
                applyScaleToAll()
                Toast.makeText(requireContext(), "Zoom in: ${"%.2f".format(currentScale)}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No models!", Toast.LENGTH_SHORT).show()
            }
        }

        btnScaleDown.setOnClickListener {
            if (modelNodes.isNotEmpty() || followNode != null) {
                currentScale = (currentScale * 0.8f).coerceIn(minScale, maxScale)
                applyScaleToAll()
                Toast.makeText(requireContext(), "Zoom out: ${"%.2f".format(currentScale)}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No models!", Toast.LENGTH_SHORT).show()
            }
        }

        btnReset.setOnClickListener {
            resetAllModels()
        }

        btnRemove.setOnClickListener {
            removeLastModel()
        }

        btnRemoveAll.setOnClickListener {
            removeAllModels()
        }

        // Mode Toggles
        btnModePlace.setOnClickListener {
            switchMode(XrMode.PLACE)
        }

        btnModeCapture.setOnClickListener {
            switchMode(XrMode.CAPTURE)
        }

        btnModeTracking.setOnClickListener {
            switchMode(XrMode.TRACKING)
        }

        btnCapture.setOnClickListener {
            captureScreenshot()
        }

        btnCloseQr.setOnClickListener {
            qrCodePanel.visibility = View.GONE
        }
    }

    private fun switchMode(mode: XrMode) {
        currentMode = mode

        val active = resources.getColor(android.R.color.holo_green_dark, null)
        val inactive = resources.getColor(android.R.color.transparent, null)
        btnModePlace.setBackgroundColor(if (mode == XrMode.PLACE) active else inactive)
        btnModeCapture.setBackgroundColor(if (mode == XrMode.CAPTURE) active else inactive)
        btnModeTracking.setBackgroundColor(if (mode == XrMode.TRACKING) active else inactive)

        val isCapture = mode == XrMode.CAPTURE
        topControls.visibility = if (isCapture) View.GONE else View.VISIBLE
        // Di mode Tracking, sembunyikan tombol "Pilih Pokemon/Reset" agar tidak
        // bertabrakan dengan tombol capture.
        bottomControls.visibility = if (mode == XrMode.PLACE) View.VISIBLE else View.GONE
        captureContainer.visibility =
            if (mode == XrMode.CAPTURE || mode == XrMode.TRACKING) View.VISIBLE else View.GONE
        sceneView.planeRenderer.isVisible = mode == XrMode.PLACE
        tvTrackingStatus.visibility = if (mode == XrMode.TRACKING) View.VISIBLE else View.GONE
        poseOverlay.visibility = if (mode == XrMode.TRACKING) View.VISIBLE else View.GONE
        if (mode == XrMode.TRACKING) {
            tvTrackingStatus.text = "Tracking: menunggu deteksi..."
        } else {
            poseOverlay.clear()
        }

        if (mode == XrMode.TRACKING) {
            startTracking()
        } else {
            stopTracking()
        }

        val msg = when (mode) {
            XrMode.PLACE -> "Placement Mode Active"
            XrMode.CAPTURE -> "Capture Mode Active"
            XrMode.TRACKING -> "Tracking Mode: arahkan kamera ke orang"
        }
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    // ===== POSE TRACKING (mode Tracking) =====

    private fun startTracking() {
        if (poseTracker == null) {
            poseTracker = PoseTracker(requireContext())
        }
        if (poseTracker?.isReady != true) {
            tvTrackingStatus.text = "MediaPipe GAGAL dimuat (model .task?)"
            Toast.makeText(requireContext(), "MediaPipe gagal dimuat", Toast.LENGTH_LONG).show()
            return
        }

        if (enablePokemonFollow && followNode == null) {
            val placed = modelNodes.firstOrNull()
            if (placed != null) {
                adoptPlacedModel(placed)
            } else {
                ensureFollowNode()
            }
        }

        if (enablePokemonFollow && followNode == null) {
            Toast.makeText(requireContext(), "Tempatkan Pokemon dulu di mode Place", Toast.LENGTH_LONG).show()
        }
    }

    private fun adoptPlacedModel(model: ModelNode) {
        val idx = modelNodes.indexOf(model)
        if (idx >= 0 && idx < anchorNodes.size) {
            val anchor = anchorNodes[idx]
            anchor.removeChildNode(model)
            sceneView.removeChildNode(anchor)
            anchorNodes.removeAt(idx)
        }
        modelNodes.remove(model)
        modelRotationAngles.remove(model)

        sceneView.addChildNode(model)
        model.isPositionEditable = false
        followNode = model

        rigController = RigPoseController(requireContext(), getModelPath())
        demoPoseApplied = false

        Toast.makeText(requireContext(), "Model siap mengikuti gerakan", Toast.LENGTH_SHORT).show()
    }

    private fun stopTracking() {
        isDetectingPose = false
        poseOverlay.clear()
        tvTrackingStatus.text = "Tracking nonaktif"
    }

    private fun ensureFollowNode() {
        if (followNode != null) return
        val pokemon = selectedPokemon ?: return

        val node: ModelNode = try {
            val instance = sceneView.modelLoader.createModelInstance(assetFileLocation = getModelPath())
            ModelNode(modelInstance = instance, scaleToUnits = 0.01f, centerOrigin = Position(y = -0.5f))
        } catch (e: Exception) {
            Log.e("XR_Tracking", "Gagal memuat ${pokemon.name}, pakai pikachu", e)
            try {
                val instance = sceneView.modelLoader.createModelInstance(assetFileLocation = "models/pikachu.glb")
                ModelNode(modelInstance = instance, scaleToUnits = 0.01f, centerOrigin = Position(y = -0.5f))
            } catch (e2: Exception) {
                Log.e("XR_Tracking", "Fallback model gagal", e2)
                return
            }
        }

        node.scale = Position(currentScale, currentScale, currentScale)
        node.isPositionEditable = false
        node.isShadowCaster = false
        node.isShadowReceiver = false
        sceneView.addChildNode(node)
        followNode = node

        rigController = RigPoseController(requireContext(), getModelPath())
        demoPoseApplied = false
    }

    private fun removeFollowNode() {
        followNode?.let { sceneView.removeChildNode(it) }
        followNode = null
        rigController = null
        demoPoseApplied = false
    }

    private fun handlePoseFrame(frame: Frame) {
        if (viewDestroyed || !isAdded) return
        isDetectingPose = true

        val image = try {
            frame.acquireCameraImage()
        } catch (e: Exception) {
            Log.e("XR_Tracking", "acquireCameraImage gagal", e)
            isDetectingPose = false
            return
        }

        // Depth ARCore diambil tiap 3 frame agar ringan.
        val depthImage = if (depthFrameCounter % 3 == 0) {
            try {
                frame.acquireDepthImage16Bits()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        depthFrameCounter++

        val intr = frame.camera.imageIntrinsics
        val fx = intr.focalLength[0]
        val fy = intr.focalLength[1]
        val cx = intr.principalPoint[0]
        val cy = intr.principalPoint[1]
        val imgW = intr.imageDimensions[0].toFloat()
        val imgH = intr.imageDimensions[1].toFloat()
        val cameraPose = frame.camera.pose

        val proj = FloatArray(16)
        frame.camera.getProjectionMatrix(proj, 0, 0.1f, 100f)
        val viewM = FloatArray(16)
        frame.camera.getViewMatrix(viewM, 0)
        val vp = mulMat4(proj, viewM)
        val overlayW = poseOverlay.width.toFloat()
        val overlayH = poseOverlay.height.toFloat()

        val tracker = poseTracker
        if (tracker == null) {
            image.close()
            isDetectingPose = false
            return
        }

        lifecycleScope.launch {
            var poseCount = -1
            var screenPoints: FloatArray? = null
            var detectionResult: PoseLandmarkerResult? = null

            val worldPos = withContext(Dispatchers.Default) {
                val bitmap = try {
                    YuvToBitmap.convert(image)
                } catch (e: Exception) {
                    Log.e("XR_Tracking", "Konversi YUV gagal", e)
                    null
                } finally {
                    try { image.close() } catch (_: Exception) {}
                }

                if (bitmap == null) {
                    poseCount = -3
                    return@withContext null
                }

                try {
                    val result = tracker.detect(bitmap)
                    detectionResult = result
                    poseCount = result?.landmarks()?.size ?: 0
                    if (result != null) {
                        screenPoints = computeScreenPoints(
                            result, cameraPose, fx, fy, cx, cy, imgW, imgH, vp, overlayW, overlayH
                        )
                        // Sampling kedalaman di pusat badan (tiap 3 frame)
                        if (depthImage != null) {
                            val bc = bodyCenter(result)
                            if (bc != null) {
                                val dm = sampleDepth(depthImage, bc[0], bc[1])
                                if (dm > 0f) lastDepthMeters = dm
                            }
                        }
                        if (enablePokemonFollow) {
                            computeFollowPosition(result, cameraPose, fx, fy, cx, cy, imgW, imgH, lastDepthMeters)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e("XR_Tracking", "Deteksi pose gagal", e)
                    poseCount = -2
                    null
                } finally {
                    try { depthImage?.close() } catch (_: Exception) {}
                }
            }

            if (viewDestroyed) {
                isDetectingPose = false
                return@launch
            }

            poseOverlay.updatePoints(screenPoints ?: FloatArray(0))

            if (!enablePokemonFollow) {
                tvTrackingStatus.text = when {
                    poseCount > 0 -> "Orang terdeteksi (pose=$poseCount)"
                    poseCount == 0 -> "Tidak ada orang terdeteksi (pose=0)"
                    else -> "Deteksi error (pose=$poseCount)"
                }
            } else if (worldPos != null) {
                val up = FloatArray(3)
                val right = FloatArray(3)
                cameraPose.rotateVector(floatArrayOf(0f, 1f, 0f), 0, up, 0)
                cameraPose.rotateVector(floatArrayOf(1f, 0f, 0f), 0, right, 0)
                val targetX = worldPos[0] + up[0] * poseOffsetUp + right[0] * poseOffsetRight
                val targetY = worldPos[1] + up[1] * poseOffsetUp + right[1] * poseOffsetRight
                val targetZ = worldPos[2] + up[2] * poseOffsetUp + right[2] * poseOffsetRight

                // Smoothing posisi
                if (!hasFollowTarget) {
                    lastNodeX = targetX; lastNodeY = targetY; lastNodeZ = targetZ
                    hasFollowTarget = true
                } else {
                    lastNodeX = lastNodeX * (1f - posSmoothAlpha) + targetX * posSmoothAlpha
                    lastNodeY = lastNodeY * (1f - posSmoothAlpha) + targetY * posSmoothAlpha
                    lastNodeZ = lastNodeZ * (1f - posSmoothAlpha) + targetZ * posSmoothAlpha
                }
                followNode?.position = Position(lastNodeX, lastNodeY, lastNodeZ)

                // Kompensasi ukuran: jarak berubah (depth) tapi ukuran tampak tetap.
                // Ukuran dasar tetap diatur manual oleh user lewat tombol + / -.
                val sizeComp = currentScale * (lastUsedDistance / poseFollowDistance)
                followNode?.scale = Position(sizeComp, sizeComp, sizeComp)

                // Hadapkan model ke arah kamera (dengan smoothing yaw)
                val targetYaw = Math.toDegrees(
                    kotlin.math.atan2(
                        (cameraPose.tx() - lastNodeX).toDouble(),
                        (cameraPose.tz() - lastNodeZ).toDouble()
                    )
                ).toFloat()
                var dYaw = targetYaw - smoothedYaw
                while (dYaw > 180f) dYaw -= 360f
                while (dYaw < -180f) dYaw += 360f
                smoothedYaw += dYaw * 0.25f
                // Selalu hadap ke depan (ke arah kamera)
                followNode?.rotation = Rotation(0f, smoothedYaw, 0f)

                demoPoseApplied = false

                var boneCount = 0
                var boneSummary = ""
                detectionResult?.let { res ->
                    val overrides = computePoseOverrides(res, cameraPose)
                    boneCount = overrides.size
                    if (overrides.isNotEmpty()) {
                        boneSummary = followNode?.let { node ->
                            rigController?.applyBonesViaNodes(node, overrides)
                        } ?: ""
                    }
                }
                tvTrackingStatus.text =
                    "pose=$poseCount | tulang=$boneCount | $boneSummary"
            } else {
                // Tidak ada orang -> kembalikan Pokemon ke bentuk (pose) awalnya
                followNode?.let { rigController?.resetViaNodes(it) }
                demoPoseApplied = false
                tvTrackingStatus.text = "Tidak ada orang (pose=$poseCount) - bentuk awal"
            }
            isDetectingPose = false
        }
    }

    private data class ArmSpec(val side: String, val shoulder: Int, val elbow: Int, val wrist: Int)

    /** Hitung rotasi lokal tulang (badan, kepala, lengan) dari landmark orang. */
    private fun computePoseOverrides(
        result: PoseLandmarkerResult,
        cameraPose: Pose
    ): Map<Int, FloatArray> {
        val rig = rigController ?: return emptyMap()
        if (!rig.isLoaded) return emptyMap()
        val lm = result.landmarks().firstOrNull() ?: return emptyMap()
        if (lm.size < 33) return emptyMap()

        updateSmooth(lm)

        val overrides = HashMap<Int, FloatArray>()
        val identity = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)

        // BADAN & KEPALA: sengaja TIDAK di-override supaya kepala tetap diam total.
        // (Hanya lengan & tangan yang mengikuti gerakan.)

        // LENGAN
        val arms = listOf(ArmSpec("L", 11, 13, 15), ArmSpec("R", 12, 14, 16))
        for (arm in arms) {
            val upper = rig.skinIndexByPrefix("${arm.side}Arm")
            val fore = rig.skinIndexByPrefix("${arm.side}ForeArm")
            if (upper < 0 || fore < 0) continue

            val wUpper = rotateVecByPose(cameraPose, screenDirection(arm.shoulder, arm.elbow))
            val wFore = rotateVecByPose(cameraPose, screenDirection(arm.elbow, arm.wrist))
            val upperRestDir = restDirection(rig, upper) ?: continue
            val foreRestDir = restDirection(rig, fore) ?: continue

            val parentUpper = rig.parentOf(upper)
            val parentRot = if (parentUpper >= 0) RigPoseController.rotationFrom4x4(rig.restGlobalOf(parentUpper)) else identity
            val upperRestRot = RigPoseController.rotationFrom4x4(rig.restGlobalOf(upper))
            val foreRestRot = RigPoseController.rotationFrom4x4(rig.restGlobalOf(fore))

            val deltaUpper = RigPoseController.rotationFromTo(upperRestDir, wUpper)
            val desiredGlobalUpper = RigPoseController.mat3Mul(deltaUpper, upperRestRot)
            overrides[upper] = RigPoseController.mat3Mul(RigPoseController.mat3Transpose(parentRot), desiredGlobalUpper)

            val deltaFore = RigPoseController.rotationFromTo(foreRestDir, wFore)
            val desiredGlobalFore = RigPoseController.mat3Mul(deltaFore, foreRestRot)
            overrides[fore] = RigPoseController.mat3Mul(RigPoseController.mat3Transpose(desiredGlobalUpper), desiredGlobalFore)

            // Tangan (wrist -> ujung telunjuk) supaya gerakan tangan lebih hidup
            val hand = rig.skinIndexByPrefix("${arm.side}Hand")
            if (hand >= 0) {
                val indexTip = if (arm.side == "L") 19 else 20
                val wHand = rotateVecByPose(cameraPose, screenDirection(arm.wrist, indexTip))
                val handRestDir = restDirection(rig, hand)
                if (handRestDir != null) {
                    val handRestRot = RigPoseController.rotationFrom4x4(rig.restGlobalOf(hand))
                    val deltaHand = RigPoseController.rotationFromTo(handRestDir, wHand)
                    val desiredGlobalHand = RigPoseController.mat3Mul(deltaHand, handRestRot)
                    overrides[hand] = RigPoseController.mat3Mul(
                        RigPoseController.mat3Transpose(desiredGlobalFore), desiredGlobalHand
                    )
                }
            }
        }

        // KAKI & BADAN: dibiarkan normal (rest). Hanya lengan & tangan yang bergerak.

        return overrides
    }

    /** Low-pass filter posisi landmark agar gerakan lebih halus. */
    private fun updateSmooth(lm: List<NormalizedLandmark>) {
        if (!smoothInit) {
            for (i in 0 until 33) {
                smoothX[i] = lm[i].x()
                smoothY[i] = lm[i].y()
                smoothZ[i] = lm[i].z()
            }
            smoothInit = true
        } else {
            for (i in 0 until 33) {
                smoothX[i] = smoothX[i] * (1f - smoothAlpha) + lm[i].x() * smoothAlpha
                smoothY[i] = smoothY[i] * (1f - smoothAlpha) + lm[i].y() * smoothAlpha
                smoothZ[i] = smoothZ[i] * (1f - smoothAlpha) + lm[i].z() * smoothAlpha
            }
        }
    }

    private fun screenDirection(a: Int, b: Int): FloatArray {
        val dx = smoothX[b] - smoothX[a]
        val dy = smoothY[b] - smoothY[a]
        // Pengaruh kedalaman (MediaPipe z) untuk arah 3D.
        val dz = (smoothZ[b] - smoothZ[a]) * 0.6f
        return floatArrayOf(dx, -dy, -dz)
    }

    private fun screenDirectionBetweenMids(a1: Int, a2: Int, b1: Int, b2: Int): FloatArray {
        val ax = (smoothX[a1] + smoothX[a2]) / 2f
        val ay = (smoothY[a1] + smoothY[a2]) / 2f
        val bx = (smoothX[b1] + smoothX[b2]) / 2f
        val by = (smoothY[b1] + smoothY[b2]) / 2f
        return floatArrayOf(bx - ax, -(by - ay), 0f)
    }

    private fun rotateVecByPose(pose: Pose, v: FloatArray): FloatArray {
        val out = FloatArray(3)
        pose.rotateVector(v, 0, out, 0)
        return out
    }

    private fun restDirection(rig: RigPoseController, bone: Int): FloatArray? {
        val child = rig.firstChildOf(bone)
        if (child < 0) return null
        val b = rig.restGlobalOf(bone)
        val c = rig.restGlobalOf(child)
        return floatArrayOf(c[12] - b[12], c[13] - b[13], c[14] - b[14])
    }

    private fun mulMat4(a: FloatArray, b: FloatArray): FloatArray {
        val r = FloatArray(16)
        for (c in 0 until 4) {
            for (row in 0 until 4) {
                var sum = 0f
                for (k in 0 until 4) sum += a[k * 4 + row] * b[c * 4 + k]
                r[c * 4 + row] = sum
            }
        }
        return r
    }

    private fun computeScreenPoints(
        result: PoseLandmarkerResult,
        cameraPose: Pose,
        fx: Float,
        fy: Float,
        cx: Float,
        cy: Float,
        imgW: Float,
        imgH: Float,
        vp: FloatArray,
        viewW: Float,
        viewH: Float
    ): FloatArray? {
        val poses = result.landmarks()
        if (poses.isEmpty()) return null
        val lm = poses[0]
        if (lm.size < PoseOverlayView.LANDMARK_COUNT) return null

        val out = FloatArray(PoseOverlayView.LANDMARK_COUNT * 2)
        val d = poseFollowDistance
        for (i in 0 until PoseOverlayView.LANDMARK_COUNT) {
            val px = lm[i].x() * imgW
            val py = lm[i].y() * imgH
            val xCam = (px - cx) / fx
            val yCam = (py - cy) / fy
            val len = sqrt(xCam * xCam + yCam * yCam + 1f)
            val local = floatArrayOf(xCam / len * d, -yCam / len * d, -d / len)
            val world = FloatArray(3)
            cameraPose.transformPoint(local, 0, world, 0)

            val x = world[0]; val y = world[1]; val z = world[2]
            val clipX = vp[0] * x + vp[4] * y + vp[8] * z + vp[12]
            val clipY = vp[1] * x + vp[5] * y + vp[9] * z + vp[13]
            val clipW = vp[3] * x + vp[7] * y + vp[11] * z + vp[15]
            if (clipW <= 0.0001f) {
                out[i * 2] = 0f
                out[i * 2 + 1] = 0f
                continue
            }
            val ndcX = clipX / clipW
            val ndcY = clipY / clipW
            out[i * 2] = (ndcX * 0.5f + 0.5f) * viewW
            out[i * 2 + 1] = (1f - (ndcY * 0.5f + 0.5f)) * viewH
        }
        return out
    }

    /** Pusat badan (rata-rata bahu & pinggul) dalam koordinat gambar ternormalisasi. */
    private fun bodyCenter(result: PoseLandmarkerResult): FloatArray? {
        val lm = result.landmarks().firstOrNull() ?: return null
        if (lm.size < 25) return null
        val indices = intArrayOf(
            PoseTracker.LEFT_SHOULDER, PoseTracker.RIGHT_SHOULDER,
            PoseTracker.LEFT_HIP, PoseTracker.RIGHT_HIP
        )
        var sx = 0f
        var sy = 0f
        for (i in indices) {
            sx += lm[i].x()
            sy += lm[i].y()
        }
        return floatArrayOf(sx / indices.size, sy / indices.size)
    }

    /** Ambil nilai kedalaman (meter) dari depth image ARCore. */
    private fun sampleDepth(depthImage: android.media.Image, normX: Float, normY: Float): Float {
        return try {
            val dw = depthImage.width
            val dh = depthImage.height
            val px = (normX * dw).toInt().coerceIn(0, dw - 1)
            val py = (normY * dh).toInt().coerceIn(0, dh - 1)
            val plane = depthImage.planes[0]
            val buffer = plane.buffer
            val idx = py * plane.rowStride + px * plane.pixelStride
            if (idx + 1 >= buffer.limit()) return -1f
            val lo = buffer.get(idx).toInt() and 0xFF
            val hi = buffer.get(idx + 1).toInt() and 0xFF
            val mm = (hi shl 8) or lo
            if (mm > 0) mm / 1000f else -1f
        } catch (e: Exception) {
            -1f
        }
    }

    private fun computeFollowPosition(
        result: PoseLandmarkerResult,
        cameraPose: Pose,
        fx: Float,
        fy: Float,
        cx: Float,
        cy: Float,
        imgW: Float,
        imgH: Float,
        depthOverride: Float = -1f
    ): FloatArray? {
        val poses = result.landmarks()
        if (poses.isEmpty()) return null
        val lm = poses[0]
        if (lm.size < 25) return null

        val indices = intArrayOf(
            PoseTracker.LEFT_SHOULDER, PoseTracker.RIGHT_SHOULDER,
            PoseTracker.LEFT_HIP, PoseTracker.RIGHT_HIP
        )
        var sumX = 0f
        var sumY = 0f
        for (i in indices) {
            sumX += lm[i].x()
            sumY += lm[i].y()
        }
        val normX = sumX / indices.size
        val normY = sumY / indices.size

        val px = normX * imgW
        val py = normY * imgH
        val xCam = (px - cx) / fx
        val yCam = (py - cy) / fy
        val len = sqrt(xCam * xCam + yCam * yCam + 1f)
        // Pakai kedalaman nyata jika valid; kalau tidak, jarak tetap.
        val d = if (depthOverride in 0.3f..8f) depthOverride else poseFollowDistance
        lastUsedDistance = d
        val local = floatArrayOf(xCam / len * d, -yCam / len * d, -d / len)
        val out = FloatArray(3)
        cameraPose.transformPoint(local, 0, out, 0)
        return out
    }

    // ===== SCREENSHOT CAPTURE =====
    private fun captureScreenshot() {
        Toast.makeText(requireContext(), "Mengambil foto...", Toast.LENGTH_SHORT).show()
        
        // Simpan status awal dan sembunyikan grid
        val wasGridVisible = sceneView.planeRenderer.isVisible
        sceneView.planeRenderer.isVisible = false

        // Beri jeda sejenak (100ms) agar layar sempat me-render frame tanpa titik-titik grid
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val bitmap = Bitmap.createBitmap(
                    sceneView.width,
                    sceneView.height,
                    Bitmap.Config.ARGB_8888
                )

                PixelCopy.request(
                    sceneView,
                    bitmap,
                    { copyResult ->
                        // Kembalikan status grid ke awal
                        sceneView.planeRenderer.isVisible = wasGridVisible

                        if (copyResult == PixelCopy.SUCCESS) {
                            saveBitmapToGallery(bitmap)
                        } else {
                            Toast.makeText(requireContext(), "Gagal mengambil gambar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            } catch (e: Exception) {
                sceneView.planeRenderer.isVisible = wasGridVisible
                Log.e("CameraXRFragment", "Capture error", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, 100)
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "PokemonAR_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        var fos: OutputStream? = null
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (imageUri != null) {
            try {
                fos = resolver.openOutputStream(imageUri)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos!!)
                fos.close()
                Toast.makeText(requireContext(), "Foto disimpan ke Galeri!", Toast.LENGTH_LONG).show()

                // Save temp file for upload
                val cacheFile = java.io.File(requireContext().cacheDir, "temp_capture.jpg")
                val fosCache = java.io.FileOutputStream(cacheFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fosCache)
                fosCache.close()
                
                uploadToImgBB(cacheFile)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Gagal menulis file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Gagal menyimpan foto ke galeri", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadToImgBB(file: java.io.File) {
        // TODO: DAFTAR DI IMGBB (https://api.imgbb.com/) DAN GANTI API KEY INI DENGAN API KEY ANDA SENDIRI!
        // INI SANGAT MUDAH DAN GRATIS
        val apiKey = "15573c730a5e07fafb939deb3575b7d4" // PLACEHOLDER API KEY
        val client = OkHttpClient() 

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload?key=$apiKey")
            .post(requestBody)
            .build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Mengupload gambar ke server...", Toast.LENGTH_SHORT).show()
                }

                client.newCall(request).execute().use { response ->
                    val responseData = response.body?.string() ?: ""
                    
                    if (!response.isSuccessful) {
                        Log.e("ImgBBUpload", "Error: $responseData")
                        throw IOException("Ganti API Key di CameraXrFragment.kt baris 373 terlebih dahulu!")
                    }

                    val jsonObject = JSONObject(responseData)
                    val dataObj = jsonObject.getJSONObject("data")
                    val imageUrl = dataObj.getString("url")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Upload berhasil!", Toast.LENGTH_SHORT).show()
                        showQrCode(imageUrl)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ImgBBUpload", "Failed to upload", e)
                    Toast.makeText(requireContext(), "Gagal upload: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showQrCode(url: String) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            ivQrCode.setImageBitmap(bitmap)
            qrCodePanel.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e("CameraXRFragment", "QR Code Error", e)
            Toast.makeText(requireContext(), "Gagal membuat QR Code: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== ROTATE FUNCTION =====
    private fun rotateAllModels() {
        Log.d("XR_Rotate", "=== ROTATE BUTTON PRESSED ===")
        Log.d("XR_Rotate", "Model count: ${modelNodes.size}")

        if (modelNodes.isEmpty()) {
            Toast.makeText(requireContext(), "No models to rotate!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            modelNodes.forEachIndexed { index, node ->
                Log.d("XR_Rotate", "--- Rotating model $index ---")

                val currentAngle = modelRotationAngles[node] ?: 0f
                val newAngle = currentAngle + 45f
                modelRotationAngles[node] = newAngle

                Log.d("XR_Rotate", "Current angle: $currentAngle -> New angle: $newAngle")

                node.rotation = Rotation(
                    x = 0f,
                    y = newAngle,
                    z = 0f
                )
            }

            Toast.makeText(requireContext(), "🔄 Rotated 45°", Toast.LENGTH_SHORT).show()
            sceneView.invalidate()

            Log.d("XR_Rotate", "=== ROTATE COMPLETE ===")

        } catch (e: Exception) {
            Log.e("XR_Rotate", "Error during rotation", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== SPAWN POKEMON =====
    private fun spawnPokemonAtHitResult(hitResult: HitResult) {
        try {
            val modelPath = getModelPath()
            val pokemonName = selectedPokemon?.name ?: "Pokemon"

            Toast.makeText(requireContext(), "Summoning $pokemonName...", Toast.LENGTH_SHORT).show()
            Log.d("XR", "Loading $modelPath")

            val modelInstance = sceneView.modelLoader.createModelInstance(
                assetFileLocation = modelPath
            )

            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(sceneView.engine, anchor).apply {
                // Mengaktifkan fitur drag-and-drop agar bisa dipindahkan (hold & drag)
                isPositionEditable = true
            }

            val initialYRotation = 0f

            val newNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 0.01f,
                centerOrigin = Position(y = -0.5f)
            ).apply {
                rotation = Rotation(
                    x = 0f,
                    y = initialYRotation,
                    z = 0f
                )
                // Memastikan child node juga bisa disentuh untuk didrag
                isPositionEditable = true
                isShadowCaster = false
                isShadowReceiver = false
            }

            anchorNode.addChildNode(newNode)
            sceneView.addChildNode(anchorNode)

            anchorNodes.add(anchorNode)
            modelNodes.add(newNode)
            modelRotationAngles[newNode] = initialYRotation

            Log.d("XR", "Model added. Total: ${modelNodes.size}")
            Log.d("XR", "Initial rotation: ${newNode.rotation}")

            val scaleAnimator = ValueAnimator.ofFloat(0.01f, currentScale)
            scaleAnimator.duration = 500
            scaleAnimator.interpolator = OvershootInterpolator(1.5f)
            scaleAnimator.addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                newNode.scale = Position(scale, scale, scale)
            }
            scaleAnimator.start()

            updateUIState()

            Toast.makeText(
                requireContext(),
                "✅ $pokemonName appears! (${modelNodes.size} total)",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Log.e("XR", "ERROR LOADING MODEL", e)

            // Fallback ke pikachu
            try {
                Toast.makeText(requireContext(), "Trying default model...", Toast.LENGTH_SHORT).show()

                val modelInstance = sceneView.modelLoader.createModelInstance(
                    assetFileLocation = "models/pikachu.glb"
                )

                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(sceneView.engine, anchor)

                val newNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 0.01f,
                    centerOrigin = Position(y = -0.5f)
                ).apply {
                    rotation = Rotation(x = 0f, y = 0f, z = 0f)
                    isShadowCaster = true
                    isShadowReceiver = true
                }

                anchorNode.addChildNode(newNode)
                sceneView.addChildNode(anchorNode)

                anchorNodes.add(anchorNode)
                modelNodes.add(newNode)
                modelRotationAngles[newNode] = 0f

                val scaleAnimator = ValueAnimator.ofFloat(0.01f, currentScale)
                scaleAnimator.duration = 500
                scaleAnimator.interpolator = OvershootInterpolator(1.5f)
                scaleAnimator.addUpdateListener { animation ->
                    val scale = animation.animatedValue as Float
                    newNode.scale = Position(scale, scale, scale)
                }
                scaleAnimator.start()

                updateUIState()

                Toast.makeText(requireContext(), "⚠️ Using default model", Toast.LENGTH_SHORT).show()

            } catch (e2: Exception) {
                Log.e("XR", "FALLBACK ALSO FAILED", e2)
                Toast.makeText(requireContext(), "Error: ${e2.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ===== REMOVE FUNCTIONS =====
    private fun removeLastModel() {
        if (followNode != null) {
            removeFollowNode()
            updateUIState()
            Toast.makeText(requireContext(), "Model tracking dihapus", Toast.LENGTH_SHORT).show()
            return
        }

        if (modelNodes.isNotEmpty()) {
            val lastNode = modelNodes.removeAt(modelNodes.size - 1)
            modelRotationAngles.remove(lastNode)
            if (anchorNodes.isNotEmpty()) {
                val lastAnchorNode = anchorNodes.removeAt(anchorNodes.size - 1)
                sceneView.removeChildNode(lastAnchorNode)
            }
            spawnCounter--
            updateUIState()
            Toast.makeText(requireContext(), "Last model removed (${modelNodes.size} left)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No models!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeAllModels() {
        anchorNodes.forEach { node ->
            sceneView.removeChildNode(node)
        }
        anchorNodes.clear()
        modelNodes.clear()
        modelRotationAngles.clear()
        spawnCounter = 0

        followNode?.let { sceneView.removeChildNode(it) }
        followNode = null
        rigController = null
        demoPoseApplied = false

        selectedPokemon = null
        currentScale = 0.15f
        tvSelectedPokemon.visibility = View.GONE

        updateUIState()
        Toast.makeText(requireContext(), "All models removed", Toast.LENGTH_SHORT).show()
    }

    private fun resetAllModels() {
        if (modelNodes.isNotEmpty()) {
            currentScale = 0.15f
            modelNodes.forEach { node ->
                node.scale = Position(currentScale, currentScale, currentScale)
                node.rotation = Rotation(x = 0f, y = 0f, z = 0f)
                modelRotationAngles[node] = 0f
            }
            Toast.makeText(requireContext(), "All reset to default", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No models!", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== SELECTION PANEL =====
    private fun showPokemonSelectionPanel() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Cancel previous job if exists
        loadPokemonJob?.cancel()

        viewModel.loadPokemon(userId)
    }

    private fun onPokemonSelected(pokemon: PokemonEntity) {
        pokemonSelectionPanel.visibility = View.GONE
        selectedPokemon = pokemon

        btnSelectPokemon.text = "📱 Pilih Pokemon Lain"
        tvSelectedPokemon.text = "👉 Ketuk lantai terdeteksi untuk memunculkan ${pokemon.name}!"
        tvSelectedPokemon.visibility = View.VISIBLE

        Toast.makeText(requireContext(), "Silakan ketuk lantai terdeteksi untuk memunculkan ${pokemon.name}!", Toast.LENGTH_LONG).show()

        if (currentMode == XrMode.TRACKING) {
            removeFollowNode()
            ensureFollowNode()
        }
    }

    // ===== HELPERS =====
    private fun getModelPath(): String {
        val pokemon = selectedPokemon
        if (pokemon == null) return "models/pikachu.glb"
        val pokemonName = pokemon.name.lowercase().trim()
        return "models/$pokemonName.glb"
    }

    private fun applyScaleToAll() {
        modelNodes.forEach { node ->
            node.scale = Position(currentScale, currentScale, currentScale)
        }
        followNode?.scale = Position(currentScale, currentScale, currentScale)
    }

    private fun updateUIState() {
        val hasModels = modelNodes.isNotEmpty() || followNode != null

        btnRotate.isEnabled = hasModels
        btnScaleUp.isEnabled = hasModels
        btnScaleDown.isEnabled = hasModels
        btnReset.isEnabled = hasModels
        btnRemove.isEnabled = hasModels
        btnRemoveAll.isEnabled = hasModels

        if (hasModels) {
            val count = modelNodes.size + (if (followNode != null) 1 else 0)
            tvModelCount.text = "🦖 $count Pokemon(s)"
            tvModelCount.visibility = View.VISIBLE
        } else {
            tvModelCount.visibility = View.GONE
        }
    }

    // ===== FIXED LIFECYCLE METHODS =====

    override fun onDestroyView() {
        super.onDestroyView()

        viewDestroyed = true
        isDetectingPose = true

        try {
            sceneView.onSessionUpdated = null
        } catch (_: Exception) {
        }

        // Cancel any pending coroutines
        loadPokemonJob?.cancel()
        loadPokemonJob = null

        try {
            removeFollowNode()
        } catch (_: Exception) {
        }
        try {
            poseTracker?.close()
        } catch (_: Exception) {
        }
        poseTracker = null

        // Remove all models first
        try {
            anchorNodes.forEach { node ->
                sceneView.removeChildNode(node)
            }
            anchorNodes.clear()
            modelNodes.clear()
            modelRotationAngles.clear()
        } catch (e: Exception) {
            Log.e("CameraXRFragment", "Error removing models", e)
        }

        // Clear adapter reference
        selectionAdapter = null
    }
}