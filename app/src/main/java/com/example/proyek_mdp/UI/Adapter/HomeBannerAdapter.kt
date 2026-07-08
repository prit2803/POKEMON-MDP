package com.example.proyek_mdp.UI.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyek_mdp.R

/**
 * Satu slide di banner carousel Home.
 * - postId != null  -> banner promo dari post admin, klik buka Shop, ribbon "PROMO"
 * - postId == null  -> banner statis/dekoratif (mis. artwork Pokemon), ribbon "FEATURED", gak nyantol ke Shop
 */
data class BannerDisplayItem(
    val imageUrl: String, // bisa path file lokal (post) ATAU URL http (statis) -> Glide handle dua-duanya
    val title: String,
    val subtitle: String,
    val postId: Int? = null
)

class HomeBannerAdapter(
    private var banners: List<BannerDisplayItem> = emptyList(),
    private val onItemClick: (BannerDisplayItem) -> Unit
) : RecyclerView.Adapter<HomeBannerAdapter.BannerViewHolder>() {

    class BannerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivBannerImage)
        val tvRibbon: TextView = view.findViewById(R.id.tvBannerRibbon)
        val tvTitle: TextView = view.findViewById(R.id.tvBannerTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvBannerPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val banner = banners[position]

        holder.tvTitle.text = banner.title
        holder.tvSubtitle.text = banner.subtitle

        if (banner.postId != null) {
            holder.tvRibbon.text = "PROMO"
        } else {
            holder.tvRibbon.text = "FEATURED"
        }

        // Glide otomatis ngerti: string "http(s)://..." di-load sebagai URL,
        // string path biasa (misal /data/user/0/.../post_123.jpg) di-load sebagai file lokal.
        Glide.with(holder.itemView.context)
            .load(banner.imageUrl)
            .centerCrop()
            .into(holder.ivImage)

        holder.itemView.setOnClickListener { onItemClick(banner) }
    }

    override fun getItemCount(): Int = banners.size

    fun updateData(newBanners: List<BannerDisplayItem>) {
        banners = newBanners
        notifyDataSetChanged()
    }
}