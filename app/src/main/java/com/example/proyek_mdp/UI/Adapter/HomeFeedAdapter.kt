package com.example.proyek_mdp.UI.Adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.Post
import java.text.NumberFormat
import java.util.Locale

/** List post vertikal di Home (gabungan Feed + filter kategori), gaya kartu elegan. */
class HomeFeedAdapter(
    private val onItemClick: (Post) -> Unit
) : ListAdapter<Post, HomeFeedAdapter.FeedViewHolder>(DiffCallback()) {

    class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivFeedImage)
        val tvCategory: TextView = view.findViewById(R.id.tvFeedCategory)
        val tvTitle: TextView = view.findViewById(R.id.tvFeedTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvFeedDescription)
        val tvPrice: TextView = view.findViewById(R.id.tvFeedPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val post = getItem(position)
        val rupiah = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

        holder.tvCategory.text = post.category.uppercase()
        holder.tvTitle.text = post.title
        holder.tvDescription.text = post.description
        holder.tvPrice.text = rupiah.format(post.price)

        if (!post.imagePath.isNullOrEmpty()) {
            holder.ivImage.setImageBitmap(BitmapFactory.decodeFile(post.imagePath))
        } else {
            holder.ivImage.setImageDrawable(null)
        }

        holder.itemView.setOnClickListener { onItemClick(post) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
    }
}