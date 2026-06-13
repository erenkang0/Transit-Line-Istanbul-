package com.transitline.istanbul.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.transitline.istanbul.R
import com.transitline.istanbul.domain.model.AppSettings
import com.transitline.istanbul.domain.model.TransitMode
import com.transitline.istanbul.ui.bus.BusScreen
import com.transitline.istanbul.ui.components.ModeToggle
import com.transitline.istanbul.ui.components.OfflineBanner
import com.transitline.istanbul.ui.metro.MetroScreen
import com.transitline.istanbul.ui.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(settings: AppSettings, isOnline: Boolean, modifier: Modifier = Modifier) {
    var mode by rememberSaveable { mutableStateOf(TransitMode.METRO) }
    var settingsOpen by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            // Top row: mode switch (left) + settings (right). No bottom nav bar.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ModeToggle(mode = mode, onModeChange = { mode = it })
                Box(Modifier.weight(1f))
                FilledIconButton(onClick = { settingsOpen = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
                }
            }

            OfflineBanner(online = isOnline)

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (mode) {
                    TransitMode.METRO -> MetroScreen(powerSaving = settings.powerSaving)
                    TransitMode.BUS -> BusScreen()
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
}
