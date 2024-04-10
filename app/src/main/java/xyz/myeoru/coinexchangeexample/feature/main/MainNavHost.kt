package xyz.myeoru.coinexchangeexample.feature.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import xyz.myeoru.coinexchangeexample.core.ui.navigation.Screens
import xyz.myeoru.coinexchangeexample.core.ui.navigation.animateComposable
import xyz.myeoru.coinexchangeexample.feature.coininfo.CoinInfoScreen
import xyz.myeoru.coinexchangeexample.feature.home.ui.HomeScreen

@Composable
fun MainNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screens.Home.route
    ) {
        composable(
            route = Screens.Home.route
        ) {
            HomeScreen(
                onNavigateToCoinInfo = { symbol ->
                    navController.navigate("${Screens.CoinInfo.route}/$symbol")
                }
            )
        }
        animateComposable(
            route = "${Screens.CoinInfo.route}/{${Screens.CoinInfo.Keys.SYMBOL}}",
            arguments = listOf(
                navArgument(Screens.CoinInfo.Keys.SYMBOL) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val symbol =
                backStackEntry.arguments?.getString(Screens.CoinInfo.Keys.SYMBOL)

            CoinInfoScreen(
                symbol = symbol,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}