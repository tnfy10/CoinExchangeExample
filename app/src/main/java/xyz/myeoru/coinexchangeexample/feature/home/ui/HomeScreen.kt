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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    val scope = rememberCoroutineScope()
    val coinMap by homeViewModel.coinMapState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(key1 = Unit) {
        with(homeViewModel) {
            fetchCoinCurrentPrice()
            connectSocket()
        }

        onPauseOrDispose {
            scope.launch {
                homeViewModel.closeSocket()
            }
        }
    }

    HomeContainer(
        coinMap = coinMap,
        onNavigateToCoinInfo = onNavigateToCoinInfo
    )
}

private val oldCoinMapMutex = Mutex()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContainer(
    coinMap: Map<String, Ticker>,
    onNavigateToCoinInfo: (symbol: String) -> Unit
) {
    val oldCoinMap = remember {
        mutableStateMapOf(*coinMap.entries.map { Pair(it.key, it.value) }.toTypedArray())
    }

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
                val closePrice by remember(coinMap) {
                    mutableStateOf(coinMap[item.name]?.closePrice)
                }
                var containerColor by remember {
                    mutableStateOf(Color.Transparent)
                }

                LaunchedEffect(key1 = closePrice) {
                    val oldClosePrice = oldCoinMap[item.name]?.closePrice
                    closePrice?.let {
                        containerColor = when {
                            oldClosePrice == null -> Color.Transparent
                            it > oldClosePrice -> Color.Red.copy(alpha = 0.1f)
                            it < oldClosePrice -> Color.Blue.copy(alpha = 0.1f)
                            else -> Color.Transparent
                        }
                        delay(150)
                    }
                    containerColor = Color.Transparent
                    oldCoinMapMutex.withLock {
                        oldCoinMap.putAll(coinMap)
                    }
                }

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
                            coinMap[item.name]?.let {
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
                                val formattedChgRate = when {
                                    it.chgRate > 0 -> "+%.2f%%".format(it.chgRate)
                                    it.chgRate < 0 -> "%.2f%%".format(it.chgRate)
                                    else -> "0.00%"
                                }
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "$formattedClosePrice 원",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "$formattedChgAmt 원($formattedChgRate)",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = when {
                                                it.chgRate > 0 -> Color.Red
                                                it.chgRate < 0 -> Color.Blue
                                                else -> Color.Black
                                            }
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.clickable { onNavigateToCoinInfo(item.name) },
                    colors = ListItemDefaults.colors(
                        containerColor = containerColor
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
            onNavigateToCoinInfo = {}
        )
    }
}