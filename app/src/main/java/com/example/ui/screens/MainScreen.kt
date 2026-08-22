package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.PlaylistEntity
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MusicViewModel,
    hasStoragePermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    val currentSong by viewModel.playerEngine.currentSong.collectAsState()
    val isPlaying by viewModel.playerEngine.isPlaying.collectAsState()
    val currentPositionMs by viewModel.playerEngine.currentPositionMs.collectAsState()
    val durationMs by viewModel.playerEngine.durationMs.collectAsState()
    val repeatMode by viewModel.playerEngine.repeatMode.collectAsState()
    val isShuffle by viewModel.playerEngine.isShuffle.collectAsState()
    val queue by viewModel.playerEngine.queue.collectAsState()
    val queueIndex by viewModel.playerEngine.queueIndex.collectAsState()
    val sleepTimerMinutesLeft by viewModel.playerEngine.sleepTimerMinutesLeft.collectAsState()
    val playbackSpeed by viewModel.playerEngine.playbackSpeed.collectAsState()
    val selectedPreset by viewModel.playerEngine.selectedPreset.collectAsState()
    val bassBoostStrength by viewModel.playerEngine.bassBoostStrength.collectAsState()
    val volume by viewModel.playerEngine.volume.collectAsState()

    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val isEqualizerOpen by viewModel.isEqualizerOpen.collectAsState()
    val isSleepTimerOpen by viewModel.isSleepTimerOpen.collectAsState()
    val isQueueOpen by viewModel.isQueueOpen.collectAsState()
    val songForAddToPlaylist by viewModel.songForAddToPlaylist.collectAsState()
    val isCreatePlaylistOpen by viewModel.isCreatePlaylistOpen.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val selectedArtist by viewModel.selectedArtist.collectAsState()
    val selectedAlbum by viewModel.selectedAlbum.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }

    val filteredSongs = remember(
        viewModel.allSongs.collectAsState().value,
        selectedTab,
        searchQuery,
        sortOption,
        selectedArtist,
        selectedAlbum,
        selectedFolder,
        selectedPlaylist,
        viewModel.favoriteSongIds.collectAsState().value
    ) {
        viewModel.getFilteredSongs()
    }

    val actualDuration = if (durationMs > 0) durationMs else (currentSong?.durationMs ?: 1L)
    val progressFraction = if (actualDuration > 0) (currentPositionMs.toFloat() / actualDuration.toFloat()).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KadjaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (currentSong != null) 72.dp else 0.dp)
        ) {
            // Header / Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Logo Image
                    Image(
                        painter = painterResource(id = R.drawable.ic_kadja_logo),
                        contentDescription = "Kadja Player Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Column {
                        Text(
                            text = "Kadja Player",
                            color = KadjaTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "مشغل الموسيقى الحديث",
                            color = KadjaPrimaryGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Search toggle button
                    IconButton(
                        onClick = { viewModel.toggleSearch(!isSearchActive) },
                        modifier = Modifier.testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchActive) KadjaPrimaryGlow else KadjaTextSecondary
                        )
                    }

                    // Sort button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = KadjaTextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(KadjaSurfaceElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("اسم الأغنية (أ - ي)", color = KadjaTextPrimary) },
                                onClick = {
                                    viewModel.setSortOption(SortOption.TITLE_ASC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("اسم الفنان", color = KadjaTextPrimary) },
                                onClick = {
                                    viewModel.setSortOption(SortOption.ARTIST_ASC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("الأطول مدة", color = KadjaTextPrimary) },
                                onClick = {
                                    viewModel.setSortOption(SortOption.DURATION_DESC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("تاريخ الإضافة الأحدث", color = KadjaTextPrimary) },
                                onClick = {
                                    viewModel.setSortOption(SortOption.DATE_ADDED_DESC)
                                    showSortMenu = false
                                }
                            )
                        }
                    }

                    // Equalizer shortcut
                    IconButton(
                        onClick = { viewModel.setEqualizerOpen(true) },
                        modifier = Modifier.testTag("header_eq_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Equalizer",
                            tint = KadjaPrimaryGlow
                        )
                    }
                }
            }

            // Search Bar Input
            AnimatedVisibility(
                visible = isSearchActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("ابحث عن أغنية، فنان، ألبوم...", color = KadjaTextTertiary, fontSize = 14.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = KadjaPrimaryGlow)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = KadjaTextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KadjaPrimary,
                        unfocusedBorderColor = KadjaCardBorder,
                        focusedContainerColor = KadjaSurfaceElevated,
                        unfocusedContainerColor = KadjaSurface,
                        focusedTextColor = KadjaTextPrimary,
                        unfocusedTextColor = KadjaTextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("search_text_field")
                )
            }

            // Permission Banner if storage permission is not granted
            if (!hasStoragePermission) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = KadjaSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, KadjaPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = KadjaPrimaryGlow,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الوصول إلى ملفات الجهاز",
                                color = KadjaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "امنح الإذن لتشغيل ملفات MP3 المخزنة على هاتفك.",
                                color = KadjaTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = KadjaPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("grant_permission_button")
                        ) {
                            Text("منح الإذن", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // Category Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = KadjaPrimaryGlow,
                edgePadding = 16.dp,
                divider = {},
                indicator = {}
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = (selectedTab == tab && selectedArtist == null && selectedAlbum == null && selectedFolder == null && selectedPlaylist == null)
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp, top = 4.dp, bottom = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectTab(tab) }
                            .testTag("tab_${tab.name}"),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) KadjaPrimary else KadjaSurfaceElevated,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, KadjaCardBorder)
                    ) {
                        Text(
                            text = tab.titleRes,
                            color = if (isSelected) Color.White else KadjaTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Sub-Selection Header (e.g. Inside Artist or Album or Folder or Playlist)
            if (selectedArtist != null || selectedAlbum != null || selectedFolder != null || selectedPlaylist != null) {
                val title = selectedArtist ?: selectedAlbum ?: selectedFolder ?: selectedPlaylist?.name ?: ""
                Surface(
                    color = KadjaSurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    viewModel.selectArtist(null)
                                    viewModel.selectAlbum(null)
                                    viewModel.selectFolder(null)
                                    viewModel.selectPlaylist(null)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = KadjaPrimaryGlow)
                            }
                            Text(
                                text = title,
                                color = KadjaTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = "${filteredSongs.size} أغنية",
                            color = KadjaTextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Quick Actions Bar: Shuffle All & Play All Buttons
            if (selectedTab == MainTab.SONGS || selectedTab == MainTab.FAVORITES || selectedArtist != null || selectedAlbum != null || selectedFolder != null || selectedPlaylist != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.shuffleAll(filteredSongs) },
                        colors = ButtonDefaults.buttonColors(containerColor = KadjaPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shuffle_all_button")
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تشغيل عشوائي", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (filteredSongs.isNotEmpty()) {
                                viewModel.playSongFromList(filteredSongs.first(), filteredSongs)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KadjaTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KadjaCardBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("play_all_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = KadjaSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تشغيل الكل", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Main Content Area based on Selected Tab
            when {
                // If a sub-view is active, show the filtered songs list
                selectedArtist != null || selectedAlbum != null || selectedFolder != null || selectedPlaylist != null || selectedTab == MainTab.SONGS || selectedTab == MainTab.FAVORITES -> {
                    if (filteredSongs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.FAVORITES) Icons.Filled.Favorite else Icons.Default.MusicOff,
                                    contentDescription = null,
                                    tint = KadjaTextTertiary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = if (selectedTab == MainTab.FAVORITES) "لا توجد أغانٍ في المفضلة بعد" else "لم يتم العثور على أغانٍ",
                                    color = KadjaTextSecondary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("songs_list"),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(filteredSongs, key = { it.id }) { song ->
                                SongListItem(
                                    song = song,
                                    isCurrentSong = (currentSong?.id == song.id),
                                    isPlaying = isPlaying,
                                    onSongClick = { viewModel.playSongFromList(song, filteredSongs) },
                                    onFavoriteClick = { viewModel.toggleFavorite(song) },
                                    onAddToPlaylistClick = { viewModel.setSongForAddToPlaylist(song) }
                                )
                            }
                        }
                    }
                }

                // Playlists Tab
                selectedTab == MainTab.PLAYLISTS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Create New Playlist Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.setCreatePlaylistOpen(true) }
                                .testTag("create_playlist_button"),
                            color = KadjaSurfaceElevated,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KadjaPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(KadjaPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = KadjaPrimaryGlow,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "إنشاء قائمة تشغيل جديدة",
                                        color = KadjaTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "اجمع أغانيك المفضلة في قائمة واحدة",
                                        color = KadjaTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (playlists.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد قوائم تشغيل حالياً، اضغط أعلاه للبدء!",
                                    color = KadjaTextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(playlists) { playlist ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { viewModel.selectPlaylist(playlist) }
                                            .testTag("playlist_row_${playlist.id}"),
                                        color = KadjaSurface,
                                        shape = RoundedCornerShape(16.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, KadjaCardBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(50.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(KadjaSecondaryVariant, KadjaPrimaryVariant)
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.QueueMusic,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = playlist.name,
                                                    color = KadjaTextPrimary,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "قائمة مخصصة",
                                                    color = KadjaTextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.deletePlaylist(playlist.id) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = "Delete Playlist",
                                                    tint = KadjaTextTertiary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Artists Tab
                selectedTab == MainTab.ARTISTS -> {
                    val artists = remember(viewModel.allSongs.collectAsState().value, searchQuery) {
                        viewModel.getArtists()
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(artists) { artist ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.selectArtist(artist.name) }
                                    .testTag("artist_card_${artist.name}"),
                                color = KadjaSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KadjaCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(KadjaPrimary, KadjaSecondary)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Text(
                                        text = artist.name,
                                        color = KadjaTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${artist.songCount} أغنية",
                                        color = KadjaPrimaryGlow,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Albums Tab
                selectedTab == MainTab.ALBUMS -> {
                    val albums = remember(viewModel.allSongs.collectAsState().value, searchQuery) {
                        viewModel.getAlbums()
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(albums) { album ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.selectAlbum(album.title) }
                                    .testTag("album_card_${album.title}"),
                                color = KadjaSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KadjaCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(KadjaPrimaryVariant, KadjaTertiary)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (album.albumArtUri != null) {
                                            AsyncImage(
                                                model = album.albumArtUri,
                                                contentDescription = album.title,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop,
                                                error = painterResource(id = R.drawable.default_album_art)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Album,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(44.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = album.title,
                                        color = KadjaTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${album.artist} • ${album.songCount} أغنية",
                                        color = KadjaTextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Folders Tab
                selectedTab == MainTab.FOLDERS -> {
                    val folders = remember(viewModel.allSongs.collectAsState().value, searchQuery) {
                        viewModel.getFolders()
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(folders) { folder ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.selectFolder(folder.name) }
                                    .testTag("folder_item_${folder.name}"),
                                color = KadjaSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KadjaCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(KadjaSecondary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = KadjaSecondary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = folder.name,
                                            color = KadjaTextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${folder.songCount} أغنية في هذا المجلد",
                                            color = KadjaTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = KadjaTextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Persistent Mini Player at the bottom
        if (currentSong != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    progressFraction = progressFraction,
                    onMiniPlayerClick = { viewModel.setNowPlayingExpanded(true) },
                    onPlayPauseClick = { viewModel.playerEngine.togglePlayPause() },
                    onNextClick = { viewModel.playerEngine.playNext() }
                )
            }
        }

        // Full Screen Now Playing Modal
        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                song = currentSong,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                repeatMode = repeatMode,
                isShuffle = isShuffle,
                sleepTimerMinutesLeft = sleepTimerMinutesLeft,
                onCollapse = { viewModel.setNowPlayingExpanded(false) },
                onPlayPause = { viewModel.playerEngine.togglePlayPause() },
                onNext = { viewModel.playerEngine.playNext() },
                onPrevious = { viewModel.playerEngine.playPrevious() },
                onSeek = { viewModel.playerEngine.seekTo(it) },
                onToggleShuffle = { viewModel.playerEngine.toggleShuffle() },
                onCycleRepeat = { viewModel.playerEngine.cycleRepeatMode() },
                onToggleFavorite = { currentSong?.let { viewModel.toggleFavorite(it) } },
                onOpenEqualizer = { viewModel.setEqualizerOpen(true) },
                onOpenSleepTimer = { viewModel.setSleepTimerOpen(true) },
                onOpenQueue = { viewModel.setQueueOpen(true) },
                onAddToPlaylist = { currentSong?.let { viewModel.setSongForAddToPlaylist(it) } }
            )
        }

        // Equalizer Sheet
        if (isEqualizerOpen) {
            EqualizerSheet(
                selectedPreset = selectedPreset,
                availablePresets = viewModel.playerEngine.availablePresets,
                onSelectPreset = { viewModel.playerEngine.setEqualizerPreset(it) },
                bassBoostStrength = bassBoostStrength,
                onBassBoostChange = { viewModel.playerEngine.setBassBoost(it) },
                playbackSpeed = playbackSpeed,
                onPlaybackSpeedChange = { viewModel.playerEngine.setPlaybackSpeed(it) },
                volume = volume,
                onVolumeChange = { viewModel.playerEngine.setVolume(it) },
                onDismiss = { viewModel.setEqualizerOpen(false) }
            )
        }

        // Sleep Timer Sheet
        if (isSleepTimerOpen) {
            SleepTimerSheet(
                minutesLeft = sleepTimerMinutesLeft,
                onSetTimer = { viewModel.playerEngine.startSleepTimer(it) },
                onCancelTimer = { viewModel.playerEngine.cancelSleepTimer() },
                onDismiss = { viewModel.setSleepTimerOpen(false) }
            )
        }

        // Queue Sheet
        if (isQueueOpen) {
            QueueSheet(
                queue = queue,
                currentIndex = queueIndex,
                isPlaying = isPlaying,
                onSongSelect = { song, index ->
                    viewModel.playerEngine.playQueue(queue, index)
                },
                onDismiss = { viewModel.setQueueOpen(false) }
            )
        }

        // Add to Playlist Sheet
        songForAddToPlaylist?.let { song ->
            AddToPlaylistSheet(
                song = song,
                playlists = playlists,
                onSelectPlaylist = { playlist ->
                    viewModel.addSongToPlaylist(playlist.id, song)
                },
                onCreateNewClick = {
                    viewModel.setCreatePlaylistOpen(true)
                },
                onDismiss = {
                    viewModel.setSongForAddToPlaylist(null)
                }
            )
        }

        // Create Playlist Dialog
        if (isCreatePlaylistOpen) {
            CreatePlaylistDialog(
                onDismiss = { viewModel.setCreatePlaylistOpen(false) },
                onConfirm = { name -> viewModel.createPlaylist(name) }
            )
        }
    }
}
