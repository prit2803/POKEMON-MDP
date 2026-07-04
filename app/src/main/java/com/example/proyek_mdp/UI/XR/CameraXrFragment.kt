package com.example.proyek_mdp.UI.XR

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Adapter.PokemonSelectionAdapter
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import com.example.proyek_mdp.UI.Database.PokemonEntity
import com.example.proyek_mdp.auth.SessionManager
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import java.util.UUID

class CameraXRFragment : Fragment(R.layout.fragment_camera_xr) {

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

    private lateinit var sessionManager: SessionManager
    private var selectionAdapter: PokemonSelectionAdapter? = null

    // MENYIMPAN BANYAK MODEL NODE
    private val modelNodes = mutableListOf<ModelNode>()
    private var pokeballNode: ModelNode? = null

    // Scale settings
    private var currentScale = 0.15f
    private var minScale = 0.05f
    private var maxScale = 2.0f

    private var selectedPokemon: PokemonEntity? = null
    private var spawnCounter = 0  // Untuk posisi spawn bergantian

    // Pokeball drag variables
    private var isPokeballVisible = false
    private var pokeballInitialY = 0f
    private var dragStartY = 0f
    private var isDragging = false
    private var pokeballRotationAngle = 0f

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize SessionManager
        sessionManager = SessionManager(requireContext())

        // Initialize views
        initViews(view)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup AR Scene
        setupARScene()

        // Setup listeners
        setupListeners()

        // Setup Pokeball interaction
        setupPokeball3DInteraction()

        // Initial state
        updateUIState()
    }

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
    }

    private fun setupRecyclerView() {
        recyclerViewPokemonSelection.layoutManager = LinearLayoutManager(requireContext())
        selectionAdapter = PokemonSelectionAdapter(emptyList()) { pokemon ->
            onPokemonSelected(pokemon)
        }
        recyclerViewPokemonSelection.adapter = selectionAdapter
    }

    private fun setupARScene() {
        sceneView.planeRenderer.isEnabled = true
        sceneView.planeRenderer.isShadowReceiver = true
        sceneView.planeRenderer.isVisible = false

        try {
            sceneView.configureSession { session, config ->
                config.lightEstimationMode =
                    com.google.ar.core.Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode =
                    com.google.ar.core.Config.PlaneFindingMode.HORIZONTAL
                config.updateMode =
                    com.google.ar.core.Config.UpdateMode.LATEST_CAMERA_IMAGE
            }
        } catch (e: Exception) {
            Log.e("CameraXRFragment", "Error configuring session", e)
        }
    }

    private fun setupListeners() {

        // Select Pokemon - selalu bisa meskipun sudah ada model
        btnSelectPokemon.setOnClickListener {
            showPokemonSelectionPanel()
        }

        btnCloseSelection.setOnClickListener {
            pokemonSelectionPanel.visibility = View.GONE
        }

        btnRotate.setOnClickListener {
            // Rotate semua model
            if (modelNodes.isNotEmpty()) {
                modelNodes.forEach { node ->
                    val currentRotation = node.rotation
                    node.rotation = Rotation(
                        x = currentRotation.x,
                        y = currentRotation.y + 45f,
                        z = currentRotation.z
                    )
                }
                Toast.makeText(requireContext(), "All rotated 45°", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "No models!", Toast.LENGTH_SHORT).show()
            }
        }

        btnScaleUp.setOnClickListener {
            if (modelNodes.isNotEmpty()) {
                currentScale = (currentScale * 1.2f).coerceIn(minScale, maxScale)
                modelNodes.forEach { node ->
                    node.scale = Position(currentScale, currentScale, currentScale)
                }
                Toast.makeText(requireContext(), "Scale: ${"%.2f".format(currentScale)}", Toast.LENGTH_SHORT).show()
            }
        }

        btnScaleDown.setOnClickListener {
            if (modelNodes.isNotEmpty()) {
                currentScale = (currentScale * 0.8f).coerceIn(minScale, maxScale)
                modelNodes.forEach { node ->
                    node.scale = Position(currentScale, currentScale, currentScale)
                }
                Toast.makeText(requireContext(), "Scale: ${"%.2f".format(currentScale)}", Toast.LENGTH_SHORT).show()
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
    }

    /**
     * Setup interaksi 3D Pokeball dengan touch screen
     */
    private fun setupPokeball3DInteraction() {
        sceneView.setOnTouchListener { view, event ->
            if (!isPokeballVisible || pokeballNode == null) {
                return@setOnTouchListener false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragStartY = event.rawY
                    pokeballInitialY = pokeballNode?.worldPosition?.y ?: 0f
                    isDragging = true

                    // Scale up sedikit saat di-touch
                    pokeballNode?.scale = Position(0.18f, 0.18f, 0.18f)

                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDragging && pokeballNode != null) {
                        val deltaY = dragStartY - event.rawY

                        val newY = pokeballInitialY + (deltaY / 500f)
                        pokeballNode?.worldPosition = Position(
                            x = pokeballNode?.worldPosition?.x ?: 0f,
                            y = newY.coerceIn(-1f, 0f),
                            z = pokeballNode?.worldPosition?.z ?: -0.8f
                        )

                        pokeballRotationAngle += deltaY * 0.5f
                        pokeballNode?.rotation = Rotation(
                            x = pokeballRotationAngle,
                            y = 0f,
                            z = 0f
                        )

                        // Jika di-swipe ke atas cukup jauh, summon Pokemon
                        if (deltaY > 150) {
                            isDragging = false
                            summonPokemonWithAnimation()
                            true
                        }
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        isDragging = false

                        // Kembalikan Pokeball ke posisi awal
                        pokeballNode?.let { node ->
                            val animator = android.animation.ValueAnimator.ofFloat(
                                node.worldPosition.y,
                                pokeballInitialY
                            )
                            animator.duration = 300
                            animator.interpolator = AccelerateDecelerateInterpolator()
                            animator.addUpdateListener { animation ->
                                node.worldPosition = Position(
                                    x = node.worldPosition.x,
                                    y = animation.animatedValue as Float,
                                    z = node.worldPosition.z
                                )
                            }
                            animator.start()
                        }

                        // Kembalikan scale
                        pokeballNode?.scale = Position(0.15f, 0.15f, 0.15f)
                    }
                    true
                }

                else -> false
            }
        }
    }

    /**
     * Animasi melempar Pokeball 3D dan mensummon Pokemon
     */
    private fun summonPokemonWithAnimation() {
        pokeballNode?.let { node ->
            val animator = android.animation.ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 800
            animator.addUpdateListener { animation ->
                val progress = animation.animatedValue as Float

                val currentY = pokeballInitialY + (progress * 2f)
                node.worldPosition = Position(
                    x = node.worldPosition.x,
                    y = currentY,
                    z = node.worldPosition.z - (progress * 2f)
                )

                node.rotation = Rotation(
                    x = pokeballRotationAngle + (progress * 720f),
                    y = progress * 360f,
                    z = progress * 180f
                )

                val scale = 0.15f * (1f - progress)
                node.scale = Position(scale, scale, scale)
            }

            animator.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // Jangan hapus Pokeball, biarkan tetap ada untuk summon lagi
                    resetPokeballPosition()

                    // Spawn Pokemon baru
                    spawnPokemon()
                }

                override fun onAnimationCancel(animation: android.animation.Animator) {}
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })

            animator.start()
        }
    }

    /**
     * Reset posisi Pokeball untuk siap digunakan lagi
     */
    private fun resetPokeballPosition() {
        pokeballNode?.let { node ->
            node.worldPosition = Position(
                x = 0f,
                y = -0.8f,
                z = -0.8f
            )
            node.rotation = Rotation(x = 0f, y = 0f, z = 0f)
            node.scale = Position(0.15f, 0.15f, 0.15f)

            // Animasi bounce ringan lagi
            val bounceAnimator = android.animation.ValueAnimator.ofFloat(-0.8f, -0.75f, -0.8f)
            bounceAnimator.duration = 1000
            bounceAnimator.repeatCount = android.animation.ValueAnimator.INFINITE
            bounceAnimator.repeatMode = android.animation.ValueAnimator.REVERSE
            bounceAnimator.addUpdateListener { animation ->
                pokeballNode?.worldPosition = Position(
                    x = 0f,
                    y = animation.animatedValue as Float,
                    z = -0.8f
                )
            }
            bounceAnimator.start()

            tvSelectedPokemon.text = "⬆️ Swipe up to summon another ${selectedPokemon?.name}!"
        }
    }

    /**
     * Spawn Pokemon baru (bisa multiple)
     */
    private fun spawnPokemon() {
        try {
            val modelPath = getModelPath()
            val pokemonName = selectedPokemon?.name ?: "Pokemon"

            Toast.makeText(requireContext(), "Summoning $pokemonName...", Toast.LENGTH_SHORT).show()
            Log.d("XR", "Loading $modelPath")

            val modelInstance = sceneView.modelLoader.createModelInstance(
                assetFileLocation = modelPath
            )

            Log.d("XR", "Model loaded. Material count = ${modelInstance.materialInstances.size}")

            // Posisi spawn bergantian (kiri, tengah, kanan)
            spawnCounter++
            val xOffset = when (spawnCounter % 3) {
                0 -> 0.3f   // Kanan
                1 -> 0f     // Tengah
                else -> -0.3f // Kiri
            }

            val zOffset = (spawnCounter / 3) * 0.3f  // Mundur setiap 3 spawn

            val newNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 0.01f, // Mulai dari sangat kecil untuk pop effect
                centerOrigin = Position(y = -0.5f)
            ).apply {
                worldPosition = Position(
                    x = xOffset,
                    y = -0.3f,
                    z = -0.5f - zOffset
                )

                rotation = Rotation(
                    x = 0f,
                    y = (spawnCounter * 30f) % 360f, // Rotasi sedikit berbeda
                    z = 0f
                )

                isShadowCaster = true
                isShadowReceiver = true
            }

            sceneView.addChildNode(newNode)
            modelNodes.add(newNode)  // TAMBAHKAN KE LIST

            // Animasi pop effect
            val scaleAnimator = android.animation.ValueAnimator.ofFloat(0.01f, currentScale)
            scaleAnimator.duration = 500
            scaleAnimator.interpolator = android.view.animation.OvershootInterpolator(1.5f)
            scaleAnimator.addUpdateListener { animation ->
                val scale = animation.animatedValue as Float
                newNode.scale = Position(scale, scale, scale)
            }
            scaleAnimator.start()

            updateUIState()

            Log.d("XR", "MODEL LOADED SUCCESSFULLY (Total: ${modelNodes.size})")
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

                spawnCounter++
                val xOffset = when (spawnCounter % 3) {
                    0 -> 0.3f
                    1 -> 0f
                    else -> -0.3f
                }

                val newNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 0.01f,
                    centerOrigin = Position(y = -0.5f)
                ).apply {
                    worldPosition = Position(x = xOffset, y = -0.3f, z = -0.5f)
                    rotation = Rotation(x = 0f, y = 0f, z = 0f)
                    isShadowCaster = true
                    isShadowReceiver = true
                }

                sceneView.addChildNode(newNode)
                modelNodes.add(newNode)

                // Pop effect
                val scaleAnimator = android.animation.ValueAnimator.ofFloat(0.01f, currentScale)
                scaleAnimator.duration = 500
                scaleAnimator.interpolator = android.view.animation.OvershootInterpolator(1.5f)
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

    /**
     * Hapus model terakhir
     */
    private fun removeLastModel() {
        if (modelNodes.isNotEmpty()) {
            val lastNode = modelNodes.removeAt(modelNodes.size - 1)
            sceneView.removeChildNode(lastNode)
            spawnCounter--
            updateUIState()
            Toast.makeText(requireContext(), "Last model removed (${modelNodes.size} left)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No models!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Hapus semua model
     */
    private fun removeAllModels() {
        modelNodes.forEach { node ->
            sceneView.removeChildNode(node)
        }
        modelNodes.clear()
        spawnCounter = 0

        // Hapus juga Pokeball
        pokeballNode?.let {
            sceneView.removeChildNode(it)
            pokeballNode = null
        }

        selectedPokemon = null
        currentScale = 0.15f
        isPokeballVisible = false
        tvSelectedPokemon.visibility = View.GONE

        updateUIState()
        Toast.makeText(requireContext(), "All models removed", Toast.LENGTH_SHORT).show()
    }

    /**
     * Reset semua model ke ukuran awal
     */
    private fun resetAllModels() {
        if (modelNodes.isNotEmpty()) {
            currentScale = 0.15f
            modelNodes.forEach { node ->
                node.scale = Position(currentScale, currentScale, currentScale)
            }
            Toast.makeText(requireContext(), "All reset to default size", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPokemonSelectionPanel() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val database = PokemonDatabase.getDatabase(requireContext())
                val pokemonList = database.pokemonDao().getPokemonByUser(userId)

                if (!isAdded) return@launch

                if (pokemonList.isEmpty()) {
                    Toast.makeText(requireContext(), "Koleksi Pokemon kosong!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                selectionAdapter?.updateData(pokemonList)
                pokemonSelectionPanel.visibility = View.VISIBLE

            } catch (e: Exception) {
                Log.e("CameraXR", "Error loading pokemon list", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onPokemonSelected(pokemon: PokemonEntity) {
        // Tutup panel selection
        pokemonSelectionPanel.visibility = View.GONE

        // Simpan pokemon yang dipilih
        selectedPokemon = pokemon

        // Hapus Pokeball lama jika ada dan buat baru
        pokeballNode?.let {
            sceneView.removeChildNode(it)
            pokeballNode = null
        }

        // Spawn 3D Pokeball baru
        spawn3DPokeball(pokemon)

        // Update UI
        btnSelectPokemon.text = "📱 Pilih Pokemon Lain"

        Toast.makeText(requireContext(), "${pokemon.name} ready! Swipe Pokeball up to spawn!", Toast.LENGTH_LONG).show()
    }

    /**
     * Spawn 3D Pokeball di scene
     */
    private fun spawn3DPokeball(pokemon: PokemonEntity) {
        try {
            val pokeballModel = sceneView.modelLoader.createModelInstance(
                assetFileLocation = "models/pokeball.glb"
            )

            pokeballNode = ModelNode(
                modelInstance = pokeballModel,
                scaleToUnits = 0.15f,
                centerOrigin = Position(y = 0f)
            ).apply {
                worldPosition = Position(
                    x = 0f,
                    y = -0.8f,
                    z = -0.8f
                )

                rotation = Rotation(x = 0f, y = 0f, z = 0f)
                isShadowCaster = true
                isShadowReceiver = true
            }

            sceneView.addChildNode(pokeballNode!!)
            isPokeballVisible = true

            // Tampilkan instruksi
            tvSelectedPokemon.text = "⬆️ Swipe Pokeball to summon ${pokemon.name}!"
            tvSelectedPokemon.visibility = View.VISIBLE
            tvSelectedPokemon.alpha = 1f

            // Animasi bounce
            val bounceAnimator = android.animation.ValueAnimator.ofFloat(-0.8f, -0.75f, -0.8f)
            bounceAnimator.duration = 1000
            bounceAnimator.repeatCount = android.animation.ValueAnimator.INFINITE
            bounceAnimator.repeatMode = android.animation.ValueAnimator.REVERSE
            bounceAnimator.addUpdateListener { animation ->
                pokeballNode?.worldPosition = Position(
                    x = 0f,
                    y = animation.animatedValue as Float,
                    z = -0.8f
                )
            }
            bounceAnimator.start()

        } catch (e: Exception) {
            Log.e("Pokeball", "Failed to load 3D Pokeball", e)
            Toast.makeText(requireContext(), "Pokeball model not found, direct spawn mode", Toast.LENGTH_SHORT).show()
            // Langsung spawn Pokemon tanpa Pokeball
            spawnPokemon()
        }
    }

    private fun getModelPath(): String {
        val pokemon = selectedPokemon
        if (pokemon == null) return "models/pikachu.glb"

        val pokemonName = pokemon.name.lowercase().trim()
        return "models/$pokemonName.glb"
    }

    /**
     * Update UI state berdasarkan jumlah model
     */
    private fun updateUIState() {
        val hasModels = modelNodes.isNotEmpty()

        btnRotate.isEnabled = hasModels
        btnScaleUp.isEnabled = hasModels
        btnScaleDown.isEnabled = hasModels
        btnReset.isEnabled = hasModels
        btnRemove.isEnabled = hasModels
        btnRemoveAll.isEnabled = hasModels

        // Update counter text
        if (hasModels) {
            tvModelCount.text = "🦖 ${modelNodes.size} Pokemon(s)"
            tvModelCount.visibility = View.VISIBLE
        } else {
            tvModelCount.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cleanup semua model
        modelNodes.forEach { sceneView.removeChildNode(it) }
        modelNodes.clear()
        pokeballNode?.let { sceneView.removeChildNode(it) }
    }
}