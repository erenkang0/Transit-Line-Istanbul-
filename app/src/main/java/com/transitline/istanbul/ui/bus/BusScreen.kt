package com.transitline.istanbul.ui.bus

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transitline.istanbul.R
import com.transitline.istanbul.domain.model.BusArrival
import com.transitline.istanbul.domain.model.BusLine
import com.transitline.istanbul.domain.model.BusStop
import com.transitline.istanbul.domain.model.DirectionStep
import com.transitline.istanbul.ui.util.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScreen(
    modifier: Modifier = Modifier,
    viewModel: BusViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val directions by viewModel.directions.collectAsStateWithLifecycle()

    var pendingStop by remember { mutableStateOf<BusStop?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearQuery) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_clear))
                    }
                }
            },
            placeholder = { Text(stringResource(R.string.bus_search_hint)) },
            shape = MaterialTheme.shapes.large,
        )
        Spacer(Modifier.height(12.dp))

        when (val current = detail) {
            null -> SearchResultsList(
                query = query,
                lines = results.lines,
                stops = results.stops,
                onLineClick = viewModel::openLine,
                onStopClick = viewModel::openStop,
            )
            is BusDetail.Stop -> StopDetail(
                arrivals = current.result.arrivals,
                lines = current.result.linesAtStop,
                stopName = current.result.stop.name,
                onBack = viewModel::closeDetail,
                onLineClick = viewModel::openLine,
            )
            is BusDetail.Line -> LineDetail(
                lineCode = current.result.line.code,
                origin = current.result.line.originName,
                destination = current.result.line.destinationName,
                departures = current.result.departures,
                stops = current.result.stops,
                onBack = viewModel::closeDetail,
                onStopClick = { pendingStop = it },
            )
        }
    }

    // "Do you want to go to this stop?" confirmation.
    val stopToConfirm = pendingStop
    val lineForDirections = (detail as? BusDetail.Line)?.result?.line
    if (stopToConfirm != null && lineForDirections != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = { pendingStop = null }, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.bus_go_to_stop_question),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(stopToConfirm.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        viewModel.requestDirections(lineForDirections.id, stopToConfirm.id)
                        pendingStop = null
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text(stringResource(R.string.bus_go_to_stop_confirm)) }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Step-by-step directions.
    if (directions.isNotEmpty()) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(onDismissRequest = viewModel::clearDirections, sheetState = sheetState) {
            DirectionsSheet(directions)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultsList(
    query: String,
    lines: List<BusLine>,
    stops: List<BusStop>,
    onLineClick: (String) -> Unit,
    onStopClick: (String) -> Unit,
) {
    if (query.isBlank()) {
        EmptyHint(stringResource(R.string.bus_search_empty))
        return
    }
    if (lines.isEmpty() && stops.isEmpty()) {
        EmptyHint(stringResource(R.string.bus_no_results, query))
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (lines.isNotEmpty()) {
            item { ResultSectionHeader(stringResource(R.string.bus_section_lines)) }
            items(lines, key = { "line_${it.id}" }) { line ->
                ListItem(
                    headlineContent = { Text(line.code, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(stringResource(R.string.bus_line_from_to, line.originName, line.destinationName)) },
                    leadingContent = { Icon(Icons.Filled.DirectionsBus, contentDescription = null) },
                    modifier = Modifier.clickableRow { onLineClick(line.id) },
                )
            }
        }
        if (stops.isNotEmpty()) {
            item { ResultSectionHeader(stringResource(R.string.bus_section_stops)) }
            items(stops, key = { "stop_${it.id}" }) { stop ->
                ListItem(
                    headlineContent = { Text(stop.name) },
                    supportingContent = { Text(stringResource(R.string.bus_stop_code, stop.code)) },
                    leadingContent = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                    modifier = Modifier.clickableRow { onStopClick(stop.id) },
                )
            }
        }
    }
}

@Composable
private fun StopDetail(
    arrivals: List<BusArrival>,
    lines: List<BusLine>,
    stopName: String,
    onBack: () -> Unit,
    onLineClick: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        DetailHeader(title = stopName, onBack = onBack)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.bus_lines_through_stop), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(lines, key = { it.id }) { line ->
                com.transitline.istanbul.ui.components.LineBadge(
                    code = line.code,
                    colorArgb = 0xFF37474FL,
                    modifier = Modifier.clickable { onLineClick(line.id) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.bus_arrivals_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(arrivals, key = { "${it.line.id}_${it.etaMinutes}" }) { arrival -> ArrivalRow(arrival) }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.bus_scheduled_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ArrivalRow(arrival: BusArrival) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(arrival.line.code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            val label = if (arrival.etaMinutes <= 0) {
                stringResource(R.string.bus_due)
            } else {
                "${arrival.etaMinutes} ${stringResource(R.string.bus_minutes_short)}"
            }
            Text(label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LineDetail(
    lineCode: String,
    origin: String,
    destination: String,
    departures: List<String>,
    stops: List<BusStop>,
    onBack: () -> Unit,
    onStopClick: (BusStop) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        DetailHeader(title = lineCode, onBack = onBack)
        Text(
            text = stringResource(R.string.bus_line_from_to, origin, destination),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                Text(stringResource(R.string.bus_departures_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    departures.forEach { time ->
                        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = MaterialTheme.shapes.small) {
                            Text(time, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(stringResource(R.string.bus_route_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
            }
            items(stops, key = { it.id }) { stop ->
                RouteStopRow(stop = stop, onClick = { onStopClick(stop) })
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun RouteStopRow(stop: BusStop, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickableRow(onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        com.transitline.istanbul.ui.components.Dot(color = MaterialTheme.colorScheme.primary, sizeDp = 12)
        Spacer(Modifier.width(width = 14.dp))
        Column {
            Text(stop.name, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.bus_stop_code, stop.code), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DirectionsSheet(steps: List<DirectionStep>) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.bus_directions_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        steps.forEachIndexed { index, step ->
            val text = when (step) {
                is DirectionStep.Board -> stringResource(R.string.bus_directions_board, step.lineCode, step.atStopName)
                is DirectionStep.Ride -> stringResource(R.string.bus_directions_ride, step.stopCount)
                is DirectionStep.Alight -> stringResource(R.string.bus_directions_alight, step.atStopName)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.transitline.istanbul.ui.components.Dot(color = MaterialTheme.colorScheme.primary, sizeDp = 10)
                Spacer(Modifier.width(14.dp))
                Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 10.dp))
            }
            if (index < steps.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(start = 4.dp), thickness = 1.dp)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ResultSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun EmptyHint(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(32.dp),
        )
    }
}

/** Small helper so list rows get a ripple-backed click without boilerplate. */
private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.fillMaxWidth().clickable(onClick = onClick)
