package app.orbitcast.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.orbitcast.OrbitCastApp
import app.orbitcast.ui.editor.FeedEditorScreen
import app.orbitcast.ui.editor.FeedEditorViewModel
import app.orbitcast.ui.episode.EpisodeDetailScreen
import app.orbitcast.ui.episode.EpisodeDetailViewModel
import app.orbitcast.ui.feed.FeedDetailScreen
import app.orbitcast.ui.feed.FeedDetailViewModel
import app.orbitcast.ui.feeds.FeedListScreen
import app.orbitcast.ui.feeds.FeedListViewModel
import app.orbitcast.ui.settings.SettingsScreen
import app.orbitcast.ui.settings.SettingsViewModel

@Composable
fun OrbitCastNav(
    navController: NavHostController = rememberNavController(),
) {
    val app = LocalContext.current.applicationContext as OrbitCastApp
    val container = app.container

    LaunchedEffect(container) {
        container.authEvent.unauthorized.collect {
            navController.navigate(SettingsRoute) {
                launchSingleTop = true
            }
        }
    }
    LaunchedEffect(container) {
        container.shareSeeds.collect { seed ->
            navController.navigate(EditorRoute(seed = seed))
        }
    }
    LaunchedEffect(container) {
        container.deepLinks.collect { feedId ->
            navController.navigate(FeedRoute(feedId))
        }
    }

    NavHost(navController = navController, startDestination = FeedsRoute) {
        composable<FeedsRoute> {
            val vm: FeedListViewModel = viewModel(
                factory = viewModelFactory { FeedListViewModel(container.feeds) },
            )
            FeedListScreen(
                viewModel = vm,
                onFeed = { navController.navigate(FeedRoute(it)) },
                onCreate = { navController.navigate(EditorRoute()) },
                onSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<FeedRoute> { entry ->
            val route = entry.toRoute<FeedRoute>()
            val vm: FeedDetailViewModel = viewModel(
                factory = viewModelFactory {
                    FeedDetailViewModel(route.feedId, container.feeds)
                },
            )
            FeedDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onEpisode = { navController.navigate(EpisodeRoute(route.feedId, it)) },
                onEdit = { navController.navigate(EditorRoute(feedId = route.feedId)) },
                onDeleted = {
                    navController.popBackStack(FeedsRoute, inclusive = false)
                },
            )
        }
        composable<EpisodeRoute> { entry ->
            val route = entry.toRoute<EpisodeRoute>()
            val vm: EpisodeDetailViewModel = viewModel(
                factory = viewModelFactory {
                    EpisodeDetailViewModel(route.feedId, route.episodeId, container.feeds)
                },
            )
            EpisodeDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
        composable<EditorRoute> { entry ->
            val route = entry.toRoute<EditorRoute>()
            val vm: FeedEditorViewModel = viewModel(
                factory = viewModelFactory {
                    FeedEditorViewModel(route.feedId, route.seed, container.feeds)
                },
            )
            FeedEditorScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSaved = { id ->
                    navController.popBackStack()
                    navController.navigate(FeedRoute(id))
                },
            )
        }
        composable<SettingsRoute> {
            val vm: SettingsViewModel = viewModel(
                factory = viewModelFactory {
                    SettingsViewModel(container.session, container.feeds)
                },
            )
            SettingsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
