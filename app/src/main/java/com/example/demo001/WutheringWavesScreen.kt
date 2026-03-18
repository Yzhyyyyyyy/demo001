package com.example.demo001

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- 颜色定义 ---
val WwGold = Color(0xFFD4AF37)
val WwPurple = Color(0xFF9370DB)
val WwBlue = Color(0xFF6495ED)
val WwTextDark = Color(0xFF0F172A)

// 页面枚举
enum class WwTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Convene("唤取", Icons.Filled.Star),
    Inventory("背包", Icons.AutoMirrored.Filled.List),
    Recharge("充值", Icons.Filled.ShoppingCart),
    Settings("设置", Icons.Filled.Settings)
}

// 池子枚举（鸣潮特色：角色池有小保底，武器池100%不歪）
enum class WwPoolType(val title: String) {
    Character("限定共鸣者唤取"),
    Weapon("限定武器唤取")
}

// 充值选项（星声）
data class WwTopUpOption(val price: Int, val astrite: Int)
val wwTopUpList = listOf(
    WwTopUpOption(6, 60), WwTopUpOption(30, 300),
    WwTopUpOption(98, 980), WwTopUpOption(198, 1980),
    WwTopUpOption(328, 3280), WwTopUpOption(648, 6480)
)

@Composable
fun WutheringWavesScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    var currentTab by remember { mutableStateOf(WwTab.Convene) }
    var astrite by remember { mutableIntStateOf(1760) }
    var currentPool by remember { mutableStateOf(WwPoolType.Character) }

    var charPity by remember { mutableIntStateOf(0) }
    var charGuaranteed by remember { mutableStateOf(false) }
    var weaponPity by remember { mutableIntStateOf(0) }
    var weaponGuaranteed by remember { mutableStateOf(false) }

    val inventory = remember { mutableStateMapOf<String, Int>() }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                enumValues<WwTab>().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.Bold) },
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, selectedTextColor = WwTextDark,
                            indicatorColor = WwTextDark, unselectedIconColor = Color(0xFF94A3B8), unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF93C5FD).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.1f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFDBA74).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.8f),
                        radius = size.width * 0.7f
                    )
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = WwTextDark)
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✦", color = WwGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$astrite", fontSize = 16.sp, fontWeight = FontWeight.Black, color = WwTextDark)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    when (currentTab) {
                        WwTab.Convene -> WwConveneTab(
                            astrite = astrite,
                            onAstriteChange = { astrite = it },
                            poolType = currentPool,
                            pity = if (currentPool == WwPoolType.Character) charPity else weaponPity,
                            isGuaranteed = if (currentPool == WwPoolType.Character) charGuaranteed else weaponGuaranteed,
                            onUpdatePity = { newPity, newGuaranteed ->
                                if (currentPool == WwPoolType.Character) {
                                    charPity = newPity
                                    charGuaranteed = newGuaranteed
                                } else {
                                    weaponPity = newPity
                                    weaponGuaranteed = newGuaranteed
                                }
                            },
                            onItemPulled = { item -> inventory[item] = (inventory[item] ?: 0) + 1 },
                            onGoToRecharge = { currentTab = WwTab.Recharge }
                        )
                        WwTab.Inventory -> WwInventoryTab(inventory) { inventory.clear() }
                        WwTab.Recharge -> WwRechargeTab { amount ->
                            astrite += amount
                            Toast.makeText(context, "成功充值 $amount 星声！", Toast.LENGTH_SHORT).show()
                        }
                        WwTab.Settings -> WwSettingsTab(
                            currentPool = currentPool,
                            onPoolChange = { currentPool = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WwConveneTab(
    astrite: Int,
    onAstriteChange: (Int) -> Unit,
    poolType: WwPoolType,
    pity: Int,
    isGuaranteed: Boolean,
    onUpdatePity: (Int, Boolean) -> Unit,
    onItemPulled: (String) -> Unit,
    onGoToRecharge: () -> Unit
) {
    var isPulling by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var currentResults by remember { mutableStateOf<List<WwPullResult>>(emptyList()) }
    var maxRarityPulled by remember { mutableIntStateOf(3) }
    var showRechargePrompt by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun doPulls(times: Int) {
        if (astrite < times * 160) {
            showRechargePrompt = true
            return
        }

        onAstriteChange(astrite - times * 160)
        isPulling = true

        coroutineScope.launch {
            val results = mutableListOf<WwPullResult>()
            var tempPity = pity
            var tempGuaranteed = isGuaranteed

            for (i in 0 until times) {
                tempPity += 1

                val hardPity = 80
                val softPity = 65
                val baseProb = 0.008f

                val prob5 = if (tempPity <= softPity) baseProb else baseProb + 0.06f * (tempPity - softPity)
                val is5Star = Random.nextFloat() < prob5 || tempPity >= hardPity

                val result: WwPullResult
                if (is5Star) {
                    tempPity = 0
                    if (poolType == WwPoolType.Character) {
                        // 角色池：50% 概率不歪
                        if (tempGuaranteed) {
                            tempGuaranteed = false
                            result = WwPullResult("限定五星共鸣者", 5, WwGold)
                        } else {
                            if (Random.nextFloat() < 0.5f) {
                                tempGuaranteed = false
                                result = WwPullResult("限定五星共鸣者", 5, WwGold)
                            } else {
                                tempGuaranteed = true
                                result = WwPullResult("常驻五星共鸣者", 5, WwGold)
                            }
                        }
                    } else {
                        // 鸣潮特色：武器池 100% 不歪！
                        tempGuaranteed = false
                        result = WwPullResult("限定五星武器", 5, WwGold)
                    }
                } else if (Random.nextFloat() < 0.1f) {
                    result = WwPullResult("四星", 4, WwPurple)
                } else {
                    result = WwPullResult("三星", 3, WwBlue)
                }

                onItemPulled(result.name)
                results.add(result)
            }

            onUpdatePity(tempPity, tempGuaranteed)
            currentResults = results.sortedByDescending { it.rarity }
            maxRarityPulled = currentResults.maxOf { it.rarity }

            delay(2800)
            isPulling = false
            showResult = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(poolType.title, color = WwTextDark, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("$pity", fontSize = 64.sp, fontWeight = FontWeight.Black, color = WwTextDark)
                    Text("当前已唤取", fontSize = 14.sp, color = Color(0xFF94A3B8))

                    if (poolType == WwPoolType.Weapon) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("鸣潮特色：武器池100%不歪", fontSize = 12.sp, color = WwGold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { doPulls(1) },
                    modifier = Modifier.weight(1f).height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = WwTextDark),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("唤取 1 次", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", color = WwGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("160", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Button(
                    onClick = { doPulls(10) },
                    modifier = Modifier.weight(1f).height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WwTextDark, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("唤取 10 次", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", color = WwGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1600", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isPulling) WwGachaAnimationOverlay(maxRarity = maxRarityPulled)

        if (showResult) {
            AlertDialog(
                onDismissRequest = { showResult = false },
                containerColor = Color.White,
                title = { Text("唤取结果", color = WwTextDark, fontWeight = FontWeight.Bold) },
                text = {
                    LazyVerticalGrid(columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentResults) { result ->
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .background(result.color.copy(alpha = 0.1f))
                                    .border(1.dp, result.color, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(result.name, color = result.color, fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showResult = false }) {
                        Text("确定", color = WwTextDark, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showRechargePrompt) {
            AlertDialog(
                onDismissRequest = { showRechargePrompt = false },
                title = { Text("星声不足", fontWeight = FontWeight.Bold, color = WwTextDark) },
                text = { Text("您的星声余额不足，是否前往充值获取？", fontSize = 16.sp) },
                confirmButton = {
                    Button(
                        onClick = { showRechargePrompt = false; onGoToRecharge() },
                        colors = ButtonDefaults.buttonColors(containerColor = WwTextDark)
                    ) { Text("去充值", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showRechargePrompt = false }) { Text("取消", color = Color(0xFF64748B)) }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun WwGachaAnimationOverlay(maxRarity: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "gacha")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 2.0f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "scale"
    )

    var currentColor by remember { mutableStateOf(WwBlue) }

    LaunchedEffect(maxRarity) {
        delay(1200)
        currentColor = when (maxRarity) {
            5 -> WwGold
            4 -> WwPurple
            else -> WwBlue
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = tween(600),
        label = "color"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp).scale(scale).clip(CircleShape)
                .background(Brush.radialGradient(colors = listOf(animatedColor, Color.Transparent)))
        )
        Text(
            text = if (currentColor == WwGold) "✨ 发现五星信号！✨" else "唤取进行中...",
            color = if (currentColor == WwGold) WwGold else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 150.dp)
        )
    }
}

@Composable
fun WwInventoryTab(inventory: Map<String, Int>, onClear: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("我的背包", color = WwTextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear) { Text("清空背包", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
        }

        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("背包空空如也，快去唤取吧！", color = Color(0xFF94A3B8))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(inventory.toList().sortedByDescending {
                    when {
                        it.first.contains("五星") -> 5
                        it.first.contains("四星") -> 4
                        else -> 3
                    }
                }) { (name, count) ->
                    val color = when {
                        name.contains("五星") -> WwGold
                        name.contains("四星") -> WwPurple
                        else -> WwBlue
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, color),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(name, color = color, fontSize = 16.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("拥有: $count", color = WwTextDark, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WwRechargeTab(onRecharge: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("充值中心", fontSize = 24.sp, fontWeight = FontWeight.Black, color = WwTextDark, modifier = Modifier.padding(vertical = 16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
            items(wwTopUpList) { option ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(110.dp).clickable { onRecharge(option.astrite) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", color = WwGold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${option.astrite}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = WwTextDark)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.background(WwTextDark, RoundedCornerShape(8.dp)).padding(horizontal = 20.dp, vertical = 6.dp)) {
                            Text("¥ ${option.price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WwSettingsTab(currentPool: WwPoolType, onPoolChange: (WwPoolType) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("卡池设置", color = WwTextDark, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 16.dp))
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("选择当前唤取目标：", color = Color(0xFF64748B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(
                        onClick = { onPoolChange(WwPoolType.Character) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPool == WwPoolType.Character) WwTextDark else Color(0xFFE2E8F0),
                            contentColor = if (currentPool == WwPoolType.Character) Color.White else Color(0xFF64748B)
                        )
                    ) { Text("限定角色池", fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = { onPoolChange(WwPoolType.Weapon) },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPool == WwPoolType.Weapon) WwTextDark else Color(0xFFE2E8F0),
                            contentColor = if (currentPool == WwPoolType.Weapon) Color.White else Color(0xFF64748B)
                        )
                    ) { Text("限定武器池", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

data class WwPullResult(val name: String, val rarity: Int, val color: Color)

@Preview(showBackground = true)
@Composable
fun WutheringWavesScreenPreview() {
    WutheringWavesScreen(onBackClick = {})
}
