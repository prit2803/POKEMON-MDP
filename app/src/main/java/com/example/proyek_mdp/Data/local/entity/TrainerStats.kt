package com.example.proyek_mdp.Data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trainer_stats")
data class TrainerStats(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Menghubungkan statistik dengan user
    val userId: Int,

    // Statistik Trainer
    var pokemonCaught: Int = 0,

    var battleWin: Int = 0,

    var distance: Double = 0.0
)