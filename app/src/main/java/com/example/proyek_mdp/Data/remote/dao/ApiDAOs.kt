package com.example.proyek_mdp.Data.remote.dao

import com.example.proyek_mdp.Data.local.dao.*
import com.example.proyek_mdp.Data.local.entity.*
import com.example.proyek_mdp.Data.remote.api.BackendRetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApiUserDao : UserDao {
    private val api = BackendRetrofitClient.api

    override suspend fun insert(user: User) {
        api.insertUser(user)
    }

    override suspend fun update(user: User) {
        api.updateUser(user)
    }

    override suspend fun delete(user: User) {
        api.deleteUser(user.id)
    }

    override suspend fun login(username: String, password: String): User? {
        return try {
            api.login(mapOf("username" to username, "password" to password))
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun isUsernameExists(username: String): Int {
        return try {
            api.isUsernameExists(username)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            api.getAllUsers()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTotalUsers(): Int {
        return try {
            api.getTotalUsers()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getBannedUsersCount(): Int {
        return try {
            api.getBannedUsersCount()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getUserByUsername(username: String): User? {
        return try {
            api.getUserByUsername(username)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserById(userId: Int): User? {
        return try {
            api.getUserById(userId)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateBannedStatus(userId: Int, status: Int) {
        api.updateBannedStatus(userId, mapOf("status" to status))
    }

    override suspend fun updateStats(userId: Int, pokemon: Int, level: Int, battle: Int, distance: Double) {
        api.updateStats(
            userId,
            mapOf(
                "pokemonCaught" to pokemon,
                "trainerLevel" to level,
                "battleWon" to battle,
                "distance" to distance
            )
        )
    }

    override suspend fun getPokemonCaught(userId: Int): Int {
        return try {
            api.getPokemonCaught(userId)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getTrainerLevel(userId: Int): Int {
        return try {
            api.getTrainerLevel(userId)
        } catch (e: Exception) {
            1
        }
    }

    override suspend fun getBattleWon(userId: Int): Int {
        return try {
            api.getBattleWon(userId)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getDistance(userId: Int): Double {
        return try {
            api.getDistance(userId)
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun updateCoin(id: Int, coin: Int) {
        api.updateCoin(id, mapOf("coins" to coin))
    }

    override suspend fun getUnsyncedUsers(): List<User> {
        return emptyList()
    }
}

class ApiPostDao : PostDao {
    private val api = BackendRetrofitClient.api

    override suspend fun insertPost(post: Post): Long {
        return try {
            api.insertPost(post)
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun updatePost(post: Post) {
        api.updatePost(post)
    }

    override suspend fun deletePost(post: Post) {
        api.deletePost(post)
    }

    override fun getAllPosts(): Flow<List<Post>> = flow {
        try {
            emit(api.getAllPosts())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override fun getActivePosts(): Flow<List<Post>> = flow {
        try {
            emit(api.getActivePosts())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getTotalPosts(): Int {
        return try {
            api.getTotalPosts()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getPostById(postId: Int): Post? {
        return try {
            api.getPostById(postId)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun decreaseStock(postId: Int): Int {
        return try {
            api.decreaseStock(postId)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getUnsyncedPosts(): List<Post> {
        return emptyList()
    }
}

class ApiUserInventoryDao : UserInventoryDao {
    private val api = BackendRetrofitClient.api

    override suspend fun getUserInventory(userId: Int): List<UserInventory> {
        return try {
            api.getUserInventory(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getItem(userId: Int, postId: Int): UserInventory? {
        return try {
            api.getItem(userId, postId)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insert(item: UserInventory) {
        api.insertInventory(item)
    }

    override suspend fun update(item: UserInventory) {
        api.updateInventory(item)
    }
}

class ApiPaymentHistoryDao : PaymentHistoryDao {
    private val api = BackendRetrofitClient.api

    override suspend fun insert(history: PaymentHistory) {
        api.insertPayment(history)
    }

    override suspend fun getHistory(userId: Int): List<PaymentHistory> {
        return try {
            api.getPaymentHistory(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class ApiPurchaseHistoryDao : PurchaseHistoryDao {
    private val api = BackendRetrofitClient.api

    override suspend fun insert(history: PurchaseHistory) {
        api.insertPurchase(history)
    }

    override fun getHistory(userId: Int): Flow<List<PurchaseHistory>> = flow {
        try {
            emit(api.getPurchaseHistory(userId))
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}

class ApiPokemonDao : PokemonDao {
    private val api = BackendRetrofitClient.api

    override suspend fun insertPokemon(pokemon: PokemonEntity): Long {
        return try {
            api.insertPokemon(pokemon)
        } catch (e: Exception) {
            0L
        }
    }

    override suspend fun updatePokemon(pokemon: PokemonEntity) {
        api.updatePokemon(pokemon)
    }

    override suspend fun getAllPokemon(): List<PokemonEntity> {
        return try {
            api.getAllPokemon()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getPokemonByUser(userId: Int): List<PokemonEntity> {
        return try {
            api.getPokemonByUser(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getStarter(userId: Int): PokemonEntity? {
        return try {
            api.getStarter(userId)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deletePokemon(pokemon: PokemonEntity) {
        api.deletePokemon(pokemon)
    }

    override suspend fun deleteAllUnlockedByUser(userId: Int) {
        api.deleteAllUnlockedByUser(userId)
    }

    override suspend fun getOwnedSpeciesSummary(userId: Int): List<OwnedSpeciesSummary> {
        return try {
            api.getOwnedSpeciesSummary(userId)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUnsyncedPokemon(): List<PokemonEntity> {
        return emptyList()
    }
}

class ApiPokedexSpeciesDao : PokedexSpeciesDao {
    private val api = BackendRetrofitClient.api

    override suspend fun insertAll(species: List<PokedexSpecies>) {
        api.insertAllSpecies(species)
    }

    override suspend fun getRange(start: Int, end: Int): List<PokedexSpecies> {
        return try {
            api.getSpeciesRange(start, end)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
