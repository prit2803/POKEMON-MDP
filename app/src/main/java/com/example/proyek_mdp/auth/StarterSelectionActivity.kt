package com.example.proyek_mdp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.proyek_mdp.MainActivity
import com.example.proyek_mdp.R
import com.example.proyek_mdp.Data.local.database.PokemonDatabase
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.Data.local.database.AppDatabase
import kotlinx.coroutines.launch

/**
 * Ditampilkan cuma sekali per akun, tepat setelah login pertama kali
 * (dicek lewat User.hasSelectedStarter di LoginActivity).
 */
class StarterSelectionActivity : AppCompatActivity() {

    private data class Starter(val speciesId: Int, val name: String, val hp: Int, val imageUrl: String)

    private val starters = listOf(
        Starter(1, "Bulbasaur", 45, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"),
        Starter(4, "Charmander", 39, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png"),
        Starter(7, "Squirtle", 44, "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starter_selection)

        val imgBulbasaur = findViewById<ImageView>(R.id.imgBulbasaur)
        val imgCharmander = findViewById<ImageView>(R.id.imgCharmander)
        val imgSquirtle = findViewById<ImageView>(R.id.imgSquirtle)

        Glide.with(this).load(starters[0].imageUrl).into(imgBulbasaur)
        Glide.with(this).load(starters[1].imageUrl).into(imgCharmander)
        Glide.with(this).load(starters[2].imageUrl).into(imgSquirtle)

        findViewById<Button>(R.id.btnPickBulbasaur).setOnClickListener { pickStarter(starters[0]) }
        findViewById<Button>(R.id.btnPickCharmander).setOnClickListener { pickStarter(starters[1]) }
        findViewById<Button>(R.id.btnPickSquirtle).setOnClickListener { pickStarter(starters[2]) }
    }

    private fun pickStarter(starter: Starter) {
        val sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        lifecycleScope.launch {
            val pokemonDb = PokemonDatabase.getDatabase(this@StarterSelectionActivity)
            pokemonDb.pokemonDao().insertPokemon(
                PokemonEntity(
                    userId = userId,
                    speciesId = starter.speciesId,
                    name = starter.name,
                    hp = starter.hp,
                    imageUrl = starter.imageUrl,
                    level = 1,
                    exp = 0,
                    isStarter = 1,
                    isLocked = 1 // starter otomatis terkunci, gak bisa kehapus gak sengaja
                )
            )

            val appDb = AppDatabase.getDatabase(this@StarterSelectionActivity)
            val user = appDb.userDao().getUserById(userId)
            if (user != null) {
                user.hasSelectedStarter = 1
                appDb.userDao().update(user)
            }

            runOnUiThread {
                startActivity(Intent(this@StarterSelectionActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}