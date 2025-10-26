@file:OptIn(ExperimentalAnimationApi::class)

package com.example.vetcare_grupo11.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.vetcare_grupo11.ui.LoginVisualScreen
import com.example.vetcare_grupo11.ui.RegisterVisualScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    // Ruta actual
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "login"

    // Recordar ruta anterior para decidir sentido
    var lastRoute by remember { mutableStateOf(currentRoute) }
    val forward = remember(currentRoute, lastRoute) {
        lastRoute == "login" && currentRoute == "register"
    }
    LaunchedEffect(currentRoute) { lastRoute = currentRoute }

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            val dur = 650
            val ease = FastOutSlowInEasing
            if (forward) {
                // Login -> Registro (entra desde la derecha)
                (slideInHorizontally(
                    initialOffsetX = { full -> (full * 0.9f).toInt() },           // 👈 parámetro nombrado
                    animationSpec = tween(dur, easing = ease)
                ) + fadeIn(
                    animationSpec = tween(dur, easing = ease),
                    initialAlpha = 0.0f
                ) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween((dur * 0.9f).toInt(), easing = ease)
                )) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 2 },       // 👈 parámetro nombrado
                            animationSpec = tween(250)
                        ) + fadeOut())
            } else {
                // Registro -> Login (entra desde la izquierda)
                (slideInHorizontally(
                    initialOffsetX = { full -> (full * 0.9f).toInt() },          // 👈 parámetro nombrado
                    animationSpec = tween(dur, easing = ease)
                ) + fadeIn(
                    animationSpec = tween(dur, easing = ease),
                    initialAlpha = 0.0f
                ) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween((dur * 0.9f).toInt(), easing = ease)
                )) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 2 },       // 👈 parámetro nombrado
                            animationSpec = tween(250)
                        ) + fadeOut(animationSpec = tween(dur, easing = ease)))
            }.using(SizeTransform(clip = false))
        },
        modifier = Modifier.fillMaxSize(),
        label = "NavTransitions"
    ) { route ->                   // 👈 ahora USAMOS el targetState
        key(route) {               // fuerza recomposición por ruta y satisface la inspección
            NavHost(
                navController = navController,
                startDestination = "login",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("login") {
                    LoginVisualScreen(
                        onCreateAccount = { navController.navigate("register") }
                    )
                }
                composable("register") {
                    RegisterVisualScreen(
                        onBackToLogin = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}



