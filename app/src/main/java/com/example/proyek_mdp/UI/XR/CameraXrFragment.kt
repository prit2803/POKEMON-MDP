package com.example.proyek_mdp.UI.XR

import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

import androidx.fragment.app.Fragment

import coil.load

import com.example.proyek_mdp.R

import io.github.sceneview.ar.ARSceneView

import kotlinx.coroutines.*

class CameraXRFragment :
    Fragment(R.layout.fragment_camera_xr) {

    // =====================================
    // VIEW
    // =====================================

    private lateinit var sceneView:
            ARSceneView

    private var pokemonView:
            ImageView? = null

    private var floatingJob:
            Job? = null

    // =====================================
    // LIFECYCLE
    // =====================================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        // INIT SCENE VIEW

        sceneView =
            view.findViewById(
                R.id.sceneView
            )

        // SHOW AR PLANE

        sceneView.planeRenderer
            .isEnabled = true

        // TAP SCREEN

        sceneView.setOnTouchListener {

                _, event ->

            if (
                event.action ==
                MotionEvent.ACTION_UP
            ) {

                spawnPokemon(
                    event.x,
                    event.y
                )
            }

            true
        }
    }

    // =====================================
    // SPAWN POKEMON
    // =====================================

    private fun spawnPokemon(
        x: Float,
        y: Float
    ) {

        // REMOVE OLD

        pokemonView?.let {

            (view as FrameLayout)
                .removeView(it)
        }

        // CREATE IMAGE

        pokemonView =
            ImageView(requireContext()).apply {

                layoutParams =

                    FrameLayout.LayoutParams(
                        350,
                        350
                    )

                // POSITION

                this.x =
                    x - 175f

                this.y =
                    y - 175f

                // LOAD SPRITE

                load(

                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                )

                elevation = 20f
            }

        // ADD TO ROOT

        (view as FrameLayout)
            .addView(pokemonView)

        // FLOATING

        startFloatingAnimation()
    }

    // =====================================
    // FLOATING EFFECT
    // =====================================

    private fun startFloatingAnimation() {

        floatingJob?.cancel()

        floatingJob =

            CoroutineScope(
                Dispatchers.Main
            ).launch {

                var direction = 1

                while (isActive) {

                    delay(16)

                    pokemonView?.let {

                        it.translationY +=
                            direction * 1f

                        if (
                            it.translationY > 30
                        ) {

                            direction = -1
                        }

                        if (
                            it.translationY < -30
                        ) {

                            direction = 1
                        }
                    }
                }
            }
    }

    // =====================================
    // DESTROY
    // =====================================

    override fun onDestroyView() {

        super.onDestroyView()

        floatingJob?.cancel()
    }
}