package com.example.proyek_mdp.Data.remote.firebase

import com.example.proyek_mdp.Data.local.entity.OwnedSpeciesSummary
import com.example.proyek_mdp.Data.local.entity.PokemonEntity
import com.example.proyek_mdp.Data.local.entity.Post
import com.example.proyek_mdp.Data.local.entity.User
import com.example.proyek_mdp.Data.local.entity.UserInventory
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

/**
 * Pengganti [com.example.proyek_mdp.Data.remote.api.BackendApiService] (Express + MySQL).
 * Semua data cloud disimpan di Cloud Firestore.
 *
 * Strategi ID dokumen:
 *  - users      -> documentId = username
 *  - posts      -> documentId = id
 *  - pokemon    -> documentId = id
 *  - inventory  -> documentId = "{userId}_{postId}"
 *
 * Query yang butuh composite index dihindari: filter multi-field dilakukan di sisi Kotlin.
 */
object FirestoreService {

    private const val USERS = "users"
    private const val POSTS = "posts"
    private const val POKEMON = "pokemon"
    private const val INVENTORY = "user_inventory"

    private fun db(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // =========================================================
    // HELPERS
    // =========================================================

    private fun DocumentSnapshot.intOf(field: String, default: Int = 0): Int =
        (get(field) as? Number)?.toInt() ?: default

    private fun DocumentSnapshot.longOf(field: String, default: Long = 0L): Long =
        (get(field) as? Number)?.toLong() ?: default

    private fun DocumentSnapshot.doubleOf(field: String, default: Double = 0.0): Double =
        (get(field) as? Number)?.toDouble() ?: default

    // =========================================================
    // USER
    // =========================================================

    private fun userToMap(u: User): Map<String, Any?> = mapOf(
        "id" to u.id,
        "username" to u.username,
        "email" to u.email,
        "password" to u.password,
        "role" to u.role,
        "isBanned" to u.isBanned,
        "coins" to u.coins,
        "lastClaimDate" to u.lastClaimDate,
        "streakCount" to u.streakCount,
        "hasSelectedStarter" to u.hasSelectedStarter,
        "pokemonCaught" to u.pokemonCaught,
        "battleWon" to u.battleWon,
        "trainerLevel" to u.trainerLevel,
        "distance" to u.distance,
        "nickname" to u.nickname,
        "team" to u.team
    )

    private fun mapToUser(d: DocumentSnapshot): User = User(
        id = d.intOf("id", 0),
        username = d.getString("username") ?: "",
        email = d.getString("email") ?: "",
        password = d.getString("password") ?: "",
        role = d.getString("role") ?: "user",
        isBanned = d.intOf("isBanned", 0),
        coins = d.intOf("coins", 0),
        lastClaimDate = d.getString("lastClaimDate"),
        streakCount = d.intOf("streakCount", 0),
        hasSelectedStarter = d.intOf("hasSelectedStarter", 0),
        pokemonCaught = d.intOf("pokemonCaught", 0),
        battleWon = d.intOf("battleWon", 0),
        trainerLevel = d.intOf("trainerLevel", 1),
        distance = d.doubleOf("distance", 0.0),
        nickname = d.getString("nickname"),
        team = d.getString("team"),
        isSynced = 1
    )

    suspend fun insertUser(user: User): Map<String, Int> {
        db().collection(USERS).document(user.username).set(userToMap(user)).await()
        return mapOf("id" to user.id)
    }

    suspend fun updateUser(user: User): Map<String, Boolean> {
        db().collection(USERS).document(user.username).set(userToMap(user)).await()
        return mapOf("success" to true)
    }

    suspend fun deleteUser(id: Int): Map<String, Boolean> {
        val docs = db().collection(USERS).whereEqualTo("id", id.toLong()).get().await().documents
        docs.forEach { it.reference.delete().await() }
        return mapOf("success" to true)
    }

    suspend fun login(body: Map<String, String>): User? {
        val username = body["username"] ?: return null
        val password = body["password"] ?: return null
        val snap = db().collection(USERS).document(username).get().await()
        if (!snap.exists()) return null
        val user = mapToUser(snap)
        return if (user.password == password && user.isBanned == 0) user else null
    }

    suspend fun isUsernameExists(username: String): Int =
        if (db().collection(USERS).document(username).get().await().exists()) 1 else 0

    suspend fun getAllUsers(): List<User> =
        db().collection(USERS).get().await().documents.map { mapToUser(it) }

    suspend fun getTotalUsers(): Int = getAllUsers().size

    suspend fun getBannedUsersCount(): Int =
        db().collection(USERS).whereEqualTo("isBanned", 1L).get().await().documents.size

    suspend fun getUserByUsername(username: String): User? {
        val snap = db().collection(USERS).document(username).get().await()
        return if (snap.exists()) mapToUser(snap) else null
    }

    suspend fun getUserById(userId: Int): User? {
        val docs = db().collection(USERS).whereEqualTo("id", userId.toLong()).limit(1).get().await().documents
        return docs.firstOrNull()?.let { mapToUser(it) }
    }

    suspend fun updateBannedStatus(userId: Int, body: Map<String, Int>): Map<String, Boolean> {
        val status = body["status"] ?: 0
        val docs = db().collection(USERS).whereEqualTo("id", userId.toLong()).get().await().documents
        docs.forEach { it.reference.update("isBanned", status).await() }
        return mapOf("success" to true)
    }

    // =========================================================
    // POST
    // =========================================================

    private fun postToMap(p: Post): Map<String, Any?> = mapOf(
        "id" to p.id,
        "title" to p.title,
        "description" to p.description,
        "price" to p.price,
        "category" to p.category,
        "imagePath" to p.imagePath,
        "isActive" to p.isActive,
        "stock" to p.stock,
        "createdAt" to p.createdAt
    )

    private fun mapToPost(d: DocumentSnapshot): Post = Post(
        id = d.intOf("id", 0),
        title = d.getString("title") ?: "",
        description = d.getString("description") ?: "",
        price = d.doubleOf("price", 0.0),
        category = d.getString("category") ?: "",
        imagePath = d.getString("imagePath"),
        isActive = d.intOf("isActive", 1),
        stock = d.intOf("stock", 0),
        createdAt = d.longOf("createdAt", 0L),
        isSynced = 1
    )

    suspend fun insertPost(post: Post): Long {
        return if (post.id > 0) {
            db().collection(POSTS).document(post.id.toString()).set(postToMap(post)).await()
            post.id.toLong()
        } else {
            db().collection(POSTS).add(postToMap(post)).await()
            0L
        }
    }

    suspend fun updatePost(post: Post): Map<String, Boolean> {
        db().collection(POSTS).document(post.id.toString()).set(postToMap(post)).await()
        return mapOf("success" to true)
    }

    suspend fun deletePost(post: Post): Map<String, Boolean> {
        db().collection(POSTS).document(post.id.toString()).delete().await()
        return mapOf("success" to true)
    }

    suspend fun getAllPosts(): List<Post> =
        db().collection(POSTS).get().await().documents
            .map { mapToPost(it) }
            .sortedByDescending { it.createdAt }

    suspend fun getActivePosts(): List<Post> =
        db().collection(POSTS).whereEqualTo("isActive", 1L).get().await().documents
            .map { mapToPost(it) }
            .sortedByDescending { it.createdAt }

    suspend fun getTotalPosts(): Int =
        db().collection(POSTS).get().await().documents.size

    suspend fun getPostById(postId: Int): Post? {
        val snap = db().collection(POSTS).document(postId.toString()).get().await()
        return if (snap.exists()) mapToPost(snap) else null
    }

    suspend fun decreaseStock(postId: Int): Int {
        val ref = db().collection(POSTS).document(postId.toString())
        val snap = ref.get().await()
        if (!snap.exists()) return 0
        val stock = snap.intOf("stock", 0)
        if (stock <= 0) return 0
        ref.update("stock", stock - 1).await()
        return 1
    }

    // =========================================================
    // POKEMON
    // =========================================================

    private fun pokemonToMap(p: PokemonEntity): Map<String, Any?> = mapOf(
        "id" to p.id,
        "userId" to p.userId,
        "speciesId" to p.speciesId,
        "name" to p.name,
        "hp" to p.hp,
        "imageUrl" to p.imageUrl,
        "level" to p.level,
        "exp" to p.exp,
        "isStarter" to p.isStarter,
        "isLocked" to p.isLocked,
        "caughtAt" to p.caughtAt
    )

    private fun mapToPokemon(d: DocumentSnapshot): PokemonEntity = PokemonEntity(
        id = d.intOf("id", 0),
        userId = d.intOf("userId", 0),
        speciesId = d.intOf("speciesId", 0),
        name = d.getString("name") ?: "",
        hp = d.intOf("hp", 0),
        imageUrl = d.getString("imageUrl") ?: "",
        level = d.intOf("level", 1),
        exp = d.intOf("exp", 0),
        isStarter = d.intOf("isStarter", 0),
        isLocked = d.intOf("isLocked", 0),
        caughtAt = d.longOf("caughtAt", 0L),
        isSynced = 1
    )

    suspend fun insertPokemon(pokemon: PokemonEntity): Long {
        return if (pokemon.id > 0) {
            db().collection(POKEMON).document(pokemon.id.toString()).set(pokemonToMap(pokemon)).await()
            pokemon.id.toLong()
        } else {
            db().collection(POKEMON).add(pokemonToMap(pokemon)).await()
            0L
        }
    }

    suspend fun updatePokemon(pokemon: PokemonEntity): Map<String, Boolean> {
        db().collection(POKEMON).document(pokemon.id.toString()).set(pokemonToMap(pokemon)).await()
        return mapOf("success" to true)
    }

    suspend fun getAllPokemon(): List<PokemonEntity> =
        db().collection(POKEMON).get().await().documents.map { mapToPokemon(it) }

    suspend fun getPokemonByUser(userId: Int): List<PokemonEntity> =
        db().collection(POKEMON).whereEqualTo("userId", userId.toLong()).get().await().documents
            .map { mapToPokemon(it) }

    suspend fun getStarter(userId: Int): PokemonEntity? =
        getPokemonByUser(userId).firstOrNull { it.isStarter == 1 }

    suspend fun deletePokemon(pokemon: PokemonEntity): Map<String, Boolean> {
        db().collection(POKEMON).document(pokemon.id.toString()).delete().await()
        return mapOf("success" to true)
    }

    suspend fun deleteAllUnlockedByUser(userId: Int): Map<String, Boolean> {
        val docs = getPokemonByUser(userId).filter { it.isLocked == 0 }
        docs.forEach {
            db().collection(POKEMON).document(it.id.toString()).delete().await()
        }
        return mapOf("success" to true)
    }

    suspend fun getOwnedSpeciesSummary(userId: Int): List<OwnedSpeciesSummary> =
        getPokemonByUser(userId)
            .filter { it.speciesId != 0 }
            .groupBy { it.speciesId }
            .map { (speciesId, list) ->
                OwnedSpeciesSummary(
                    speciesId = speciesId,
                    highestLevel = list.maxOf { it.level },
                    firstCaughtAt = list.minOf { it.caughtAt }
                )
            }

    // =========================================================
    // INVENTORY
    // =========================================================

    private fun inventoryToMap(i: UserInventory): Map<String, Any?> = mapOf(
        "userId" to i.userId,
        "postId" to i.postId,
        "quantity" to i.quantity
    )

    private fun mapToInventory(d: DocumentSnapshot): UserInventory = UserInventory(
        userId = d.intOf("userId", 0),
        postId = d.intOf("postId", 0),
        quantity = d.intOf("quantity", 0),
        isSynced = 1
    )

    private fun inventoryDocId(userId: Int, postId: Int) = "${userId}_${postId}"

    suspend fun getUserInventory(userId: Int): List<UserInventory> =
        db().collection(INVENTORY).whereEqualTo("userId", userId.toLong()).get().await().documents
            .map { mapToInventory(it) }

    suspend fun getItem(userId: Int, postId: Int): UserInventory? {
        val snap = db().collection(INVENTORY).document(inventoryDocId(userId, postId)).get().await()
        return if (snap.exists()) mapToInventory(snap) else null
    }

    suspend fun syncInventory(item: UserInventory): Map<String, Boolean> {
        db().collection(INVENTORY)
            .document(inventoryDocId(item.userId, item.postId))
            .set(inventoryToMap(item))
            .await()
        return mapOf("success" to true)
    }
}
