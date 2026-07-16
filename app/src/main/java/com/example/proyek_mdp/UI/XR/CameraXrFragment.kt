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
import com.google.ar.core.HitResult
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

    // Mode & Capture UI
    private lateinit var btnModePlace: Button
    private lateinit var btnModeCapture: Button
    private lateinit var btnCapture: Button
    private lateinit var topControls: LinearLayout
    private lateinit var bottomControls: LinearLayout
    private lateinit var captureContainer: FrameLayout
    private var isCaptureMode = false

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

    private val viewModel: CameraViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    // ===== LIFECYCLE =====
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        btnModePlace = view.findViewById(R.id.btnModePlace)
        btnModeCapture = view.findViewById(R.id.btnModeCapture)
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
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
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
            if (modelNodes.isNotEmpty()) {
                currentScale = (currentScale * 1.2f).coerceIn(minScale, maxScale)
                modelNodes.forEach { node ->
                    node.scale = Position(currentScale, currentScale, currentScale)
                }
                Toast.makeText(requireContext(), "Scale: ${"%.2f".format(currentScale)}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No models!", Toast.LENGTH_SHORT).show()
            }
        }

        btnScaleDown.setOnClickListener {
            if (modelNodes.isNotEmpty()) {
                currentScale = (currentScale * 0.8f).coerceIn(minScale, maxScale)
                modelNodes.forEach { node ->
                    node.scale = Position(currentScale, currentScale, currentScale)
                }
                Toast.makeText(requireContext(), "Scale: ${"%.2f".format(currentScale)}", Toast.LENGTH_SHORT).show()
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
            switchMode(false)
        }

        btnModeCapture.setOnClickListener {
            switchMode(true)
        }

        btnCapture.setOnClickListener {
            captureScreenshot()
        }

        btnCloseQr.setOnClickListener {
            qrCodePanel.visibility = View.GONE
        }
    }

    private fun switchMode(toCaptureMode: Boolean) {
        isCaptureMode = toCaptureMode
        if (isCaptureMode) {
            btnModeCapture.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
            btnModePlace.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            topControls.visibility = View.GONE
            bottomControls.visibility = View.GONE
            captureContainer.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Capture Mode Active", Toast.LENGTH_SHORT).show()
        } else {
            btnModePlace.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark, null))
            btnModeCapture.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            topControls.visibility = View.VISIBLE
            bottomControls.visibility = View.VISIBLE
            captureContainer.visibility = View.GONE
            Toast.makeText(requireContext(), "Placement Mode Active", Toast.LENGTH_SHORT).show()
        }
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
    }

    // ===== HELPERS =====
    private fun getModelPath(): String {
        val pokemon = selectedPokemon
        if (pokemon == null) return "models/pikachu.glb"
        val pokemonName = pokemon.name.lowercase().trim()
        return "models/$pokemonName.glb"
    }

    private fun updateUIState() {
        val hasModels = modelNodes.isNotEmpty()

        btnRotate.isEnabled = hasModels
        btnScaleUp.isEnabled = hasModels
        btnScaleDown.isEnabled = hasModels
        btnReset.isEnabled = hasModels
        btnRemove.isEnabled = hasModels
        btnRemoveAll.isEnabled = hasModels

        if (hasModels) {
            tvModelCount.text = "🦖 ${modelNodes.size} Pokemon(s)"
            tvModelCount.visibility = View.VISIBLE
        } else {
            tvModelCount.visibility = View.GONE
        }
    }

    // ===== FIXED LIFECYCLE METHODS =====

    override fun onDestroyView() {
        super.onDestroyView()

        // Cancel any pending coroutines
        loadPokemonJob?.cancel()
        loadPokemonJob = null

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