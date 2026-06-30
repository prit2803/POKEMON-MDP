package com.example.proyek_mdp.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.User

class UserAdapter(
    private var users: List<User>,
    private val onEdit: (User) -> Unit,
    private val onBan: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnBan: Button = view.findViewById(R.id.btnBan)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.tvUsername.text = user.username
        holder.tvEmail.text = user.email

        // Visual feedback jika banned
        if (user.isBanned == 1) {
            holder.tvUsername.setTextColor(Color.RED)
            holder.btnBan.text = "Unban"
        } else {
            holder.tvUsername.setTextColor(Color.BLACK)
            holder.btnBan.text = "Ban"
        }

        holder.btnEdit.setOnClickListener { onEdit(user) }
        holder.btnBan.setOnClickListener { onBan(user) }
        holder.btnDelete.setOnClickListener { onDelete(user) }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        this.users = newUsers
        notifyDataSetChanged()
    }
}