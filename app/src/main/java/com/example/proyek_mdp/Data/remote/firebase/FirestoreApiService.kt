package com.example.proyek_mdp.Data.remote.firebase

import com.example.proyek_mdp.Data.local.entity.*
import com.example.proyek_mdp.Data.remote.api.BackendApiService
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Implementasi [BackendApiService] yang memakai Cloud Firestore (tanpa MySQL).
 *
 * Dicatat oleh [com.example.proyek_mdp.Data.remote.api.BackendRetrofitClient] sehingga
 * seluruh Repository & DAO otomatis memakai Firestore tanpa perubahan di sisi pemanggil.
 *
 * Strategi ID dokumen:
 *  - users            -> documentId = username (field "id" menyimpan nomor user)
 *  - posts            -> documentId = id
 *  - pokemon          -> documentId = id
 *  - user_inventory   -> documentId = "{userId}_{postId}"
 *  - payment_history  -> auto
 *  - purchase_history -> auto
 *  - pokedex_species  -> documentId = speciesId
 */
class FirestoreApiService : BackendApiService {

    private val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    private companion object {
        const val USERS = "users"
        const val POSTS = "posts"
        const val POKEMON = "pokemon"
        const val INVENTORY = "user_inventory"
        const val PAYMENTS = "payment_history"
        const val PURCHASES = "purchase_history"
        const val SPECIES = "pokedex_species"
    }

    // ---------- helpers ----------
    private fun DocumentSnapshot.intOf(f: String, d: Int = 0) = (get(f) as? Number)?.toInt() ?: d
    private fun DocumentSnapshot.longOf(f: String, d: Long = 0L) = (get(f) as? Number)?.toLong() ?: d
    private fun DocumentSnapshot.doubleOf(f: String, d: Double = 0.0) = (get(f) as? Number)?.toDouble() ?: d

    private suspend fun nextId(collection: String): Int {
        val docs = db.collection(collection).get().await().documents
        val maxId = docs.mapNotNull { (it.get("id") as? Number)?.toInt() }.maxOrNull() ?: 0
        return maxId + 1
    }

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

    private fun mapToUser(d: DocumentSnapshot) = User(
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

    private fun mapToPost(d: DocumentSnapshot) = Post(
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

    private fun mapToPokemon(d: DocumentSnapshot) = PokemonEntity(
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

    // =========================================================
    // USER
    // =========================================================
    override suspend fun insertUser(user: User): Map<String, Int> {
        val id = if (user.id > 0) user.id else nextId(USERS)
        db.collection(USERS).document(user.username).set(userToMap(user.copy(id = id))).await()
        return mapOf("id" to id)
    }

    override suspend fun updateUser(user: User): Map<String, Boolean> {
        db.collection(USERS).document(user.username).set(userToMap(user)).await()
        return mapOf("success" to true)
    }

    override suspend fun deleteUser(id: Int): Map<String, Boolean> {
        db.collection(USERS).whereEqualTo("id", id.toLong()).get().await().documents
            .forEach { it.reference.delete().await() }
        return mapOf("success" to true)
    }

    override suspend fun login(body: Map<String, String>): User? {
        val username = body["username"] ?: return null
        val password = body["password"] ?: return null
        val snap = db.collection(USERS).document(username).get().await()
        if (!snap.exists()) return null
        val user = mapToUser(snap)
        return if (user.password == password && user.isBanned == 0) user else null
    }

    override suspend fun isUsernameExists(username: String): Int =
        if (db.collection(USERS).document(username).get().await().exists()) 1 else 0

    override suspend fun getAllUsers(): List<User> =
        db.collection(USERS).get().await().documents.map { mapToUser(it) }

    override suspend fun getTotalUsers(): Int =
        db.collection(USERS).get().await().documents.size

    override suspend fun getBannedUsersCount(): Int =
        db.collection(USERS).get().await().documents.count { it.intOf("isBanned", 0) == 1 }

    override suspend fun getUserByUsername(username: String): User? {
        val snap = db.collection(USERS).document(username).get().await()
        return if (snap.exists()) mapToUser(snap) else null
    }

    override suspend fun getUserById(userId: Int): User? =
        db.collection(USERS).whereEqualTo("id", userId.toLong()).limit(1).get().await()
            .documents.firstOrNull()?.let { mapToUser(it) }

    override suspend fun updateBannedStatus(userId: Int, body: Map<String, Int>): Map<String, Boolean> {
        val status = body["status"] ?: 0
        db.collection(USERS).whereEqualTo("id", userId.toLong()).get().await().documents
            .forEach { it.reference.update("isBanned", status).await() }
        return mapOf("success" to true)
    }

    override suspend fun updateStats(userId: Int, body: Map<String, Any>): Map<String, Boolean> {
        val updates = HashMap<String, Any>()
        body["pokemonCaught"]?.let { updates["pokemonCaught"] = it }
        body["trainerLevel"]?.let { updates["trainerLevel"] = it }
        body["battleWon"]?.let { updates["battleWon"] = it }
        body["distance"]?.let { updates["distance"] = it }
        db.collection(USERS).whereEqualTo("id", userId.toLong()).get().await().documents
            .forEach { it.reference.update(updates).await() }
        return mapOf("success" to true)
    }

    override suspend fun getPokemonCaught(userId: Int): Int =
        getUserById(userId)?.pokemonCaught ?: 0

    override suspend fun getTrainerLevel(userId: Int): Int =
        getUserById(userId)?.trainerLevel ?: 1

    override suspend fun getBattleWon(userId: Int): Int =
        getUserById(userId)?.battleWon ?: 0

    override suspend fun getDistance(userId: Int): Double =
        getUserById(userId)?.distance ?: 0.0

    override suspend fun updateCoin(userId: Int, body: Map<String, Int>): Map<String, Boolean> {
        val coins = body["coins"] ?: 0
        db.collection(USERS).whereEqualTo("id", userId.toLong()).get().await().documents
            .forEach { it.reference.update("coins", coins).await() }
        return mapOf("success" to true)
    }

    // =========================================================
    // POST
    // =========================================================
    override suspend fun insertPost(post: Post): Long {
        return if (post.id > 0) {
            db.collection(POSTS).document(post.id.toString()).set(postToMap(post)).await()
            post.id.toLong()
        } else {
            val id = nextId(POSTS)
            db.collection(POSTS).document(id.toString()).set(postToMap(post.copy(id = id))).await()
            id.toLong()
        }
    }

    override suspend fun updatePost(post: Post): Map<String, Boolean> {
        db.collection(POSTS).document(post.id.toString()).set(postToMap(post)).await()
        return mapOf("success" to true)
    }

    override suspend fun deletePost(post: Post): Map<String, Boolean> {
        db.collection(POSTS).document(post.id.toString()).delete().await()
        return mapOf("success" to true)
    }

    override suspend fun getAllPosts(): List<Post> =
        db.collection(POSTS).get().await().documents.map { mapToPost(it) }
            .sortedByDescending { it.createdAt }

    override suspend fun getActivePosts(): List<Post> =
        db.collection(POSTS).get().await().documents.map { mapToPost(it) }
            .filter { it.isActive == 1 }
            .sortedByDescending { it.createdAt }

    override suspend fun getTotalPosts(): Int =
        db.collection(POSTS).get().await().documents.size

    override suspend fun getPostById(postId: Int): Post? {
        val snap = db.collection(POSTS).document(postId.toString()).get().await()
        return if (snap.exists()) mapToPost(snap) else null
    }

    override suspend fun decreaseStock(postId: Int): Int {
        val ref = db.collection(POSTS).document(postId.toString())
        val snap = ref.get().await()
        if (!snap.exists()) return 0
        val stock = snap.intOf("stock", 0)
        if (stock <= 0) return 0
        ref.update("stock", stock - 1).await()
        return 1
    }

    // =========================================================
    // INVENTORY
    // =========================================================
    private fun invDocId(userId: Int, postId: Int) = "${userId}_${postId}"

    private fun invToMap(i: UserInventory): Map<String, Any?> = mapOf(
        "userId" to i.userId,
        "postId" to i.postId,
        "quantity" to i.quantity
    )

    private fun mapToInv(d: DocumentSnapshot) = UserInventory(
        userId = d.intOf("userId", 0),
        postId = d.intOf("postId", 0),
        quantity = d.intOf("quantity", 0),
        isSynced = 1
    )

    override suspend fun getUserInventory(userId: Int): List<UserInventory> =
        db.collection(INVENTORY).get().await().documents.map { mapToInv(it) }
            .filter { it.userId == userId }

    override suspend fun getItem(userId: Int, postId: Int): UserInventory? {
        val snap = db.collection(INVENTORY).document(invDocId(userId, postId)).get().await()
        return if (snap.exists()) mapToInv(snap) else null
    }

    override suspend fun insertInventory(item: UserInventory): Map<String, Boolean> {
        db.collection(INVENTORY).document(invDocId(item.userId, item.postId)).set(invToMap(item)).await()
        return mapOf("success" to true)
    }

    override suspend fun updateInventory(item: UserInventory): Map<String, Boolean> {
        db.collection(INVENTORY).document(invDocId(item.userId, item.postId)).set(invToMap(item)).await()
        return mapOf("success" to true)
    }

    override suspend fun syncInventory(item: UserInventory): Map<String, Boolean> {
        db.collection(INVENTORY).document(invDocId(item.userId, item.postId)).set(invToMap(item)).await()
        return mapOf("success" to true)
    }

    override suspend fun deleteInventory(userId: Int, postId: Int): Map<String, Boolean> {
        db.collection(INVENTORY).document(invDocId(userId, postId)).delete().await()
        return mapOf("success" to true)
    }

    // =========================================================
    // PAYMENTS
    // =========================================================
    override suspend fun insertPayment(history: PaymentHistory): Map<String, Boolean> {
        db.collection(PAYMENTS).add(mapOf(
            "userId" to history.userId,
            "paymentMethod" to history.paymentMethod,
            "coinAmount" to history.coinAmount,
            "totalPrice" to history.totalPrice,
            "status" to history.status,
            "transactionDate" to history.transactionDate
        )).await()
        return mapOf("success" to true)
    }

    override suspend fun getPaymentHistory(userId: Int): List<PaymentHistory> =
        db.collection(PAYMENTS).get().await().documents
            .map {
                PaymentHistory(
                    id = it.id.hashCode(),
                    userId = it.intOf("userId", 0),
                    paymentMethod = it.getString("paymentMethod") ?: "",
                    coinAmount = it.intOf("coinAmount", 0),
                    totalPrice = it.intOf("totalPrice", 0),
                    status = it.getString("status") ?: "",
                    transactionDate = it.longOf("transactionDate", 0L)
                )
            }
            .filter { it.userId == userId }
            .sortedByDescending { it.transactionDate }

    // =========================================================
    // PURCHASES
    // =========================================================
    override suspend fun insertPurchase(history: PurchaseHistory): Map<String, Boolean> {
        db.collection(PURCHASES).add(mapOf(
            "userId" to history.userId,
            "postId" to history.postId,
            "itemName" to history.itemName,
            "price" to history.price,
            "quantity" to history.quantity,
            "purchaseDate" to history.purchaseDate
        )).await()
        return mapOf("success" to true)
    }

    override suspend fun getPurchaseHistory(userId: Int): List<PurchaseHistory> =
        db.collection(PURCHASES).get().await().documents
            .map {
                PurchaseHistory(
                    id = it.id.hashCode(),
                    userId = it.intOf("userId", 0),
                    postId = it.intOf("postId", 0),
                    itemName = it.getString("itemName") ?: "",
                    price = it.intOf("price", 0),
                    quantity = it.intOf("quantity", 0),
                    purchaseDate = it.longOf("purchaseDate", 0L)
                )
            }
            .filter { it.userId == userId }
            .sortedByDescending { it.purchaseDate }

    // =========================================================
    // POKEMON
    // =========================================================
    override suspend fun insertPokemon(pokemon: PokemonEntity): Long {
        val id = if (pokemon.id > 0) pokemon.id else nextId(POKEMON)
        db.collection(POKEMON).document(id.toString()).set(pokemonToMap(pokemon.copy(id = id))).await()
        return id.toLong()
    }

    override suspend fun updatePokemon(pokemon: PokemonEntity): Map<String, Boolean> {
        db.collection(POKEMON).document(pokemon.id.toString()).set(pokemonToMap(pokemon)).await()
        return mapOf("success" to true)
    }

    override suspend fun getAllPokemon(): List<PokemonEntity> =
        db.collection(POKEMON).get().await().documents.map { mapToPokemon(it) }

    override suspend fun getPokemonByUser(userId: Int): List<PokemonEntity> =
        db.collection(POKEMON).get().await().documents.map { mapToPokemon(it) }
            .filter { it.userId == userId }

    override suspend fun getStarter(userId: Int): PokemonEntity? =
        getPokemonByUser(userId).firstOrNull { it.isStarter == 1 }

    override suspend fun deletePokemon(pokemon: PokemonEntity): Map<String, Boolean> {
        db.collection(POKEMON).document(pokemon.id.toString()).delete().await()
        return mapOf("success" to true)
    }

    override suspend fun deleteAllUnlockedByUser(userId: Int): Map<String, Boolean> {
        getPokemonByUser(userId).filter { it.isLocked == 0 }.forEach {
            db.collection(POKEMON).document(it.id.toString()).delete().await()
        }
        return mapOf("success" to true)
    }

    override suspend fun getOwnedSpeciesSummary(userId: Int): List<OwnedSpeciesSummary> =
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
    // SPECIES CACHE
    // =========================================================
    override suspend fun insertAllSpecies(speciesList: List<PokedexSpecies>): Map<String, Boolean> {
        speciesList.forEach { s ->
            db.collection(SPECIES).document(s.speciesId.toString()).set(mapOf(
                "speciesId" to s.speciesId,
                "name" to s.name,
                "imageUrl" to s.imageUrl,
                "type1" to s.type1,
                "type2" to s.type2
            )).await()
        }
        return mapOf("success" to true)
    }

    override suspend fun getSpeciesRange(start: Int, end: Int): List<PokedexSpecies> =
        db.collection(SPECIES).get().await().documents
            .map {
                PokedexSpecies(
                    speciesId = it.intOf("speciesId", 0),
                    name = it.getString("name") ?: "",
                    imageUrl = it.getString("imageUrl") ?: "",
                    type1 = it.getString("type1") ?: "",
                    type2 = it.getString("type2")
                )
            }
            .filter { it.speciesId in start..end }
            .sortedBy { it.speciesId }
}
