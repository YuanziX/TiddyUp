package dev.yuanzix.tiddyup.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.yuanzix.tiddyup.models.FilterCriteria

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterChips(
    selected: FilterCriteria,
    onChangeCriteria: (FilterCriteria) -> Unit,
) {

    return FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = spacedBy(12.dp)
    ) {
        Text(
            "Sort",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Bottom),
        )
        FilterChip(
            label = {
                Text("By Album")
            },
            selected = selected == FilterCriteria.ALBUM,
            onClick = {
                onChangeCriteria(FilterCriteria.ALBUM)
            },
        )
        FilterChip(
            label = {
                Text("By Month")
            },
            selected = selected == FilterCriteria.MONTH,
            onClick = {
                onChangeCriteria(FilterCriteria.MONTH)
            },
        )
    }
}

