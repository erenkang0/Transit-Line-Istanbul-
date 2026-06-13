package com.transitline.istanbul.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.transitline.istanbul.R
import com.transitline.istanbul.domain.model.TransitMode

/** Polite, non-error info note shown while offline. */
@Composable
fun OfflineBanner(online: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = !online,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Icon(Icons.Filled.CloudOff, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    text = stringResource(R.string.offline_banner),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

/** The Metro / Bus switch that lives at the top-left in place of a nav bar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeToggle(
    mode: TransitMode,
    onModeChange: (TransitMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = mode == TransitMode.METRO,
            onClick = { onModeChange(TransitMode.METRO) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = { Icon(Icons.Filled.Train, contentDescription = null, modifier = Modifier.size(18.dp)) },
        ) {
            Text(stringResource(R.string.mode_metro))
        }
        SegmentedButton(
            selected = mode == TransitMode.BUS,
            onClick = { onModeChange(TransitMode.BUS) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = { Icon(Icons.Filled.DirectionsBus, contentDescription = null, modifier = Modifier.size(18.dp)) },
        ) {
            Text(stringResource(R.string.mode_bus))
        }
    }
}

/** Small filled circle in an official line color with the line code. */
@Composable
fun LineBadge(code: String, colorArgb: Long, modifier: Modifier = Modifier) {
    val color = Color(colorArgb)
    Surface(
        color = color,
        contentColor = Color.White,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
fun FavoriteButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onToggle, modifier = modifier) {
        if (isFavorite) {
            Icon(
                Icons.Filled.Star,
                contentDescription = stringResource(R.string.favorite_remove),
                tint = MaterialTheme.colorScheme.primary,
            )
        } else {
            Icon(Icons.Filled.StarBorder, contentDescription = stringResource(R.string.favorite_add))
        }
    }
}

/** A plain colored dot used in list rows for bus lines without an official color. */
@Composable
fun Dot(color: Color, sizeDp: Int = 10, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(sizeDp.dp).clip(CircleShape).background(color))
}
