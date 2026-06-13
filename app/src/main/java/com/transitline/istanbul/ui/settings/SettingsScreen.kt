package com.transitline.istanbul.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transitline.istanbul.R
import com.transitline.istanbul.domain.model.AppLanguage
import com.transitline.istanbul.domain.model.TextSize
import kotlinx.coroutines.launch

private val GPS_INTERVALS = listOf(5, 10, 15, 30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = com.transitline.istanbul.ui.util.AppViewModelProvider.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val payload = viewModel.buildExport()
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(payload.toByteArray()) }
                }.onSuccess { viewModel.onExported(R.string.settings_export_done) }
                    .onFailure { viewModel.onExported(R.string.settings_import_error) }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val text = runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                }.getOrNull()
                if (text != null) {
                    viewModel.import(text, R.string.settings_import_done, R.string.settings_import_error)
                } else {
                    viewModel.onExported(R.string.settings_import_error)
                }
            }
        }
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, context.getString(it), Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp),
        )

        SettingsSection(stringResource(R.string.settings_section_appearance)) {
            SwitchRow(
                title = stringResource(R.string.settings_power_saving),
                subtitle = stringResource(R.string.settings_power_saving_desc),
                checked = settings.powerSaving,
                onCheckedChange = viewModel::setPowerSaving,
            )
            SwitchRow(
                title = stringResource(R.string.settings_dynamic_color),
                subtitle = stringResource(R.string.settings_dynamic_color_desc),
                checked = settings.dynamicColor,
                onCheckedChange = viewModel::setDynamicColor,
            )
            SwitchRow(
                title = stringResource(R.string.settings_high_contrast),
                subtitle = null,
                checked = settings.highContrast,
                onCheckedChange = viewModel::setHighContrast,
            )
        }

        SettingsSection(stringResource(R.string.settings_language)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = settings.language == AppLanguage.TURKISH,
                    onClick = { viewModel.setLanguage(AppLanguage.TURKISH) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text(stringResource(R.string.language_turkish)) }
                SegmentedButton(
                    selected = settings.language == AppLanguage.ENGLISH,
                    onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text(stringResource(R.string.language_english)) }
            }
        }

        SettingsSection(stringResource(R.string.settings_section_accessibility)) {
            Text(stringResource(R.string.text_size_label), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TextSize.entries.forEachIndexed { index, size ->
                    SegmentedButton(
                        selected = settings.textSize == size,
                        onClick = { viewModel.setTextSize(size) },
                        shape = SegmentedButtonDefaults.itemShape(index, TextSize.entries.size),
                    ) { Text(stringResource(size.labelRes())) }
                }
            }
            Spacer(Modifier.height(8.dp))
            SwitchRow(
                title = stringResource(R.string.settings_easy_mode),
                subtitle = stringResource(R.string.settings_easy_mode_desc),
                checked = settings.easyMode,
                onCheckedChange = viewModel::setEasyMode,
            )
        }

        SettingsSection(stringResource(R.string.settings_section_location)) {
            Text(stringResource(R.string.settings_gps_interval), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GPS_INTERVALS.forEach { minutes ->
                    FilterChip(
                        selected = settings.gpsIntervalMinutes == minutes,
                        onClick = { viewModel.setGpsInterval(minutes) },
                        label = { Text(stringResource(R.string.settings_gps_interval_value, minutes)) },
                    )
                }
            }
        }

        SettingsSection(stringResource(R.string.settings_section_data)) {
            OutlinedButton(
                onClick = { exportLauncher.launch("transit-line-backup.json") },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_export)) }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_import)) }
        }

        SettingsSection(stringResource(R.string.settings_section_about)) {
            Text(
                text = stringResource(R.string.settings_version, com.transitline.istanbul.BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_data_source),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(16.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { content() }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun TextSize.labelRes(): Int = when (this) {
    TextSize.STANDARD -> R.string.text_size_standard
    TextSize.LARGE -> R.string.text_size_large
    TextSize.EXTRA_LARGE -> R.string.text_size_extra_large
}
