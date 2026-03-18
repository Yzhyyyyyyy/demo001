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

// --- 明日方舟颜色定义 ---
val AkSixStar = Color(0xFFFF5722)   // 6星：火橙色
val AkFiveStar = Color(0xFFFFD700)  // 5星：金黄色
val AkFourStar = Color(0xFFB388FF)  // 4星：紫色
val AkThreeStar = Color(0xFF94A3B8) // 3星：灰白色
val AkTextDark = Color(0xFF0F172A)

// 页面枚举
enum class AkTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Headhunt("寻访", Icons.Filled.Star),
    Roster("干员", Icons.AutoMirrored.Filled.List),
    Store("采购", Icons.Filled.ShoppingCart),
    Settings("设置", Icons.Filled.Settings)
}

// 池子枚举
enum class AkPoolType(val title: String) { Standard("标准寻访"), Limited("限定寻访") }

// 充值选项
data class AkTopUpOption(val price: Int, val orundum: Int)
val akTopUpList = listOf(
    AkTopUpOption(6, 600), AkTopUpOption(30, 3000),
    AkTopUpOption(98, 9800), AkTopUpOption(198, 19800),
    AkTopUpOption(328, 32800), AkTopUpOption(648, 64800)
)

data class AkPullResult(val name: String, val rarity: Int, val color: Color)

@Composable
fun ArknightsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    // --- 全局状态 ---
    var currentTab by remember { mutableStateOf(AkTab.Headhunt) }
    // 初始合成玉：刚好够一发单抽(600) + 一发十连(6000) = 6600
    var orundum by remember { mutableIntStateOf(6600) }
    var currentPool by remember { mutableStateOf(AkPoolType.Standard) }

    // 抽卡保底数据 (标准池和限定池水位独立)
    var standardPity by remember { mutableIntStateOf(0) }
    var limitedPity by remember { mutableIntStateOf(0) }

    // 背包数据
    val inventory = remember { mutableStateMapOf<String, Int>() }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                enumValues<AkTab>().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.Bold) },
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, selectedTextColor = AkTextDark,
                            indicatorColor = AkTextDark, unselectedIconColor = Color(0xFF94A3B8), unselectedTextColor = Color(0xFF94A3B8)
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
            // ================= 核心背景：蓝紫圆形渐变光晕 =================
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF7DD3FC).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.1f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD8B4FE).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.8f),
                        radius = size.width * 0.7f
                    )
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部状态栏
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = AkTextDark)
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("♦", color = Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$orundum", fontSize = 16.sp, fontWeight = FontWeight.Black, color = AkTextDark)
                        }
                    }
                }

                // 内容区域切换
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    when (currentTab) {
                        AkTab.Headhunt -> AkHeadhuntTab(
                            orundum = orundum,
                            onOrundumChange = { orundum = it },
                            poolType = currentPool,
                            pity = if (currentPool == AkPoolType.Standard) standardPity else limitedPity,
                            onUpdatePity = { newPity ->
                                if (currentPool == AkPoolType.Standard) standardPity = newPity
                                else limitedPity = newPity
                            },
                            onItemPulled = { item -> inventory[item] = (inventory[item] ?: 0) + 1 },
                            onGoToRecharge = { currentTab = AkTab.Store }
                        )
                        AkTab.Roster -> AkRosterTab(inventory) { inventory.clear() }
                        AkTab.Store -> AkStoreTab { amount ->
                            orundum += amount
                            Toast.makeText(context, "成功采购 $amount 合成玉！", Toast.LENGTH_SHORT).show()
                        }
                        AkTab.Settings -> AkSettingsTab(
                            currentPool = currentPool,
                            onPoolChange = { currentPool = it }
                        )
                    }
                }
            }
        }
    }
}

// ==================== 1. 抽卡页面 (HeadhuntTab) ====================
@Composable
fun AkHeadhuntTab(
    orundum: Int,
    onOrundumChange: (Int) -> Unit,
    poolType: AkPoolType,
    pity: Int,
    onUpdatePity: (Int) -> Unit,
    onItemPulled: (String) -> Unit,
    onGoToRecharge: () -> Unit
) {
    var isPulling by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var currentResults by remember { mutableStateOf<List<AkPullResult>>(emptyList()) }
    var maxRarityPulled by remember { mutableIntStateOf(3) }
    var showRechargePrompt by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // 核心抽卡逻辑 (明日方舟 50抽软保底机制)
    fun doPulls(times: Int) {
        if (orundum < times * 600) {
            showRechargePrompt = true
            return
        }

        onOrundumChange(orundum - times * 600)
        isPulling = true

        coroutineScope.launch {
            val results = mutableListOf<AkPullResult>()
            var tempPity = pity

            for (i in 0 until times) {
                tempPity += 1

                // 超过50抽，每抽增加2%概率
                val rate6 = if (tempPity > 50) 0.02f + (tempPity - 50) * 0.02f else 0.02f
                val roll = Random.nextFloat()

                val result = when {
                    roll < rate6 -> {
                        tempPity = 0 // 出6星，水位清零
                        AkPullResult("★★★★★★", 6, AkSixStar)
                    }
                    roll < rate6 + 0.08f -> AkPullResult("★★★★★", 5, AkFiveStar)
                    roll < rate6 + 0.08f + 0.50f -> AkPullResult("★★★★", 4, AkFourStar)
                    else -> AkPullResult("★★★", 3, AkThreeStar)
                }

                onItemPulled(result.name)
                results.add(result)
            }

            onUpdatePity(tempPity)
            currentResults = results.sortedByDescending { it.rarity }
            maxRarityPulled = currentResults.maxOf { it.rarity }

            delay(2800) // 动画总时长
            isPulling = false
            showResult = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 卡池信息面板
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
                    Text(poolType.title, color = AkTextDark, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("$pity", fontSize = 64.sp, fontWeight = FontWeight.Black, color = AkTextDark)
                    Text("当前未出6星抽数", fontSize = 14.sp, color = Color(0xFF94A3B8))

                    if (pity > 50) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("概率提升中！", color = AkSixStar, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 抽卡按钮
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { doPulls(1) },
                    modifier = Modifier.weight(1f).height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AkTextDark),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("寻访 1 次", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♦", color = Color(0xFFEF4444), fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("600", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Button(
                    onClick = { doPulls(10) },
                    modifier = Modifier.weight(1f).height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AkTextDark, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("寻访 10 次", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♦", color = Color(0xFFEF4444), fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("6000", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 抽卡动画遮罩 ---
        if (isPulling) {
            AkGachaAnimationOverlay(maxRarity = maxRarityPulled)
        }

        // 抽卡结果弹窗
        if (showResult) {
            AlertDialog(
                onDismissRequest = { showResult = false },
                containerColor = Color.White,
                title = { Text("人事部入职报告", color = AkTextDark, fontWeight = FontWeight.Bold) },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentResults) { result ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
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
                        Text("确认签字", color = AkTextDark, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 余额不足弹窗
        if (showRechargePrompt) {
            AlertDialog(
                onDismissRequest = { showRechargePrompt = false },
                title = { Text("合成玉不足", fontWeight = FontWeight.Bold, color = AkTextDark) },
                text = { Text("您的合成玉余额不足，是否前往采购中心？", fontSize = 16.sp) },
                confirmButton = {
                    Button(
                        onClick = { showRechargePrompt = false; onGoToRecharge() },
                        colors = ButtonDefaults.buttonColors(containerColor = AkTextDark)
                    ) {
                        Text("去采购", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRechargePrompt = false }) { Text("取消", color = Color(0xFF64748B)) }
                },
                containerColor = Color.White
            )
        }
    }
}

// --- 抽卡动画组件 (带期待感升格变色) ---
@Composable
fun AkGachaAnimationOverlay(maxRarity: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "gacha")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 2.0f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "scale"
    )

    // 初始颜色永远是白光 (模拟拉开拉链的瞬间)
    var currentColor by remember { mutableStateOf(Color.White) }

    // 延迟 1.2 秒后，根据最高星级发生颜色突变
    LaunchedEffect(maxRarity) {
        delay(1200)
        currentColor = when (maxRarity) {
            6 -> AkSixStar
            5 -> AkFiveStar
            4 -> AkFourStar
            else -> Color.White
        }
    }

    // 颜色渐变动画
    val animatedColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = tween(600), // 变色过程耗时 0.6 秒
        label = "color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Brush.radialGradient(colors = listOf(animatedColor, Color.Transparent)))
        )
        Text(
            text = if (currentColor == AkSixStar) "✨ 发现高级资深干员！✨" else "罗德岛人事部连线中...",
            color = if (currentColor == AkSixStar) AkSixStar else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 150.dp)
        )
    }
}

// ==================== 2. 背包页面 (RosterTab) ====================
@Composable
fun AkRosterTab(inventory: Map<String, Int>, onClear: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("干员名单", color = AkTextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear) { Text("清空名单", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
        }

        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无干员入职，快去寻访吧！", color = Color(0xFF94A3B8))
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
                        it.first.contains("六星") -> 6
                        it.first.contains("五星") -> 5
                        it.first.contains("四星") -> 4
                        else -> 3
                    }
                }) { (name, count) ->
                    val color = when {
                        name.contains("六星") -> AkSixStar
                        name.contains("五星") -> AkFiveStar
                        name.contains("四星") -> AkFourStar
                        else -> AkThreeStar
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
                            Text("拥有: $count", color = AkTextDark, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 3. 充值页面 (StoreTab) ====================
@Composable
fun AkStoreTab(onRecharge: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("采购中心", fontSize = 24.sp, fontWeight = FontWeight.Black, color = AkTextDark, modifier = Modifier.padding(vertical = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(akTopUpList) { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable { onRecharge(option.orundum) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("♦", color = Color(0xFFEF4444), fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${option.orundum}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AkTextDark)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .background(AkTextDark, RoundedCornerShape(8.dp))
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text("¥ ${option.price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 4. 设置页面 (SettingsTab) ====================
@Composable
fun AkSettingsTab(currentPool: AkPoolType, onPoolChange: (AkPoolType) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("寻访设置", color = AkTextDark, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("选择当前寻访目标：", color = Color(0xFF64748B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(
                        onClick = { onPoolChange(AkPoolType.Standard) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPool == AkPoolType.Standard) AkTextDark else Color(0xFFE2E8F0),
                            contentColor = if (currentPool == AkPoolType.Standard) Color.White else Color(0xFF64748B)
                        )
                    ) {
                        Text("标准寻访", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onPoolChange(AkPoolType.Limited) },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPool == AkPoolType.Limited) AkTextDark else Color(0xFFE2E8F0),
                            contentColor = if (currentPool == AkPoolType.Limited) Color.White else Color(0xFF64748B)
                        )
                    ) {
                        Text("限定寻访", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArknightsScreenPreview() {
    ArknightsScreen(onBackClick = {})
}
