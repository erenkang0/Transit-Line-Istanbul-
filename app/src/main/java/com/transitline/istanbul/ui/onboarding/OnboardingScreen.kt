package com.transitline.istanbul.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.transitline.istanbul.R
import com.transitline.istanbul.domain.model.AppLanguage
import com.transitline.istanbul.domain.model.TextSize
import com.transitline.istanbul.ui.util.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.onboarding_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.onboarding_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(28.dp))
            SectionLabel(stringResource(R.string.onboarding_language_title))
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChoiceCard(
                    title = stringResource(R.string.language_turkish),
                    selected = viewModel.language == AppLanguage.TURKISH,
                    onClick = { viewModel.selectLanguage(AppLanguage.TURKISH) },
                    modifier = Modifier.weight(1f),
                )
                ChoiceCard(
                    title = stringResource(R.string.language_english),
                    selected = viewModel.language == AppLanguage.ENGLISH,
                    onClick = { viewModel.selectLanguage(AppLanguage.ENGLISH) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(28.dp))
            SectionLabel(stringResource(R.string.onboarding_accessibility_title))
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.onboarding_accessibility_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            ToggleRow(
                title = stringResource(R.string.onboarding_easy_mode),
                checked = viewModel.easyMode,
                onCheckedChange = viewModel::setEasyMode,
            )

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.text_size_label), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                TextSize.entries.forEachIndexed { index, size ->
                    SegmentedButton(
                        selected = viewModel.textSize == size,
                        onClick = { viewModel.selectTextSize(size) },
                        shape = SegmentedButtonDefaults.itemShape(index, TextSize.entries.size),
                    ) {
                        Text(size.labelRes().let { stringResource(it) })
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            ToggleRow(
                title = stringResource(R.string.onboarding_high_contrast),
                checked = viewModel.highContrast,
                onCheckedChange = viewModel::setHighContrast,
            )

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { viewModel.finish(onComplete) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(stringResource(R.string.onboarding_get_started), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceCard(title: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val container = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(containerColor = container),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
        )
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

private fun TextSize.labelRes(): Int = when (this) {
    TextSize.STANDARD -> R.string.text_size_standard
    TextSize.LARGE -> R.string.text_size_large
    TextSize.EXTRA_LARGE -> R.string.text_size_extra_large
}
