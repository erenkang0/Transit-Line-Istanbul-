package com.transitline.istanbul.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transitline.istanbul.R
import com.transitline.istanbul.domain.model.AppSettings
import com.transitline.istanbul.domain.model.FavoriteItem
import com.transitline.istanbul.domain.model.FavoriteType
import com.transitline.istanbul.domain.model.TransitMode
import com.transitline.istanbul.ui.bus.BusScreen
import com.transitline.istanbul.ui.bus.BusViewModel
import com.transitline.istanbul.ui.components.ModeToggle
import com.transitline.istanbul.ui.components.OfflineBanner
import com.transitline.istanbul.ui.metro.MetroScreen
import com.transitline.istanbul.ui.metro.MetroViewModel
import com.transitline.istanbul.ui.settings.SettingsScreen
import com.transitline.istanbul.ui.util.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(settings: AppSettings, isOnline: Boolean, modifier: Modifier = Modifier) {
    var mode by rememberSaveable { mutableStateOf(TransitMode.METRO) }
    var settingsOpen by remember { mutableStateOf(false) }
    var favoritesOpen by remember { mutableStateOf(false) }

    // All ViewModels are Activity-scoped, so these are the same instances the
    // screens use — letting the favorites sheet drive cross-screen navigation.
    val metroViewModel: MetroViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val busViewModel: BusViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val favoritesViewModel: FavoritesViewModel = viewModel(factory = AppViewModelProvider.Factory)

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModeToggle(mode = mode, onModeChange = { mode = it })
                Box(Modifier.weight(1f))
                IconButton(onClick = { favoritesOpen = true }) {
                    Icon(Icons.Filled.Star, contentDescription = stringResource(R.string.favorites_title))
                }
                Spacer(Modifier.width(4.dp))
                FilledIconButton(onClick = { settingsOpen = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
                }
            }

            OfflineBanner(online = isOnline)

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (mode) {
                    TransitMode.METRO -> MetroScreen(powerSaving = settings.powerSaving, viewModel = metroViewModel)
                    TransitMode.BUS -> BusScreen(viewModel = busViewModel)
                }
            }
        }
    }

    if (settingsOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { settingsOpen = false },
            sheetState = sheetState,
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            SettingsScreen()
        }
    }

    if (favoritesOpen) {
        val favorites by favoritesViewModel.favorites.collectAsStateWithLifecycle()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { favoritesOpen = false }, sheetState = sheetState) {
            FavoritesSheet(
                favorites = favorites,
                onRemove = favoritesViewModel::remove,
                onOpen = { item ->
                    when (item.type) {
                        FavoriteType.METRO_STATION -> { mode = TransitMode.METRO; metroViewModel.select(item.refId) }
                        FavoriteType.BUS_STOP -> { mode = TransitMode.BUS; busViewModel.openStop(item.refId) }
                        FavoriteType.BUS_LINE -> { mode = TransitMode.BUS; busViewModel.openLine(item.refId) }
                    }
                    favoritesOpen = false
                },
            )
        }
    }
}

@Composable
private fun FavoritesSheet(
    favorites: List<FavoriteItem>,
    onRemove: (FavoriteItem) -> Unit,
    onOpen: (FavoriteItem) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.favorites_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        if (favorites.isEmpty()) {
            Text(
                text = stringResource(R.string.favorites_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        } else {
            LazyColumn {
                items(favorites, key = { "${it.type}_${it.refId}" }) { item ->
                    ListItem(
                        headlineContent = { Text(item.label) },
                        leadingContent = { Icon(item.icon(), contentDescription = null) },
                        trailingContent = {
                            IconButton(onClick = { onRemove(item) }) {
                                Icon(Icons.Filled.StarBorder, contentDescription = stringResource(R.string.favorite_remove))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(item) },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun FavoriteItem.icon() = when (type) {
    FavoriteType.METRO_STATION -> Icons.Filled.Train
    FavoriteType.BUS_STOP -> Icons.Filled.LocationOn
    FavoriteType.BUS_LINE -> Icons.Filled.DirectionsBus
}
