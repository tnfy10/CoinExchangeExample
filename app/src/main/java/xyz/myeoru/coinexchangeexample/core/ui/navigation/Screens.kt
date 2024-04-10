package xyz.myeoru.coinexchangeexample.core.ui.navigation

sealed class Screens(val route: String) {
    data object Home : Screens(route = "home")
    data object CoinInfo : Screens(route = "coinInfo") {
        object Keys {
            const val SYMBOL = "symbol"
        }
    }
}