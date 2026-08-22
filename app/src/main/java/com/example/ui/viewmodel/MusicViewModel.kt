package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.KadjaApplication
import com.example.data.local.PlaylistEntity
import com.example.data.model.*
import com.example.player.AudioPlayerEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab(val titleRes: String) {
    SONGS("الأغاني"),
    FAVORITES("المفضلة"),
    PLAYLISTS("قوائم التشغيل"),
    ARTISTS("الفنانين"),
    ALBUMS("الألبومات"),
    FOLDERS("المجلدات")
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as KadjaApplication
    private val repository = app.musicRepository
    val playerEngine: AudioPlayerEngine = app.playerEngine

    // All songs from storage + demo synth
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs = _allSongs.asStateFlow()

    private val _favoriteSongIds = MutableStateFlow<Set<Long>>(emptySet())
    val favoriteSongIds = _favoriteSongIds.asStateFlow()

    private val _playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    val playlists = _playlists.asStateFlow()

    // Navigation & UI State
    private val _selectedTab = MutableStateFlow(MainTab.SONGS)
    val selectedTab = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.TITLE_ASC)
    val sortOption = _sortOption.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded = _isNowPlayingExpanded.asStateFlow()

    private val _isEqualizerOpen = MutableStateFlow(false)
    val isEqualizerOpen = _isEqualizerOpen.asStateFlow()

    private val _isSleepTimerOpen = MutableStateFlow(false)
    val isSleepTimerOpen = _isSleepTimerOpen.asStateFlow()

    private val _isQueueOpen = MutableStateFlow(false)
    val isQueueOpen = _isQueueOpen.asStateFlow()

    private val _songForAddToPlaylist = MutableStateFlow<Song?>(null)
    val songForAddToPlaylist = _songForAddToPlaylist.asStateFlow()

    private val _isCreatePlaylistOpen = MutableStateFlow(false)
    val isCreatePlaylistOpen = _isCreatePlaylistOpen.asStateFlow()

    // Sub-view filters
    private val _selectedArtist = MutableStateFlow<String?>(null)
    val selectedArtist = _selectedArtist.asStateFlow()

    private val _selectedAlbum = MutableStateFlow<String?>(null)
    val selectedAlbum = _selectedAlbum.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder = _selectedFolder.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<PlaylistEntity?>(null)
    val selectedPlaylist = _selectedPlaylist.asStateFlow()

    private val _playlistSongIds = MutableStateFlow<List<Long>>(emptyList())

    init {
        loadData()
        observeFavorites()
        observePlaylists()
    }

    fun loadData() {
        viewModelScope.launch {
            val songs = repository.loadAllDeviceSongs()
            _allSongs.value = songs
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavoritesIdsFlow().collect { ids ->
                _favoriteSongIds.value = ids.toSet()
                _allSongs.update { currentList ->
                    currentList.map { song ->
                        song.copy(isFavorite = ids.contains(song.id))
                    }
                }
            }
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            repository.getAllPlaylistsFlow().collect { list ->
                _playlists.value = list
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
        _selectedArtist.value = null
        _selectedAlbum.value = null
        _selectedFolder.value = null
        _selectedPlaylist.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setEqualizerOpen(open: Boolean) {
        _isEqualizerOpen.value = open
    }

    fun setSleepTimerOpen(open: Boolean) {
        _isSleepTimerOpen.value = open
    }

    fun setQueueOpen(open: Boolean) {
        _isQueueOpen.value = open
    }

    fun setSongForAddToPlaylist(song: Song?) {
        _songForAddToPlaylist.value = song
    }

    fun setCreatePlaylistOpen(open: Boolean) {
        _isCreatePlaylistOpen.value = open
    }

    fun selectArtist(artistName: String?) {
        _selectedArtist.value = artistName
    }

    fun selectAlbum(albumTitle: String?) {
        _selectedAlbum.value = albumTitle
    }

    fun selectFolder(folderName: String?) {
        _selectedFolder.value = folderName
    }

    fun selectPlaylist(playlist: PlaylistEntity?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            viewModelScope.launch {
                repository.getSongsForPlaylist(playlist.id).collect { ids ->
                    _playlistSongIds.value = ids
                }
            }
        } else {
            _playlistSongIds.value = emptyList()
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song.id)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val id = repository.createPlaylist(name)
                _isCreatePlaylistOpen.value = false
                _songForAddToPlaylist.value?.let { song ->
                    repository.addSongToPlaylist(id, song.id)
                    _songForAddToPlaylist.value = null
                }
            }
        }
    }

    fun renamePlaylist(id: Long, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(id, newName)
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(id)
            if (_selectedPlaylist.value?.id == id) {
                _selectedPlaylist.value = null
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song.id)
            _songForAddToPlaylist.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getFilteredSongs(): List<Song> {
        val query = _searchQuery.value.trim().lowercase()
        var list = _allSongs.value

        if (_selectedArtist.value != null) {
            list = list.filter { it.artist.equals(_selectedArtist.value, ignoreCase = true) }
        } else if (_selectedAlbum.value != null) {
            list = list.filter { it.album.equals(_selectedAlbum.value, ignoreCase = true) }
        } else if (_selectedFolder.value != null) {
            list = list.filter { it.folderName.equals(_selectedFolder.value, ignoreCase = true) }
        } else if (_selectedPlaylist.value != null) {
            val ids = _playlistSongIds.value.toSet()
            list = list.filter { ids.contains(it.id) }
        } else {
            when (_selectedTab.value) {
                MainTab.SONGS -> {}
                MainTab.FAVORITES -> {
                    val favIds = _favoriteSongIds.value
                    list = list.filter { favIds.contains(it.id) }
                }
                else -> {}
            }
        }

        if (query.isNotEmpty()) {
            list = list.filter {
                it.title.lowercase().contains(query) ||
                it.artist.lowercase().contains(query) ||
                it.album.lowercase().contains(query) ||
                it.folderName.lowercase().contains(query)
            }
        }

        return when (_sortOption.value) {
            SortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            SortOption.ARTIST_ASC -> list.sortedBy { it.artist.lowercase() }
            SortOption.DURATION_DESC -> list.sortedByDescending { it.durationMs }
            SortOption.DATE_ADDED_DESC -> list.sortedByDescending { it.dateAdded }
        }
    }

    fun getArtists(): List<Artist> {
        val query = _searchQuery.value.trim().lowercase()
        val all = _allSongs.value
        val grouped = all.groupBy { it.artist }
        val artists = grouped.entries.map { entry ->
            val artistName = entry.key
            val songs = entry.value
            val albumCount = songs.map { it.album }.distinct().size
            Artist(name = artistName, songCount = songs.size, albumsCount = albumCount)
        }.sortedBy { it.name.lowercase() }

        if (query.isNotEmpty()) {
            return artists.filter { it.name.lowercase().contains(query) }
        }
        return artists
    }

    fun getAlbums(): List<Album> {
        val query = _searchQuery.value.trim().lowercase()
        val all = _allSongs.value
        val grouped = all.groupBy { it.album }
        val albums = grouped.entries.mapIndexed { index, entry ->
            val albumTitle = entry.key
            val songs = entry.value
            val firstSong = songs.firstOrNull()
            Album(
                id = index.toLong(),
                title = albumTitle,
                artist = firstSong?.artist ?: "Unknown Artist",
                songCount = songs.size,
                albumArtUri = firstSong?.albumArtUri
            )
        }.sortedBy { it.title.lowercase() }

        if (query.isNotEmpty()) {
            return albums.filter { it.title.lowercase().contains(query) || it.artist.lowercase().contains(query) }
        }
        return albums
    }

    fun getFolders(): List<Folder> {
        val query = _searchQuery.value.trim().lowercase()
        val all = _allSongs.value
        val grouped = all.groupBy { it.folderName }
        val folders = grouped.entries.map { entry ->
            val folderName = entry.key
            val songs = entry.value
            Folder(name = folderName, path = folderName, songCount = songs.size)
        }.sortedBy { it.name.lowercase() }

        if (query.isNotEmpty()) {
            return folders.filter { it.name.lowercase().contains(query) }
        }
        return folders
    }

    fun playSongFromList(song: Song, list: List<Song>) {
        val index = list.indexOfFirst { it.id == song.id }
        if (index != -1) {
            playerEngine.playQueue(list, index)
        } else {
            playerEngine.playSong(song)
        }
    }

    fun shuffleAll(list: List<Song>) {
        if (list.isNotEmpty()) {
            val shuffled = list.shuffled()
            playerEngine.playQueue(shuffled, 0)
        }
    }
}
