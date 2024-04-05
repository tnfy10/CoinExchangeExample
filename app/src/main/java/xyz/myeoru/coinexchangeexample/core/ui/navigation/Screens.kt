package xyz.myeoru.coinexchangeexample.core.ui.navigation

sealed class Screens(val route: String) {
    data object Main : Screens(route = "main")
    data object CoinInfo : Screens(route = "coinInfo") {
        object Keys {
            const val SYMBOL = "symbol"
        }
    }
}