package com.example.proyek_mdp.UI.Model



data class PokemonResponse(

    val name: String,

    val height: Int,

    val weight: Int,

    val sprites: Sprites,

    val types: List<TypeSlot>
)

data class Sprites(

    val front_default: String
)

data class TypeSlot(

    val type: PokemonType
)

data class PokemonType(

    val name: String
)