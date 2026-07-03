package com.example.proyek_mdp.UI.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_table")
data class PokemonEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: Int = 0, // pemilik pokemon ini (0 = data lama/belum ada pemilik)

    val name: String,

    val hp: Int,

    val imageUrl: String,

    var level: Int = 1,

    var exp: Int = 0,

    val isStarter: Int = 0, // 1 = ini pokemon starter yang dipilih pas awal

    var isLocked: Int = 0 // 1 = terkunci, gak bisa dihapus (satuan maupun Hapus Semua)
)