package dev.yuanzix.tiddyup.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.yuanzix.tiddyup.models.FilterCriteria
import dev.yuanzix.tiddyup.ui.viewmodels.HomeViewModel

@Composable
fun MonthGrouping(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onNavigateToCleanup: (filterCriteria: FilterCriteria, albumId: Long, albumName: String?, month: String?) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.fetchMonths()
    }

    return LazyColumn(
        modifier = modifier,
        verticalArrangement = spacedBy(8.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(viewModel.months.value.toList()) { month ->
            GroupItem(
                imageUri = month.thumbnailUri,
                label = month.month,
                onClick = {
                    onNavigateToCleanup(FilterCriteria.MONTH, -1, null, month.month)
                }
            )
        }
    }
}