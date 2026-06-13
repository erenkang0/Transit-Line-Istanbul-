package com.transitline.istanbul.ui.metro

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transitline.istanbul.R
import com.transitline.istanbul.data.repository.FavoritesRepository
import com.transitline.istanbul.domain.model.FavoriteType
import com.transitline.istanbul.ui.components.FavoriteButton
import com.transitline.istanbul.ui.components.LineBadge
import com.transitline.istanbul.ui.util.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetroScreen(
    powerSaving: Boolean,
    modifier: Modifier = Modifier,
    viewModel: MetroViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val network by viewModel.network.collectAsStateWithLifecycle()
    val favoriteKeys by viewModel.favoriteKeys.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.any { it }) viewModel.locateNow()
    }

    Box(modifier = modifier.fillMaxSize()) {
        MetroCanvas(
            network = network,
            selectedStationId = viewModel.selectedStationId,
            originId = viewModel.originId,
            destinationId = viewModel.destinationId,
            nearestStationId = viewModel.nearestStationId,
            onStationTap = viewModel::select,
            modifier = Modifier.fillMaxSize(),
        )

        // Route + nearest summary, top.
        val origin = viewModel.originId?.let { network.stationsById[it]?.name }
        val destination = viewModel.destinationId?.let { network.stationsById[it]?.name }
        val nearest = viewModel.nearestStationId?.let { network.stationsById[it]?.name }
        if (origin != null || destination != null || nearest != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        nearest?.let { Text(stringResource(R.string.metro_nearest_station, it), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary) }
                        origin?.let { Text(stringResource(R.string.metro_route_origin, it), style = MaterialTheme.typography.bodyMedium) }
                        destination?.let { Text(stringResource(R.string.metro_route_destination, it), style = MaterialTheme.typography.bodyMedium) }
                    }
                    if (origin != null || destination != null) {
                        IconButton(onClick = viewModel::clearRoute) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.metro_clear_route))
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                if (powerSaving) {
                    Toast.makeText(context, context.getString(R.string.metro_location_powersave_off), Toast.LENGTH_SHORT).show()
                } else {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }
            },
            icon = { Icon(Icons.Filled.MyLocation, contentDescription = null) },
            text = { Text(stringResource(R.string.metro_locate_me)) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .safeDrawingPadding()
                .padding(20.dp),
        )
    }

    val selectedId = viewModel.selectedStationId
    if (selectedId != null) {
        val station = network.stationsById[selectedId]
        if (station != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::clearSelection,
                sheetState = sheetState,
            ) {
                StationActionSheet(
                    stationName = station.name,
                    lines = network.linesFor(selectedId),
                    isFavorite = favoriteKeys.contains(FavoritesRepository.key(FavoriteType.METRO_STATION, selectedId)),
                    onToggleFavorite = { viewModel.toggleFavorite(selectedId, station.name) },
                    onFromHere = viewModel::useSelectionAsOrigin,
                    onToHere = viewModel::useSelectionAsDestination,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StationActionSheet(
    stationName: String,
    lines: List<com.transitline.istanbul.domain.model.MetroLine>,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onFromHere: () -> Unit,
    onToHere: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stationName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            FavoriteButton(isFavorite = isFavorite, onToggle = onToggleFavorite)
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.metro_lines_label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            lines.forEach { line -> LineBadge(code = line.code, colorArgb = line.colorArgb) }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = onFromHere, modifier = Modifier.weight(1f).height(52.dp)) {
                Text(stringResource(R.string.metro_from_here))
            }
            FilledTonalButton(onClick = onToHere, modifier = Modifier.weight(1f).height(52.dp)) {
                Text(stringResource(R.string.metro_to_here))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
