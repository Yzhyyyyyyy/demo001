package com.example.demo001

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.demo001.ui.theme.Demo001Theme

// 1. 定义我们现在有哪些页面 (新增了 PeaceElite)
enum class AppScreen {
    Start,          // 对应 start.kt
    Choose,         // 对应 choose.kt
    Efootball,      // 对应 efootball_main.kt
    Help,           // 对应 HelpScreen.kt (开发中界面)
    StarRail,       // 对应 StarRailScreen.kt (星穹铁道界面)
    Genshin,        // 对应 GenshinScreen.kt (原神界面)
    WutheringWaves, // 对应 WutheringWavesScreen.kt (鸣潮界面)
    Arknights,      // 对应 ArknightsScreen.kt (明日方舟界面)
    PeaceElite      // ★ 新增：对应 PeaceEliteScreen.kt (和平精英界面)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Demo001Theme {
                // 2. 启动总指挥
                MainApp()
            }
        }
    }
}

// 3. 导航总指挥
@Composable
fun MainApp() {
    // 记住当前状态，默认一开始是 Start (大厅)
    var currentScreen by remember { mutableStateOf(AppScreen.Start) }

    // 根据当前状态，显示不同的页面
    when (currentScreen) {
        AppScreen.Start -> {
            // 显示 start.kt 里的页面
            MyFirstScreen(
                onStartClick = {
                    currentScreen = AppScreen.Choose // 点击后切换到 Choose 状态
                }
            )
        }
        AppScreen.Choose -> {
            // 显示 choose.kt 里的页面
            GameSelectionScreen(
                onNavigate = { gameId ->
                    when (gameId) {
                        "efootball" -> {
                            // 点击实况足球，切换到 Efootball 状态
                            currentScreen = AppScreen.Efootball
                        }
                        "starrail", "star_rail" -> {
                            // 点击星穹铁道，切换到 StarRail 状态 (兼容两种写法)
                            currentScreen = AppScreen.StarRail
                        }
                        "genshin" -> {
                            // 点击原神，切换到 Genshin 状态
                            currentScreen = AppScreen.Genshin
                        }
                        "wuthering_waves" -> {
                            // 点击鸣潮，切换到 WutheringWaves 状态
                            currentScreen = AppScreen.WutheringWaves
                        }
                        "arknights" -> {
                            // 点击明日方舟，切换到 Arknights 状态
                            currentScreen = AppScreen.Arknights
                        }
                        "peace_elite" -> {
                            // ★ 新增：点击和平精英，切换到 PeaceElite 状态
                            currentScreen = AppScreen.PeaceElite
                        }
                        else -> {
                            // 点击其他游戏，切换到 Help 状态
                            currentScreen = AppScreen.Help
                        }
                    }
                }
            )
        }
        AppScreen.Efootball -> {
            // 显示实况足球抽卡界面
            EfootballMainScreen(
                onBackClick = {
                    // 点击左上角返回按钮，退回到 Choose 状态
                    currentScreen = AppScreen.Choose
                }
            )
        }
        AppScreen.StarRail -> {
            // 显示星穹铁道界面
            StarRailScreen(
                onBackClick = {
                    currentScreen = AppScreen.Choose
                }
            )
        }
        AppScreen.Genshin -> {
            // 显示原神界面
            GenshinScreen(
                onBackClick = {
                    currentScreen = AppScreen.Choose
                }
            )
        }
        AppScreen.WutheringWaves -> {
            // 显示鸣潮界面
            WutheringWavesScreen(
                onBackClick = {
                    currentScreen = AppScreen.Choose
                }
            )
        }
        AppScreen.Arknights -> {
            // 显示明日方舟界面
            ArknightsScreen(
                onBackClick = {
                    currentScreen = AppScreen.Choose
                }
            )
        }
        // ★ 新增点：显示和平精英界面
        AppScreen.PeaceElite -> {
            PeaceEliteScreen(
                onBackClick = {
                    currentScreen = AppScreen.Choose
                }
            )
        }
        AppScreen.Help -> {
            HelpScreen(
                onBackClick = {
                    // 点击左上角返回按钮，退回到 Choose 状态
                    currentScreen = AppScreen.Choose
                }
            )
        }
    }
}