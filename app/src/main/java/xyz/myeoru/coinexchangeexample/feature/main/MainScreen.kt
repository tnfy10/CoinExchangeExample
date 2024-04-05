package xyz.myeoru.coinexchangeexample.feature.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import xyz.myeoru.coinexchangeexample.core.constant.CoinSymbols
import xyz.myeoru.coinexchangeexample.data.socket.WebSocketConnectionState
import xyz.myeoru.coinexchangeexample.data.socket.rememberBitThumbSocketManager

@Composable
fun MainScreen(
    onNavigateToCoinInfo: (symbol: String) -> Unit
) {
    var connectionState by remember { mutableStateOf(WebSocketConnectionState.Closed) }
    val bitThumbWebSocketManager = rememberBitThumbSocketManager(
        onConnectionState = { state ->
            connectionState = state
        },
        onReceiveTicker = { ticker ->
            Timber.v(ticker.toString())
        }
    )
    val scope = rememberCoroutineScope()

    LifecycleResumeEffect(key1 = Unit) {
        scope.launch(Dispatchers.IO) {
            bitThumbWebSocketManager.openConnection()
        }

        onPauseOrDispose {
            scope.launch(Dispatchers.IO) {
                bitThumbWebSocketManager.closeConnection()
            }
        }
    }

    LaunchedEffect(key1 = connectionState) {
        withContext(Dispatchers.IO) {
            while (connectionState == WebSocketConnectionState.Open) {
                bitThumbWebSocketManager.requestTicker(CoinSymbols.entries.map { it.name })
                delay(500L)
            }
        }
    }

    MainContainer(
        onNavigateToCoinInfo = onNavigateToCoinInfo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContainer(
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
                    },
                    modifier = Modifier.clickable { onNavigateToCoinInfo(item.name) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MaterialTheme {
        MainContainer(
            onNavigateToCoinInfo = {}
        )
    }
}