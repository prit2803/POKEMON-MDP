package com.example.proyek_mdp.UI.Camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView

import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope

import com.bumptech.glide.Glide

import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Database.PokemonDatabase
import com.example.proyek_mdp.UI.Database.PokemonEntity
import com.example.proyek_mdp.UI.Network.RetrofitClient

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import kotlinx.coroutines.launch

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment :
    Fragment(R.layout.fragment_camera) {

    // ===============================
    // VIEW
    // ===============================

    private lateinit var previewView:
            PreviewView

    private lateinit var tvResult:
            TextView

    private lateinit var imgPokemon:
            ImageView

    private lateinit var btnSave:
            Button

    private lateinit var btnClear:
            Button

    // ===============================
    // CAMERA
    // ===============================

    private lateinit var cameraExecutor:
            ExecutorService

    // ===============================
    // CURRENT DATA
    // ===============================

    private var currentPokemonName =
        ""

    private var currentPokemonHp =
        0

    private var currentPokemonImage =
        ""

    // Prevent duplicate OCR spam
    private var isScanning =
        false

    // ===============================
    // CAMERA PERMISSION
    // ===============================

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) { granted ->

            if (granted) {

                startCamera()

            } else {

                tvResult.text =
                    "Permission kamera ditolak"
            }
        }

    // ===============================
    // LIFECYCLE
    // ===============================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        // INIT VIEW

        previewView =
            view.findViewById(
                R.id.previewView
            )

        tvResult =
            view.findViewById(
                R.id.tvResult
            )

        imgPokemon =
            view.findViewById(
                R.id.imgPokemon
            )

        btnSave =
            view.findViewById(
                R.id.btnSave
            )

        btnClear =
            view.findViewById(
                R.id.btnClear
            )

        // CAMERA EXECUTOR

        cameraExecutor =
            Executors.newSingleThreadExecutor()

        // CHECK CAMERA

        checkCameraPermission()

        // SAVE BUTTON

        btnSave.setOnClickListener {

            savePokemon()
        }

        // CLEAR BUTTON

        btnClear.setOnClickListener {

            clearPokemon()
        }
    }

    // ===============================
    // CAMERA PERMISSION
    // ===============================

    private fun checkCameraPermission() {

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    // ===============================
    // START CAMERA
    // ===============================

    private fun startCamera() {

        val cameraProviderFuture =
            ProcessCameraProvider
                .getInstance(requireContext())

        cameraProviderFuture.addListener({

            val cameraProvider =
                cameraProviderFuture.get()

            // PREVIEW

            val preview =
                Preview.Builder()
                    .build()
                    .also {

                        it.surfaceProvider =
                            previewView.surfaceProvider
                    }

            // REALTIME ANALYZER

            val imageAnalyzer =
                ImageAnalysis.Builder()

                    .setBackpressureStrategy(
                        ImageAnalysis
                            .STRATEGY_KEEP_ONLY_LATEST
                    )

                    .build()

            imageAnalyzer.setAnalyzer(
                cameraExecutor
            ) { imageProxy ->

                processImageProxy(imageProxy)
            }

            val cameraSelector =
                CameraSelector
                    .DEFAULT_BACK_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

            } catch (e: Exception) {

                Log.e(
                    "CameraFragment",
                    "Camera gagal",
                    e
                )
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // ===============================
    // REALTIME OCR
    // ===============================

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        imageProxy: ImageProxy
    ) {

        if (isScanning) {

            imageProxy.close()
            return
        }

        val mediaImage =
            imageProxy.image

        if (mediaImage != null) {

            val image =
                InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy
                        .imageInfo
                        .rotationDegrees
                )

            val recognizer =
                TextRecognition.getClient(
                    TextRecognizerOptions
                        .DEFAULT_OPTIONS
                )

            recognizer.process(image)

                .addOnSuccessListener {

                        visionText ->

                    val text =
                        visionText.text

                    Log.d(
                        "OCR_RAW_TEXT",
                        text
                    )

                    if (text.isNotEmpty()) {

                        val pokemonName =
                            extractPokemonName(text)

                        if (
                            pokemonName.isNotEmpty()
                        ) {

                            isScanning = true

                            searchPokemon(text)
                        }
                    }
                }

                .addOnFailureListener {

                    tvResult.text =
                        "OCR gagal"

                    isScanning = false
                }

                .addOnCompleteListener {

                    imageProxy.close()
                }
        }
    }

    // ===============================
    // EXTRACT POKEMON NAME
    // ===============================

    private fun extractPokemonName(
        text: String
    ): String {

        val lines =
            text.lines()

        for (line in lines) {

            val cleaned =
                line
                    .trim()
                    .split(" ")
                    .firstOrNull()
                    ?.lowercase()
                    ?.replace(
                        "[^a-z]".toRegex(),
                        ""
                    )

            if (
                !cleaned.isNullOrEmpty()
            ) {

                return cleaned
            }
        }

        return ""
    }

    // ===============================
    // EXTRACT HP
    // ===============================

    private fun extractPokemonHp(
        text: String
    ): Int {

        val regex =
            Regex("\\b\\d{2,3}\\b")

        val matches =
            regex.findAll(text)

        for (match in matches) {

            val value =
                match.value.toIntOrNull()

            if (
                value != null &&
                value in 40..350
            ) {

                return value
            }
        }

        return 0
    }

    // ===============================
    // SEARCH POKEMON
    // ===============================

    private fun searchPokemon(
        text: String
    ) {

        val pokemonName =
            extractPokemonName(text)

        currentPokemonHp =
            extractPokemonHp(text)

        if (
            pokemonName.isEmpty()
        ) {

            tvResult.text =
                "Pokemon tidak ditemukan"

            isScanning = false

            return
        }

        lifecycleScope.launch {

            try {

                val response =
                    RetrofitClient.api
                        .getPokemon(
                            pokemonName
                        )

                if (response.isSuccessful) {

                    val pokemon =
                        response.body()

                    currentPokemonName =
                        pokemon?.name ?: ""

                    currentPokemonImage =
                        pokemon?.sprites
                            ?.front_default
                            ?: ""

                    val pokemonType =
                        pokemon?.types
                            ?.firstOrNull()
                            ?.type
                            ?.name
                            ?: "-"

                    val pokemonHeight =
                        pokemon?.height ?: 0

                    val pokemonWeight =
                        pokemon?.weight ?: 0

                    tvResult.text =
                        """
                        Pokemon:
                        ${pokemon?.name}

                        Type:
                        $pokemonType

                        HP:
                        $currentPokemonHp

                        Height:
                        $pokemonHeight

                        Weight:
                        $pokemonWeight
                        """.trimIndent()

                    Glide.with(requireContext())
                        .load(
                            pokemon?.sprites
                                ?.front_default
                        )
                        .into(imgPokemon)

                } else {

                    tvResult.text =
                        "Pokemon tidak ditemukan"

                    isScanning = false
                }

            } catch (e: Exception) {

                tvResult.text =
                    "Error: ${e.message}"

                isScanning = false
            }
        }
    }

    // ===============================
    // SAVE DATABASE
    // ===============================

    private fun savePokemon() {

        if (
            currentPokemonName.isEmpty()
        ) {

            tvResult.text =
                "Belum ada pokemon"

            return
        }

        lifecycleScope.launch {

            val database =
                PokemonDatabase
                    .getDatabase(
                        requireContext()
                    )

            val pokemon =
                PokemonEntity(

                    name = currentPokemonName,

                    hp = currentPokemonHp,

                    imageUrl = currentPokemonImage
                )

            database
                .pokemonDao()
                .insertPokemon(pokemon)

            tvResult.text =
                "$currentPokemonName berhasil disimpan!"
        }
    }

    // ===============================
    // CLEAR POKEMON
    // ===============================

    private fun clearPokemon() {

        currentPokemonName = ""

        currentPokemonHp = 0

        currentPokemonImage = ""

        tvResult.text =
            "Arahkan kartu Pokémon"

        imgPokemon.setImageDrawable(null)

        isScanning = false
    }

    // ===============================
    // DESTROY
    // ===============================

    override fun onDestroyView() {

        super.onDestroyView()

        cameraExecutor.shutdown()
    }
}