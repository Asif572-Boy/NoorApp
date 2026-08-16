package com.noor.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.noor.app.ui.screens.AudioQuranScreen
import com.noor.app.ui.screens.DailyAyahScreen
import com.noor.app.ui.screens.PermissionsScreen
import com.noor.app.ui.screens.PrayerTimesScreen
import com.noor.app.ui.screens.ReadQuranScreen
import com.noor.app.ui.theme.DeepGreen
import com.noor.app.ui.theme.Gold
import com.noor.app.ui.theme.SoftGold

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("prayer", "Prayers", Icons.Filled.AccessTime),
    Tab("listen", "Listen", Icons.Filled.Headphones),
    Tab("read", "Read", Icons.Filled.MenuBook),
    Tab("ayah", "Ayah", Icons.Filled.WbSunny)
)

@Composable
fun NoorApp() {
    val nav = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = DeepGreen) {
                val backStack by nav.currentBackStackEntryAsState()
                val current = backStack?.destination
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = current?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepGreen,
                            selectedTextColor = SoftGold,
                            indicatorColor = Gold,
                            unselectedIconColor = SoftGold.copy(alpha = 0.7f),
                            unselectedTextColor = SoftGold.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "prayer",
            modifier = Modifier.padding(padding)
        ) {
            composable("prayer") {
                PrayerTimesScreen(onOpenPermissions = { nav.navigate("permissions") })
            }
            composable("listen") { AudioQuranScreen() }
            composable("read") { ReadQuranScreen() }
            composable("ayah") { DailyAyahScreen() }
            composable("permissions") {
                PermissionsScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
