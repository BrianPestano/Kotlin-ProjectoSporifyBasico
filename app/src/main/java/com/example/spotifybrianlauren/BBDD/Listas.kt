package com.example.spotifybrianlauren.BBDD

import com.example.spotifybrianlauren.R

data class Song(
    val name: String,
    val coverResourceId: Int,
    val cancion: Int,
    val duration: String
)

object BBDD {
    val ListaCanciones: List<Song> = listOf(
        Song( "Ibai Farsante Cover", R.drawable.ibai, R.raw.ibai, "02:02"),
        Song( "I Play Pokemon Go", R.drawable.iplaypokemon,R.raw.iplaypokemon,  "01:15"),
        Song( "Kingdom Hearts", R.drawable.kingdomhearts,R.raw.kingdomhearts,   "04:26"),
        Song( "Minero ft Rubius", R.drawable.minero,R.raw.minero,   "3:53"),
        Song( "Yo tengo un moco", R.drawable.moco,R.raw.moco,   "00:47"),
        Song( "Me pico un mosquito", R.drawable.mosquito,R.raw.mosquito,   "02:18"),
        Song( "Rickroll", R.drawable.rickroll,R.raw.rickroll,   "03:32"),
        Song( "Rocky Tiger", R.drawable.rocky, R.raw.rocky,  "04:04"),
        Song( "Vegetta", R.drawable.vegetta,R.raw.vegetta,   "03:21"),
        Song( "Vinito Barato", R.drawable.vinitobarato,R.raw.vinitobarato,   "03:22")
    )
}