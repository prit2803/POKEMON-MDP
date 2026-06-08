package com.example.proyek_mdp.UI.XR

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.proyek_mdp.R
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode

class CameraXRFragment : Fragment(R.layout.fragment_camera_xr) {

    private lateinit var sceneView: ARSceneView
    private lateinit var btnSpawn: Button
    private lateinit var btnRotate: Button
    private lateinit var btnScaleUp: Button
    private lateinit var btnScaleDown: Button
    private lateinit var btnReset: Button
    private lateinit var btnRemove: Button

    private var modelNode: ModelNode? = null
    private var currentScale = 0.5f

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        sceneView = view.findViewById(R.id.sceneView)
        btnSpawn = view.findViewById(R.id.btnSpawn)
        btnRotate = view.findViewById(R.id.btnRotate)
        btnScaleUp = view.findViewById(R.id.btnScaleUp)
        btnScaleDown = view.findViewById(R.id.btnScaleDown)
        btnReset = view.findViewById(R.id.btnReset)
        btnRemove = view.findViewById(R.id.btnRemove)

        // Setup AR Scene
        sceneView.planeRenderer.isEnabled = true
        sceneView.planeRenderer.isShadowReceiver = true

        // Configure AR session for better lighting
        try {
            sceneView.configureSession { session, config ->
                config.lightEstimationMode =
                    com.google.ar.core.Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.planeFindingMode =
                    com.google.ar.core.Config.PlaneFindingMode.HORIZONTAL
            }
        } catch (e: Exception) {
            Log.e("CameraXRFragment", "Error configuring session", e)
        }

        // Setup button listeners
        setupListeners()

        // Initial state
        enableControls(false)
    }

    private fun setupListeners() {
        btnSpawn.setOnClickListener {
            if (modelNode == null) {
                loadModel()
            } else {
                Toast.makeText(requireContext(), "Model already loaded!", Toast.LENGTH_SHORT).show()
            }
        }

        btnRotate.setOnClickListener {
            modelNode?.let { node ->
                val currentRotation = node.rotation
                node.rotation = Rotation(
                    x = currentRotation.x,
                    y = currentRotation.y + 45f,
                    z = currentRotation.z
                )
                Toast.makeText(requireContext(), "Rotated 45°", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(requireContext(), "No model!", Toast.LENGTH_SHORT).show()
        }

        btnScaleUp.setOnClickListener {

            modelNode?.let {

                currentScale =
                    (currentScale * 1.2f)
                        .coerceIn(0.1f, 5f)

                it.scale = Position(
                    currentScale,
                    currentScale,
                    currentScale
                )
            }
        }

        btnScaleDown.setOnClickListener {

            modelNode?.let {

                currentScale =
                    (currentScale * 0.8f)
                        .coerceIn(0.1f, 5f)

                it.scale = Position(
                    currentScale,
                    currentScale,
                    currentScale
                )
            }
        }

        btnScaleDown.setOnClickListener {

            modelNode?.let {

                currentScale =
                    (currentScale * 0.8f)
                        .coerceIn(0.1f, 5f)

                it.scale = Position(
                    currentScale,
                    currentScale,
                    currentScale
                )
            }
        }

        btnRemove.setOnClickListener {
            modelNode?.let { node ->
                sceneView.removeChildNode(node)
                modelNode = null
                currentScale = 0.5f
                enableControls(false)
                Toast.makeText(requireContext(), "Model removed", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(requireContext(), "No model!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadModel() {
        try {
            Toast.makeText(requireContext(), "Loading model...", Toast.LENGTH_SHORT).show()
            Log.d("XR", "Loading models/box.glb")

            val modelInstance = sceneView.modelLoader.createModelInstance(
                assetFileLocation = "models/test.glb"
            )

            Log.d(
                "XR",
                "Model loaded. Material count = ${modelInstance.materialInstances.size}"
            )

            modelNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = currentScale,
                centerOrigin = Position(y = -0.5f)
            ).apply {
                position = Position(
                    x = 0f,
                    y = 0f,
                    z = -0.2f
                )

                isShadowCaster = true
                isShadowReceiver = true
            }

            sceneView.addChildNode(modelNode!!)

            enableControls(true)

            Log.d("XR", "MODEL LOADED SUCCESSFULLY")

            Toast.makeText(
                requireContext(),
                "✅ Model loaded!",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Log.e("XR", "ERROR LOADING MODEL", e)

            Toast.makeText(
                requireContext(),
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun recreateModel(newScale: Float) {

        modelNode?.let { currentNode ->

            val currentPosition = currentNode.position
            val currentRotation = currentNode.rotation

            sceneView.removeChildNode(currentNode)

            try {

                val modelInstance =
                    sceneView.modelLoader.createModelInstance(
                        assetFileLocation = "models/pikachu.glb"
                    )

                modelNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = newScale,
                    centerOrigin = Position(y = -0.5f)
                ).apply {

                    position = currentPosition
                    rotation = currentRotation

                    isShadowCaster = true
                    isShadowReceiver = true
                }

                sceneView.addChildNode(modelNode!!)

            } catch (e: Exception) {

                Log.e("XR", "Error recreating model", e)

                Toast.makeText(
                    requireContext(),
                    "Error scaling model",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun enableControls(enabled: Boolean) {
        btnRotate.isEnabled = enabled
        btnScaleUp.isEnabled = enabled
        btnScaleDown.isEnabled = enabled
        btnReset.isEnabled = enabled
        btnRemove.isEnabled = enabled
    }
}