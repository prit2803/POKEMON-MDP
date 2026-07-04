package com.example.proyek_mdp.UI.Adapter

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyek_mdp.R
import com.example.proyek_mdp.UI.Database.PokedexSpecies
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PokedexItem(
    val species: PokedexSpecies,
    val isCaught: Boolean,
    val highestLevel: Int,
    val firstCaughtAt: Long
)

class PokedexAdapter(
    private var items: List<PokedexItem> = emptyList()
) : RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder>() {

    class PokedexViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivPokedexImage)
        val tvNumber: TextView = view.findViewById(R.id.tvPokedexNumber)
        val tvName: TextView = view.findViewById(R.id.tvPokedexName)
        val tvType1: TextView = view.findViewById(R.id.tvPokedexType1)
        val tvType2: TextView = view.findViewById(R.id.tvPokedexType2)
        val tvInfo: TextView = view.findViewById(R.id.tvPokedexInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokedexViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokedex, parent, false)
        return PokedexViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokedexViewHolder, position: Int) {
        val item = items[position]
        val species = item.species

        holder.tvNumber.text = "#${species.speciesId.toString().padStart(3, '0')}"
        holder.tvName.text = species.name.replaceFirstChar { it.uppercase() }

        Glide.with(holder.itemView.context)
            .load(species.imageUrl)
            .into(holder.ivImage)

        if (item.isCaught) {
            // Sudah ditangkap: gambar normal, tipe & info kelihatan
            holder.ivImage.clearColorFilter()
            holder.ivImage.alpha = 1f

            holder.tvType1.visibility = View.VISIBLE
            holder.tvType1.text = species.type1.uppercase()
            holder.tvType1.setBackgroundColor(typeColor(species.type1))

            if (!species.type2.isNullOrEmpty()) {
                holder.tvType2.visibility = View.VISIBLE
                holder.tvType2.text = species.type2.uppercase()
                holder.tvType2.setBackgroundColor(typeColor(species.type2))
            } else {
                holder.tvType2.visibility = View.GONE
            }

            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(item.firstCaughtAt))
            holder.tvInfo.text = "Lv. ${item.highestLevel} • $dateStr"
        } else {
            // Belum ditangkap: gambar jadi siluet hitam, tipe & info disembunyikan
            holder.ivImage.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
            holder.ivImage.alpha = 0.55f

            holder.tvType1.visibility = View.GONE
            holder.tvType2.visibility = View.GONE

            holder.tvInfo.text = "Belum Ditangkap"
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<PokedexItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun typeColor(type: String): Int {
        return when (type.lowercase()) {
            "grass" -> Color.parseColor("#4CAF50")
            "poison" -> Color.parseColor("#9C27B0")
            "fire" -> Color.parseColor("#FF5722")
            "water" -> Color.parseColor("#2196F3")
            "electric" -> Color.parseColor("#FFC107")
            "bug" -> Color.parseColor("#8BC34A")
            "normal" -> Color.parseColor("#9E9E9E")
            "flying" -> Color.parseColor("#90CAF9")
            "ground" -> Color.parseColor("#A1887F")
            "fairy" -> Color.parseColor("#F48FB1")
            "fighting" -> Color.parseColor("#D32F2F")
            "psychic" -> Color.parseColor("#EC407A")
            "rock" -> Color.parseColor("#795548")
            "ghost" -> Color.parseColor("#673AB7")
            "ice" -> Color.parseColor("#4DD0E1")
            "dragon" -> Color.parseColor("#3F51B5")
            "dark" -> Color.parseColor("#424242")
            "steel" -> Color.parseColor("#78909C")
            else -> Color.parseColor("#757575")
        }
    }
}