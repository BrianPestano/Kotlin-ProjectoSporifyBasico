package com.example.spotifybrianlauren.ExoPlayer

sealed class Rutas(val ruta : String) {
    object Player : Rutas("player")
    object Foto : Rutas("foto")
}