package com.example.proyek_mdp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TrainerStatsDao {

    // Membuat statistik baru saat user register
    @Insert
    suspend fun insert(stats: TrainerStats)

    // Mengupdate seluruh data statistik
    @Update
    suspend fun update(stats: TrainerStats)

    // Mengambil statistik berdasarkan user
    @Query("""
        SELECT * FROM trainer_stats
        WHERE userId = :userId
        LIMIT 1
    """)
    suspend fun getStats(userId: Int): TrainerStats?

    // Menambah jumlah pokemon yang berhasil didapat
    @Query("""
        UPDATE trainer_stats
        SET pokemonCaught = pokemonCaught + 1
        WHERE userId = :userId
    """)
    suspend fun addPokemonCaught(userId: Int)

    // Menambah jumlah battle yang dimenangkan
    @Query("""
        UPDATE trainer_stats
        SET battleWin = battleWin + 1
        WHERE userId = :userId
    """)
    suspend fun addBattleWin(userId: Int)

    // Menambah jarak tempuh
    @Query("""
        UPDATE trainer_stats
        SET distance = distance + :distance
        WHERE userId = :userId
    """)
    suspend fun addDistance(
        userId: Int,
        distance: Double
    )

    // Reset statistik (opsional)
    @Query("""
        UPDATE trainer_stats
        SET pokemonCaught = 0,
            battleWin = 0,
            distance = 0
        WHERE userId = :userId
    """)
    suspend fun resetStats(userId: Int)
}