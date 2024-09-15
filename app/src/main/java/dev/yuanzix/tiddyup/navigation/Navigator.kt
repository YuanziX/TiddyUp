package dev.yuanzix.tiddyup.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.yuanzix.tiddyup.models.FilterCriteria
import dev.yuanzix.tiddyup.ui.screens.base.BaseScreen
import dev.yuanzix.tiddyup.ui.screens.cleanup.CleanupScreen
import dev.yuanzix.tiddyup.ui.screens.home.HomeScreen
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1
import kotlin.reflect.typeOf

@Composable
fun Navigator(
    openAppSettings: KFunction0<Unit>,
    shouldShowRequestPermissionRationale: KFunction1<String, Boolean>,
) {
    val navController = rememberNavController()

    return NavHost(
        navController = navController, startDestination = Base
    ) {
        composable<Base> {
            BaseScreen(openAppSettings = openAppSettings,
                shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
                onNavigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Base) { inclusive = true }
                    }
                })
        }

        composable<Home> {
            HomeScreen(onNavigateToCleanup = { filterCriteria: FilterCriteria, albumId: Long, month: String? ->
                navController.navigate(Cleanup(filterCriteria, albumId, month)) {
                    popUpTo(Home)
                }
            })
        }

        composable<Cleanup>(
            typeMap = mapOf(
                typeOf<FilterCriteria>() to NavType.EnumType(FilterCriteria::class.java)
            )
        ) { entry ->
            val cleanup = entry.toRoute<Cleanup>()
            CleanupScreen(
                filterCriteria = cleanup.filterCriteria,
                albumId = cleanup.albumId,
                month = cleanup.month,
            )
        }
    }
}