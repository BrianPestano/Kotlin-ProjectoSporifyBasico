package com.example.spotifybrianlauren.ExoPlayer

import android.content.ContentResolver
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.spotifybrianlauren.BBDD.BBDD.ListaCanciones
import com.example.spotifybrianlauren.BBDD.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrincipalViewModel : ViewModel() {

    // Estado del reproductor ExoPlayer
    private val _exoPlayer: MutableStateFlow<ExoPlayer?> = MutableStateFlow(null)
    val exoPlayer = _exoPlayer.asStateFlow()

    // Estado del modo de bucle
    private val _isLoopMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoopMode = _isLoopMode.asStateFlow()

    // Estado del modo de reproducción aleatoria
    private val _isRandomMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRandomMode = _isRandomMode.asStateFlow()

    // Índice de la canción actual
    private val _currentSongIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentSongIndex = _currentSongIndex.asStateFlow()

    // Información de la canción actual
    private val _actual = MutableStateFlow(ListaCanciones[_currentSongIndex.value].cancion)
    val actual = _actual.asStateFlow()

    // Duración total de la canción
    private val _duracion = MutableStateFlow(0)
    val duracion = _duracion.asStateFlow()

    // Progreso actual de reproducción
    private val _progreso = MutableStateFlow(0)
    val progreso = _progreso.asStateFlow()

    // Crear el reproductor ExoPlayer
    fun crearExoPlayer(context: Context) {
        _exoPlayer.value = ExoPlayer.Builder(context).build()
        _exoPlayer.value!!.prepare()
    }

    // Iniciar la reproducción de música
    fun hacerSonarMusica(context: Context) {
        val mediaItem = MediaItem.fromUri(obtenerRuta(context, _currentSongIndex.value))

        _exoPlayer.value!!.setMediaItem(mediaItem)
        _exoPlayer.value!!.prepare()
        _exoPlayer.value!!.playWhenReady = true
    }

    // Manejar el cambio de estado de reproducción
    private fun handlePlaybackStateChanged(playbackState: Int, context: Context) {
        if (playbackState == Player.STATE_READY) {
            _duracion.value = (_exoPlayer.value!!.duration / 1000).toInt()
            viewModelScope.launch {
                while (_exoPlayer.value!!.isPlaying) {
                    _progreso.value = (_exoPlayer.value!!.currentPosition / 1000).toInt()
                    delay(1000)
                }
            }
        } else if (playbackState == Player.STATE_ENDED) {
            playNext(context)
        }
    }

    // Obtener la ruta de la canción
    private fun obtenerRuta(context: Context, songIndex: Int): String {
        setCurrentSongIndex(songIndex)

        val currentSong = getCurrentSong().cancion

        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.packageName + '/' + context.resources.getResourceTypeName(currentSong) + '/' +
                context.resources.getResourceEntryName(currentSong)
    }

    // Obtener la canción actual
    fun getCurrentSong(): Song {
        return ListaCanciones[_currentSongIndex.value]
    }

    // Obtener el índice de la siguiente canción
    fun getNextSongIndex(): Int {
        return if (_isRandomMode.value) {
            (0 until ListaCanciones.size).random()
        } else {
            (_currentSongIndex.value + 1) % ListaCanciones.size
        }
    }

    // Alternar el modo de bucle
    fun toggleLoopMode() {
        _isLoopMode.value = !_isLoopMode.value

        if (_isLoopMode.value) {
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_ONE
        } else {
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_OFF
        }

        if (_isLoopMode.value) {
            _isRandomMode.value = false
        }
    }

    // Alternar el modo de reproducción aleatoria
    fun toggleRandomMode() {
        _isRandomMode.value = !_isRandomMode.value

        if (_isRandomMode.value) {
            _exoPlayer.value?.shuffleModeEnabled = true
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_OFF
        } else {
            _exoPlayer.value?.shuffleModeEnabled = false
        }

        if (_isRandomMode.value) {
            _isLoopMode.value = false
        }
    }

    // Establecer el índice de la canción actual
    fun setCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    // Inicializar el ExoPlayer y añadir un listener
    fun inicializarEP(context: Context) {
        _exoPlayer.value = ExoPlayer.Builder(context).build()
        _exoPlayer.value!!.prepare()
        _exoPlayer.value!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                handlePlaybackStateChanged(playbackState, context)
            }
        })
    }

    // Liberar recursos cuando se destruye el ViewModel
    override fun onCleared() {
        _exoPlayer.value?.release()
        super.onCleared()
    }

    // Pausar o reanudar la reproducción
    fun pausarOSeguirMusica(): Boolean {
        return if (_exoPlayer.value!!.isPlaying) {
            _exoPlayer.value!!.pause()
            true // Devuelve true si la música está en pausa
        } else {
            _exoPlayer.value!!.play()
            false // Devuelve false si la música se está reproduciendo
        }
    }

    // Reproducir la canción anterior
    fun playPrevious(context: Context) {
        setCurrentSongIndex((_currentSongIndex.value - 1 + ListaCanciones.size) % ListaCanciones.size)

        _actual.value = ListaCanciones[_currentSongIndex.value].cancion

        val mediaItem = MediaItem.fromUri(obtenerRuta(context, _currentSongIndex.value))
        _exoPlayer.value?.setMediaItem(mediaItem)
        _exoPlayer.value?.prepare()

        if (_isLoopMode.value) {
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_ONE
        }

        _exoPlayer.value?.playWhenReady = true
    }

    // Reproducir la siguiente canción
    fun playNext(context: Context) {
        val nextSongIndex = if (_isRandomMode.value) {
            (0 until ListaCanciones.size).random()
        } else {
            (_currentSongIndex.value + 1) % ListaCanciones.size
        }

        setCurrentSongIndex(nextSongIndex)

        _actual.value = ListaCanciones[_currentSongIndex.value].cancion

        val mediaItem = MediaItem.fromUri(obtenerRuta(context, _currentSongIndex.value))
        _exoPlayer.value?.setMediaItem(mediaItem)
        _exoPlayer.value?.prepare()

        if (_isRandomMode.value) {
            _exoPlayer.value?.shuffleModeEnabled = true
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_OFF
        }

        if (_isLoopMode.value) {
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_ONE
        }

        _exoPlayer.value?.playWhenReady = true
    }

    // Saltar a una posición específica de la canción
    fun seekTo(positionMillis: Long) {
        _exoPlayer.value?.seekTo(positionMillis)
    }

    // Reproducir una canción específica(para el search bar)
    fun playSong(context: Context, songIndex: Int) {
        setCurrentSongIndex(songIndex)

        val mediaItem = MediaItem.fromUri(obtenerRuta(context, songIndex))
        _exoPlayer.value?.setMediaItem(mediaItem)
        _exoPlayer.value?.prepare()
        _exoPlayer.value?.playWhenReady = true
    }
}