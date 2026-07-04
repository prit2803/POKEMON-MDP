package com.example.proyek_mdp.UI.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Database.PokemonEntity

class PokemonAdapter(
    private var pokemonList: List<PokemonEntity>,
    private val onDeleteClick: (PokemonEntity) -> Unit = {},
    private val onItemClick: (PokemonEntity) -> Unit = {},      // tap -> buka opsi (mis. Beri Makan)
    private val onItemLongClick: (PokemonEntity) -> Unit = {}   // tap-tahan -> toggle lock/unlock
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPokemon: ImageView = itemView.findViewById(R.id.imgPokemon)
        val ivLock: ImageView = itemView.findViewById(R.id.ivLock)
        val tvPokemonName: TextView = itemView.findViewById(R.id.tvPokemonName)
        val tvPokemonLevel: TextView = itemView.findViewById(R.id.tvPokemonLevel)
        val tvPokemonHp: TextView = itemView.findViewById(R.id.tvPokemonHp)
        val btnDelete: Button = itemView.findViewById(R.id.btnDeletePokemon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.tvPokemonName.text = pokemon.name
        holder.tvPokemonLevel.text = "Level ${pokemon.level}"
        holder.tvPokemonHp.text = "HP: ${pokemon.hp}"

        Glide.with(holder.itemView.context)
            .load(pokemon.imageUrl)
            .into(holder.imgPokemon)

        val locked = pokemon.isLocked == 1
        holder.ivLock.visibility = if (locked) View.VISIBLE else View.GONE
        // Pokemon terkunci gak bisa dihapus sama sekali, tombolnya disembunyikan
        holder.btnDelete.visibility = if (locked) View.GONE else View.VISIBLE

        holder.btnDelete.setOnClickListener { onDeleteClick(pokemon) }
        holder.itemView.setOnClickListener { onItemClick(pokemon) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(pokemon)
            true
        }
    }

    override fun getItemCount(): Int = pokemonList.size

    fun updateData(newList: List<PokemonEntity>) {
        this.pokemonList = newList
        notifyDataSetChanged()
    }
}