package com.example.proyek_mdp.admin

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.database.AppDatabase
import com.example.proyek_mdp.database.User
import kotlinx.coroutines.launch

class UserManagementFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var db: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_management, container, false)
        rvUsers = view.findViewById(R.id.rvUsers)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        loadUsers()
        return view
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(emptyList(),
            onEdit = { showEditDialog(it) },
            onBan = { toggleBan(it) },
            onDelete = { deleteUser(it) }
        )
        rvUsers.layoutManager = LinearLayoutManager(requireContext())
        rvUsers.adapter = adapter
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            val list = db.userDao().getAllUsers()
            adapter.updateData(list)
        }
    }

    private fun toggleBan(user: User) {
        lifecycleScope.launch {
            val newStatus = if (user.isBanned == 1) 0 else 1
            db.userDao().updateBannedStatus(user.id, newStatus)
            loadUsers()
        }
    }

    private fun deleteUser(user: User) {
        lifecycleScope.launch {
            db.userDao().delete(user)
            loadUsers()
        }
    }

    private fun showEditDialog(user: User) {
        val et = EditText(requireContext())
        et.setText(user.username)
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Username")
            .setView(et)
            .setPositiveButton("Simpan") { _, _ ->
                user.username = et.text.toString()
                lifecycleScope.launch {
                    db.userDao().update(user)
                    loadUsers()
                }
            }.show()
    }
}