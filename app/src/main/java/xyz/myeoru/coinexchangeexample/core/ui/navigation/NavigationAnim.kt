package xyz.myeoru.coinexchangeexample.core.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.animateComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideIntoAnim()
    },
    exitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutAnim(
            direction = AnimatedContentTransitionScope.SlideDirection.Start
        )
    },
    popEnterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?) = {
        slideIntoAnim(
            direction = AnimatedContentTransitionScope.SlideDirection.End
        )
    },
    popExitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?) = {
        slideOutAnim()
    },
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        content = content
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideIntoAnim(
    direction: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Start
): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            300, easing = LinearEasing
        )
    ) + slideIntoContainer(
        animationSpec = tween(300, easing = EaseIn),
        towards = direction
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOutAnim(
    direction: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.End
): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            300, easing = LinearEasing
        )
    ) + slideOutOfContainer(
        animationSpec = tween(300, easing = EaseOut),
        towards = direction
    )
}