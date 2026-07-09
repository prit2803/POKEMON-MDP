package com.example.proyek_mdp.UI.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyek_mdp.R
import com.example.proyek_mdp.Data.local.entity.PokemonEntity

class PokemonSelectionAdapter(
    private var pokemonList: List<PokemonEntity>,
    private val onPokemonSelected: (PokemonEntity) -> Unit
) : RecyclerView.Adapter<PokemonSelectionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPokemon: ImageView = view.findViewById(R.id.ivPokemonImage)
        val tvName: TextView = view.findViewById(R.id.tvPokemonName)
        val tvLevel: TextView = view.findViewById(R.id.tvPokemonLevel)
        val btnSpawn: TextView = view.findViewById(R.id.btnSpawnThis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pokemon_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        holder.tvName.text = pokemon.name
        holder.tvLevel.text = "Lv. ${pokemon.level} | HP: ${pokemon.hp}"

        // Load image with Glide
        Glide.with(holder.itemView.context)
            .load(pokemon.imageUrl)
            .placeholder(R.drawable.ic_pokemon_placeholder)
            .error(R.drawable.ic_pokemon_placeholder)
            .into(holder.ivPokemon)

        holder.btnSpawn.setOnClickListener {
            onPokemonSelected(pokemon)
        }

        holder.itemView.setOnClickListener {
            onPokemonSelected(pokemon)
        }
    }

    override fun getItemCount() = pokemonList.size

    fun updateData(newList: List<PokemonEntity>) {
        pokemonList = newList
        notifyDataSetChanged()
    }
}