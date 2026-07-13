package com.example.proyek_mdp.Data.remote.api

import com.example.proyek_mdp.Data.local.entity.*
import com.example.proyek_mdp.Data.local.entity.OwnedSpeciesSummary
import retrofit2.http.*

interface BackendApiService {
    
    // User
    @POST("api/users")
    suspend fun insertUser(@Body user: User): Map<String, Int>
    
    @PUT("api/users")
    suspend fun updateUser(@Body user: User): Map<String, Boolean>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Map<String, Boolean>

    @POST("api/users/login")
    suspend fun login(@Body body: Map<String, String>): User?

    @GET("api/users/exists/{username}")
    suspend fun isUsernameExists(@Path("username") username: String): Int

    @GET("api/users")
    suspend fun getAllUsers(): List<User>

    @GET("api/users/count")
    suspend fun getTotalUsers(): Int

    @GET("api/users/banned/count")
    suspend fun getBannedUsersCount(): Int

    @GET("api/users/by-username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): User?

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): User?

    @PUT("api/users/{id}/banned")
    suspend fun updateBannedStatus(@Path("id") userId: Int, @Body body: Map<String, Int>): Map<String, Boolean>

    @PUT("api/users/{id}/stats")
    suspend fun updateStats(
        @Path("id") userId: Int,
        @Body body: Map<String, Any>
    ): Map<String, Boolean>

    @GET("api/users/{id}/pokemon-caught")
    suspend fun getPokemonCaught(@Path("id") userId: Int): Int

    @GET("api/users/{id}/trainer-level")
    suspend fun getTrainerLevel(@Path("id") userId: Int): Int

    @GET("api/users/{id}/battle-won")
    suspend fun getBattleWon(@Path("id") userId: Int): Int

    @GET("api/users/{id}/distance")
    suspend fun getDistance(@Path("id") userId: Int): Double

    @PUT("api/users/{id}/coins")
    suspend fun updateCoin(@Path("id") userId: Int, @Body body: Map<String, Int>): Map<String, Boolean>

    // Post
    @POST("api/posts")
    suspend fun insertPost(@Body post: Post): Long

    @PUT("api/posts")
    suspend fun updatePost(@Body post: Post): Map<String, Boolean>

    @HTTP(method = "DELETE", path = "api/posts", hasBody = true)
    suspend fun deletePost(@Body post: Post): Map<String, Boolean>

    @GET("api/posts")
    suspend fun getAllPosts(): List<Post>

    @GET("api/posts/active")
    suspend fun getActivePosts(): List<Post>

    @GET("api/posts/count")
    suspend fun getTotalPosts(): Int

    @GET("api/posts/{id}")
    suspend fun getPostById(@Path("id") postId: Int): Post?

    @PUT("api/posts/{id}/decrease-stock")
    suspend fun decreaseStock(@Path("id") postId: Int): Int

    // User Inventory
    @GET("api/inventory/{userId}")
    suspend fun getUserInventory(@Path("userId") userId: Int): List<UserInventory>

    @GET("api/inventory/{userId}/{postId}")
    suspend fun getItem(@Path("userId") userId: Int, @Path("postId") postId: Int): UserInventory?

    @POST("api/inventory")
    suspend fun insertInventory(@Body item: UserInventory): Map<String, Boolean>

    @PUT("api/inventory")
    suspend fun updateInventory(@Body item: UserInventory): Map<String, Boolean>

    @PUT("api/inventory/sync")
    suspend fun syncInventory(@Body item: UserInventory): Map<String, Boolean>

    @DELETE("api/inventory/{userId}/{postId}")
    suspend fun deleteInventory(@Path("userId") userId: Int, @Path("postId") postId: Int): Map<String, Boolean>

    // Payment History
    @POST("api/payments")
    suspend fun insertPayment(@Body history: PaymentHistory): Map<String, Boolean>

    @GET("api/payments/{userId}")
    suspend fun getPaymentHistory(@Path("userId") userId: Int): List<PaymentHistory>

    // Purchase History
    @POST("api/purchases")
    suspend fun insertPurchase(@Body history: PurchaseHistory): Map<String, Boolean>

    @GET("api/purchases/{userId}")
    suspend fun getPurchaseHistory(@Path("userId") userId: Int): List<PurchaseHistory>

    // Pokemon
    @POST("api/pokemon")
    suspend fun insertPokemon(@Body pokemon: PokemonEntity): Long

    @PUT("api/pokemon")
    suspend fun updatePokemon(@Body pokemon: PokemonEntity): Map<String, Boolean>

    @GET("api/pokemon")
    suspend fun getAllPokemon(): List<PokemonEntity>

    @GET("api/pokemon/user/{userId}")
    suspend fun getPokemonByUser(@Path("userId") userId: Int): List<PokemonEntity>

    @GET("api/pokemon/user/{userId}/starter")
    suspend fun getStarter(@Path("userId") userId: Int): PokemonEntity?

    @HTTP(method = "DELETE", path = "api/pokemon", hasBody = true)
    suspend fun deletePokemon(@Body pokemon: PokemonEntity): Map<String, Boolean>

    @DELETE("api/pokemon/user/{userId}/unlocked")
    suspend fun deleteAllUnlockedByUser(@Path("userId") userId: Int): Map<String, Boolean>

    @GET("api/pokemon/user/{userId}/owned-summary")
    suspend fun getOwnedSpeciesSummary(@Path("userId") userId: Int): List<OwnedSpeciesSummary>

    // Pokedex Species Cache
    @POST("api/species/batch")
    suspend fun insertAllSpecies(@Body speciesList: List<PokedexSpecies>): Map<String, Boolean>

    @GET("api/species/range")
    suspend fun getSpeciesRange(@Query("start") start: Int, @Query("end") end: Int): List<PokedexSpecies>
}
