package xyz.myeoru.coinexchangeexample.feature.coininfo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.myeoru.coinexchangeexample.core.constant.CoinSymbols

@Composable
fun CoinInfoScreen(
    symbol: String?,
    onBack: () -> Unit
) {
    val coinSymbols = CoinSymbols.entries.find { it.name == symbol }

    if (coinSymbols == null) {
        CoinInfoError(
            onBack = onBack
        )
    } else {
        CoinInfoContainer(
            coinSymbols = coinSymbols,
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoinInfoError(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(top = 150.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "코인 정보를 찾을 수 없습니다."
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoinInfoContainer(
    coinSymbols: CoinSymbols,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = coinSymbols.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = coinSymbols.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.Gray
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding))
    }
}

@Preview
@Composable
private fun CoinInfoScreenPreview() {
    MaterialTheme {
        CoinInfoContainer(
            coinSymbols = CoinSymbols.BTC,
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun CoinInfoErrorPreview() {
    MaterialTheme {
        CoinInfoError(
            onBack = {},
        )
    }
}