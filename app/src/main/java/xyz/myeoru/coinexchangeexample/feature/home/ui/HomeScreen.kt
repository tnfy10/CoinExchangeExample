package xyz.myeoru.coinexchangeexample.feature.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.myeoru.coinexchangeexample.core.constant.CoinChangeType
import xyz.myeoru.coinexchangeexample.core.constant.CoinSymbols
import xyz.myeoru.coinexchangeexample.core.model.Ticker
import xyz.myeoru.coinexchangeexample.feature.home.viewmodel.HomeViewModel
import java.text.DecimalFormat
import kotlin.math.abs

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCoinInfo: (symbol: String) -> Unit
) {
    val coinMap by homeViewModel.coinMapState.collectAsStateWithLifecycle()
    val coinChangeMap by homeViewModel.coinChangeMapState.collectAsStateWithLifecycle()

    HomeContainer(
        coinMap = coinMap,
        coinChangeMap = coinChangeMap,
        onNavigateToCoinInfo = onNavigateToCoinInfo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContainer(
    coinMap: Map<CoinSymbols, Ticker>,
    coinChangeMap: Map<CoinSymbols, CoinChangeType>,
    onNavigateToCoinInfo: (symbol: String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "CoinExchangeExample")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(
                items = CoinSymbols.entries.toList(),
                key = { it.name }
            ) { item ->
                ListItem(
                    headlineContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                            coinMap[item]?.let {
                                val formattedClosePrice = when {
                                    it.closePrice == 0.0 -> "0"
                                    it.closePrice >= 1 -> DecimalFormat("#,##0").format(it.closePrice)
                                    else -> "%.4f".format(it.closePrice)
                                }
                                val absChgAmt = abs(it.chgAmt)
                                val formattedChgAmt = when {
                                    absChgAmt == 0.0 -> "0"
                                    absChgAmt >= 1 -> DecimalFormat("#,##0").format(absChgAmt)
                                    else -> "%.4f".format(absChgAmt)
                                }
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "$formattedClosePrice 원",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "$formattedChgAmt 원" +
                                                "(${if (it.chgRate >= 0) "+" else ""}${
                                                    "%.2f%%".format(
                                                        it.chgRate
                                                    )
                                                })",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = if (it.chgRate >= 0) Color.Red else Color.Blue
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.clickable { onNavigateToCoinInfo(item.name) },
                    colors = ListItemDefaults.colors(
                        containerColor = when (coinChangeMap[item]) {
                            CoinChangeType.Up -> Color.Red.copy(alpha = 0.1f)
                            CoinChangeType.Down -> Color.Blue.copy(alpha = 0.1f)
                            else -> Color.Transparent
                        }
                    )
                )
            }
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeContainer(
            coinMap = emptyMap(),
            coinChangeMap = emptyMap(),
            onNavigateToCoinInfo = {}
        )
    }
}