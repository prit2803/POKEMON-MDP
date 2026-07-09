package com.example.proyek_mdp.UI.XR

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
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
import androidx.fragment.app.viewModels
import com.example.proyek_mdp.viewmodel.CameraViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.auth.SessionManager
import com.google.ar.core.Config
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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

    // ===== VARIABLES =====
    private lateinit var sessionManager: SessionManager
    private var selectionAdapter: PokemonSelectionAdapter? = null

    // Model nodes
    private val modelNodes = mutableListOf<ModelNode>()

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
            sceneView.planeRenderer.isVisible = false

            sceneView.configureSession { session, config ->
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
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
    private fun spawnPokemon() {
        try {
            val modelPath = getModelPath()
            val pokemonName = selectedPokemon?.name ?: "Pokemon"

            Toast.makeText(requireContext(), "Summoning $pokemonName...", Toast.LENGTH_SHORT).show()
            Log.d("XR", "Loading $modelPath")

            val modelInstance = sceneView.modelLoader.createModelInstance(
                assetFileLocation = modelPath
            )

            spawnCounter++

            val xOffset = when (spawnCounter % 3) {
                0 -> 0.3f
                1 -> 0f
                else -> -0.3f
            }
            val zOffset = (spawnCounter / 3) * 0.3f

            val initialYRotation = 0f

            val newNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 0.01f,
                centerOrigin = Position(y = -0.5f)
            ).apply {
                worldPosition = Position(
                    x = xOffset,
                    y = -0.3f,
                    z = -0.5f - zOffset
                )
                rotation = Rotation(
                    x = 0f,
                    y = initialYRotation,
                    z = 0f
                )
                isShadowCaster = false
                isShadowReceiver = false
            }

            sceneView.addChildNode(newNode)
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
            sceneView.removeChildNode(lastNode)
            spawnCounter--
            updateUIState()
            Toast.makeText(requireContext(), "Last model removed (${modelNodes.size} left)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No models!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeAllModels() {
        modelNodes.forEach { node ->
            sceneView.removeChildNode(node)
        }
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

        spawnPokemon()

        btnSelectPokemon.text = "📱 Pilih Pokemon Lain"
        tvSelectedPokemon.text = "✅ ${pokemon.name} summoned!"
        tvSelectedPokemon.visibility = View.VISIBLE

        Toast.makeText(requireContext(), "${pokemon.name} summoned! Click again to add more!", Toast.LENGTH_SHORT).show()
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

    override fun onPause() {
        super.onPause()
        // Pause AR session when fragment is paused
        try {
            // Try to pause the session if available
            sceneView.session?.pause()
        } catch (e: Exception) {
            Log.e("CameraXRFragment", "Error pausing session", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Resume AR session when fragment is resumed
        try {
            // Try to resume the session if available
            sceneView.session?.resume()
        } catch (e: Exception) {
            Log.e("CameraXRFragment", "Error resuming session", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Cancel any pending coroutines
        loadPokemonJob?.cancel()
        loadPokemonJob = null

        // Remove all models first
        try {
            modelNodes.forEach { node ->
                sceneView.removeChildNode(node)
            }
            modelNodes.clear()
            modelRotationAngles.clear()
        } catch (e: Exception) {
            Log.e("CameraXRFragment", "Error removing models", e)
        }

        // Clear adapter reference
        selectionAdapter = null
    }
}