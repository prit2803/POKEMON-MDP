package com.example.proyek_mdp.admin

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyek_mdp.R
import com.example.proyek_mdp.Data.local.entity.User
import com.example.proyek_mdp.viewmodel.UserManagementViewModel
import com.example.proyek_mdp.viewmodel.ViewModelFactory

class UserManagementFragment : Fragment() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: UserAdapter

    private val viewModel: UserManagementViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_management, container, false)
        rvUsers = view.findViewById(R.id.rvUsers)

        setupRecyclerView()

        viewModel.usersList.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
        }

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
        viewModel.loadUsers()
    }

    private fun toggleBan(user: User) {
        viewModel.toggleBan(user)
    }

    private fun deleteUser(user: User) {
        viewModel.deleteUser(user)
    }

    private fun showEditDialog(user: User) {
        val et = EditText(requireContext())
        et.setText(user.username)
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Username")
            .setView(et)
            .setPositiveButton("Simpan") { _, _ ->
                viewModel.updateUsername(user, et.text.toString())
            }.show()
    }
}